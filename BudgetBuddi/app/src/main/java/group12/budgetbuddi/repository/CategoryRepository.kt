package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import group12.budgetbuddi.data.entity.CategoryEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
Author: Firebase / Google Developers
Description: Used as reference for reading, writing, updating and deleting user-specific category documents in Cloud Firestore.
Reference: Firebase. 2026. Cloud Firestore documentation. [Online].
Available at: https://firebase.google.com/docs/firestore
Accessed: 19 May 2026.
*/

class CategoryRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "CategoryRepository"
        private const val USERS_COLLECTION = "users"
        private const val CATEGORIES_COLLECTION = "categories"
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun categoriesCollection(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(CATEGORIES_COLLECTION)

    fun observeAllCategories(userId: String): Flow<List<CategoryEntity>> = callbackFlow {
        val listener = categoriesCollection(userId)
            .orderBy("mainCategory")
            .orderBy("subCategory")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error observing categories: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents?.map { document ->
                    CategoryEntity(
                        id = document.id,
                        userId = document.getString("userId") ?: userId,
                        mainCategory = document.getString("mainCategory") ?: "",
                        subCategory = document.getString("subCategory") ?: "",
                        visualType = document.getString("visualType") ?: "emoji",
                        visualValue = document.getString("visualValue") ?: "🍳",
                        outlineColor = document.getString("outlineColor") ?: "#139A43",
                        createdAt = document.getLong("createdAt") ?: 0L,
                        updatedAt = document.getLong("updatedAt") ?: 0L
                    )
                } ?: emptyList()

                trySend(categories)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun insertCategory(userId: String, category: CategoryEntity) {
        val cleanMainCategory = category.mainCategory.trim()
        val cleanSubCategory = category.subCategory.trim()

        if (cleanMainCategory.isBlank() || cleanSubCategory.isBlank()) {
            throw IllegalArgumentException("Category and subcategory cannot be empty")
        }

        val duplicateSnapshot = categoriesCollection(userId)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .whereEqualTo("subCategory", cleanSubCategory)
            .get()
            .await()

        if (!duplicateSnapshot.isEmpty) {
            Log.d(TAG, "Duplicate category ignored: $cleanMainCategory / $cleanSubCategory")
            return
        }

        val documentRef = categoriesCollection(userId).document()
        val now = System.currentTimeMillis()

        val categoryData = hashMapOf(
            "userId" to userId,
            "mainCategory" to cleanMainCategory,
            "subCategory" to cleanSubCategory,
            "visualType" to category.visualType,
            "visualValue" to category.visualValue,
            "outlineColor" to category.outlineColor,
            "createdAt" to now,
            "updatedAt" to now
        )

        documentRef.set(categoryData).await()

        Log.d(TAG, "Category inserted: ${documentRef.id}")
    }

    suspend fun insertCategories(userId: String, categories: List<CategoryEntity>) {
        if (categories.isEmpty()) return

        val batch = firestore.batch()
        val now = System.currentTimeMillis()

        categories.forEach { category ->
            val documentRef = categoriesCollection(userId).document()

            val categoryData = hashMapOf(
                "userId" to userId,
                "mainCategory" to category.mainCategory.trim(),
                "subCategory" to category.subCategory.trim(),
                "visualType" to category.visualType,
                "visualValue" to category.visualValue,
                "outlineColor" to category.outlineColor,
                "createdAt" to now,
                "updatedAt" to now
            )

            batch.set(documentRef, categoryData)
        }

        batch.commit().await()

        Log.d(TAG, "Inserted ${categories.size} default categories")
    }

    suspend fun updateCategory(userId: String, category: CategoryEntity) {
        if (category.id.isBlank()) {
            throw IllegalArgumentException("Cannot update category without Firestore document ID")
        }

        val updates = mapOf(
            "mainCategory" to category.mainCategory.trim(),
            "subCategory" to category.subCategory.trim(),
            "visualType" to category.visualType,
            "visualValue" to category.visualValue,
            "outlineColor" to category.outlineColor,
            "updatedAt" to System.currentTimeMillis()
        )

        categoriesCollection(userId)
            .document(category.id)
            .update(updates)
            .await()

        Log.d(TAG, "Category updated: ${category.id}")
    }

    suspend fun updateCategoryAndMaybeRenameMainCategory(
        userId: String,
        oldMainCategory: String,
        newMainCategory: String,
        selectedCategory: CategoryEntity?
    ) {
        val cleanOldMain = oldMainCategory.trim()
        val cleanNewMain = newMainCategory.trim()

        if (cleanOldMain.isBlank() || cleanNewMain.isBlank()) {
            throw IllegalArgumentException("Main category name cannot be empty")
        }

        val now = System.currentTimeMillis()

        if (cleanOldMain == cleanNewMain) {
            selectedCategory?.let {
                updateCategory(userId, it.copy(mainCategory = cleanNewMain))
            }
            return
        }

        val snapshot = categoriesCollection(userId)
            .whereEqualTo("mainCategory", cleanOldMain)
            .get()
            .await()

        val batch = firestore.batch()

        snapshot.documents.forEach { document ->
            val updates = mutableMapOf<String, Any>(
                "mainCategory" to cleanNewMain,
                "updatedAt" to now
            )

            if (selectedCategory != null && document.id == selectedCategory.id) {
                updates["subCategory"] = selectedCategory.subCategory.trim()
                updates["visualType"] = selectedCategory.visualType
                updates["visualValue"] = selectedCategory.visualValue
                updates["outlineColor"] = selectedCategory.outlineColor
            }

            batch.update(document.reference, updates)
        }

        batch.commit().await()

        Log.d(TAG, "Renamed main category from $cleanOldMain to $cleanNewMain")
    }

    suspend fun deleteCategory(userId: String, category: CategoryEntity) {
        if (category.id.isBlank()) {
            throw IllegalArgumentException("Cannot delete category without Firestore document ID")
        }

        categoriesCollection(userId)
            .document(category.id)
            .delete()
            .await()

        Log.d(TAG, "Category deleted: ${category.id}")
    }

    suspend fun deleteWholeMainCategory(userId: String, mainCategory: String) {
        val cleanMainCategory = mainCategory.trim()

        val snapshot = categoriesCollection(userId)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .get()
            .await()

        if (snapshot.isEmpty) return

        val batch = firestore.batch()

        snapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }

        batch.commit().await()

        Log.d(TAG, "Deleted whole category: $cleanMainCategory")
    }

    suspend fun getCategoryCountForUser(userId: String): Int {
        val snapshot = categoriesCollection(userId)
            .limit(1)
            .get()
            .await()

        return snapshot.size()
    }

    suspend fun seedDefaultCategoriesIfNeeded(userId: String) {
        if (getCategoryCountForUser(userId) > 0) return

        val defaultCategories = listOf(
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Breakfast", visualType = "emoji", visualValue = "🍳", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Lunch", visualType = "emoji", visualValue = "🥪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Dinner", visualType = "emoji", visualValue = "🍽️", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Snacks", visualType = "emoji", visualValue = "🍪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Restaurant", visualType = "emoji", visualValue = "🍴", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Eating Out", visualType = "emoji", visualValue = "🛍️", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, mainCategory = "Food & Drink", subCategory = "Drinks", visualType = "emoji", visualValue = "🥤", outlineColor = "#4CAF50"),

            CategoryEntity(userId = userId, mainCategory = "Transport", subCategory = "Bus", visualType = "emoji", visualValue = "🚌", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, mainCategory = "Transport", subCategory = "Subway", visualType = "emoji", visualValue = "🚇", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, mainCategory = "Transport", subCategory = "Taxi", visualType = "emoji", visualValue = "🚕", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, mainCategory = "Transport", subCategory = "Train", visualType = "emoji", visualValue = "🚆", outlineColor = "#2196F3"),

            CategoryEntity(userId = userId, mainCategory = "Housing", subCategory = "Groceries", visualType = "emoji", visualValue = "🛒", outlineColor = "#795548"),
            CategoryEntity(userId = userId, mainCategory = "Housing", subCategory = "Rent", visualType = "emoji", visualValue = "🏠", outlineColor = "#795548"),
            CategoryEntity(userId = userId, mainCategory = "Housing", subCategory = "Water", visualType = "emoji", visualValue = "💧", outlineColor = "#795548"),
            CategoryEntity(userId = userId, mainCategory = "Housing", subCategory = "Electricity", visualType = "emoji", visualValue = "⚡", outlineColor = "#795548"),

            CategoryEntity(userId = userId, mainCategory = "Entertainment", subCategory = "Movie", visualType = "emoji", visualValue = "🎬", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, mainCategory = "Entertainment", subCategory = "Games", visualType = "emoji", visualValue = "🎮", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, mainCategory = "Entertainment", subCategory = "Music", visualType = "emoji", visualValue = "🎵", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, mainCategory = "Entertainment", subCategory = "Party", visualType = "emoji", visualValue = "🎉", outlineColor = "#9C27B0"),

            CategoryEntity(userId = userId, mainCategory = "Education", subCategory = "School", visualType = "emoji", visualValue = "🏫", outlineColor = "#FF9800"),
            CategoryEntity(userId = userId, mainCategory = "Education", subCategory = "Self-development", visualType = "emoji", visualValue = "📚", outlineColor = "#FF9800"),

            CategoryEntity(userId = userId, mainCategory = "Healthcare", subCategory = "Hospital", visualType = "emoji", visualValue = "🏥", outlineColor = "#F44336"),
            CategoryEntity(userId = userId, mainCategory = "Healthcare", subCategory = "Medicine", visualType = "emoji", visualValue = "💊", outlineColor = "#F44336"),

            CategoryEntity(userId = userId, mainCategory = "Car", subCategory = "Fuel", visualType = "emoji", visualValue = "⛽", outlineColor = "#607D8B"),
            CategoryEntity(userId = userId, mainCategory = "Car", subCategory = "Parking", visualType = "emoji", visualValue = "🅿️", outlineColor = "#607D8B")
        )

        insertCategories(userId, defaultCategories)
    }
}