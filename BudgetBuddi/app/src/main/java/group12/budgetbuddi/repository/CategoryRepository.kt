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
        Log.d(TAG, "Starting category listener for userId=$userId")

        val listener = categoriesCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing categories: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Category snapshot is null for userId=$userId")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Raw category documents found: ${snapshot.documents.size}")

                val categories = snapshot.documents.mapNotNull { document ->
                    val mainCategory = document.getString("mainCategory") ?: ""
                    val subCategory = document.getString("subCategory") ?: ""

                    if (mainCategory.isBlank() || subCategory.isBlank()) {
                        Log.w(
                            TAG,
                            "Skipping invalid category document ${document.id}. mainCategory='$mainCategory', subCategory='$subCategory'"
                        )
                        return@mapNotNull null
                    }

                    CategoryEntity(
                        id = document.id,
                        userId = document.getString("userId") ?: userId,
                        categoryType = document.getString("categoryType") ?: "expense",
                        mainCategory = mainCategory,
                        subCategory = subCategory,
                        visualType = document.getString("visualType") ?: "emoji",
                        visualValue = document.getString("visualValue") ?: "🍳",
                        outlineColor = document.getString("outlineColor") ?: "#139A43",
                        createdAt = document.getLong("createdAt") ?: 0L,
                        updatedAt = document.getLong("updatedAt") ?: 0L
                    )
                }.sortedWith(
                    compareBy<CategoryEntity> { it.categoryType.lowercase() }
                        .thenBy { it.mainCategory.lowercase() }
                        .thenBy { it.subCategory.lowercase() }
                )

                Log.d(TAG, "Observed valid categories: ${categories.size} for userId=$userId")
                trySend(categories)
            }

        awaitClose {
            Log.d(TAG, "Closing category listener for userId=$userId")
            listener.remove()
        }
    }

    suspend fun insertCategory(userId: String, category: CategoryEntity) {
        val cleanCategoryType = when (category.categoryType.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        val cleanMainCategory = category.mainCategory.trim()
        val cleanSubCategory = category.subCategory.trim()

        if (cleanMainCategory.isBlank() || cleanSubCategory.isBlank()) {
            throw IllegalArgumentException("Category and subcategory cannot be empty")
        }

        Log.d(
            TAG,
            "Attempting to insert category for userId=$userId: $cleanCategoryType / $cleanMainCategory / $cleanSubCategory"
        )

        val duplicateSnapshot = categoriesCollection(userId)
            .whereEqualTo("categoryType", cleanCategoryType)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .whereEqualTo("subCategory", cleanSubCategory)
            .get()
            .await()

        if (!duplicateSnapshot.isEmpty) {
            Log.d(TAG, "Duplicate category ignored: $cleanCategoryType / $cleanMainCategory / $cleanSubCategory")
            return
        }

        val documentRef = categoriesCollection(userId).document()
        val now = System.currentTimeMillis()

        val categoryData = hashMapOf(
            "userId" to userId,
            "categoryType" to cleanCategoryType,
            "mainCategory" to cleanMainCategory,
            "subCategory" to cleanSubCategory,
            "visualType" to category.visualType,
            "visualValue" to category.visualValue,
            "outlineColor" to category.outlineColor,
            "createdAt" to now,
            "updatedAt" to now
        )

        documentRef.set(categoryData).await()
        Log.d(TAG, "Category inserted with documentId=${documentRef.id}")
    }

    suspend fun updateCategory(userId: String, category: CategoryEntity) {
        if (category.id.isBlank()) {
            throw IllegalArgumentException("Cannot update category without Firestore document ID")
        }

        val cleanCategoryType = when (category.categoryType.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        val cleanMainCategory = category.mainCategory.trim()
        val cleanSubCategory = category.subCategory.trim()

        if (cleanMainCategory.isBlank() || cleanSubCategory.isBlank()) {
            throw IllegalArgumentException("Category and subcategory cannot be empty")
        }

        val updates = mapOf(
            "userId" to userId,
            "categoryType" to cleanCategoryType,
            "mainCategory" to cleanMainCategory,
            "subCategory" to cleanSubCategory,
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
                updateCategory(
                    userId = userId,
                    category = it.copy(
                        userId = userId,
                        mainCategory = cleanNewMain,
                        subCategory = it.subCategory.trim()
                    )
                )
            }
            return
        }

        var query = categoriesCollection(userId)
            .whereEqualTo("mainCategory", cleanOldMain)

        val snapshot = query.get().await()

        if (snapshot.isEmpty) {
            Log.w(TAG, "No documents found when renaming main category: $cleanOldMain")
            return
        }

        val selectedType = selectedCategory?.categoryType ?: "expense"
        val batch = firestore.batch()

        snapshot.documents.forEach { document ->
            val documentType = document.getString("categoryType") ?: "expense"

            if (documentType != selectedType) {
                return@forEach
            }

            val updates = mutableMapOf<String, Any>(
                "userId" to userId,
                "categoryType" to selectedType,
                "mainCategory" to cleanNewMain,
                "updatedAt" to now
            )

            if (selectedCategory != null && document.id == selectedCategory.id) {
                val cleanSubCategory = selectedCategory.subCategory.trim()

                if (cleanSubCategory.isBlank()) {
                    throw IllegalArgumentException("Subcategory cannot be empty")
                }

                updates["subCategory"] = cleanSubCategory
                updates["visualType"] = selectedCategory.visualType
                updates["visualValue"] = selectedCategory.visualValue
                updates["outlineColor"] = selectedCategory.outlineColor
            }

            batch.update(document.reference, updates)
        }

        batch.commit().await()
        Log.d(TAG, "Renamed main category from '$cleanOldMain' to '$cleanNewMain'")
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

    suspend fun deleteWholeMainCategory(
        userId: String,
        mainCategory: String,
        categoryType: String = "expense"
    ) {
        val cleanMainCategory = mainCategory.trim()
        val cleanCategoryType = when (categoryType.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        if (cleanMainCategory.isBlank()) {
            throw IllegalArgumentException("Main category cannot be empty")
        }

        val snapshot = categoriesCollection(userId)
            .whereEqualTo("categoryType", cleanCategoryType)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.w(TAG, "No documents found for whole category delete: $cleanCategoryType / $cleanMainCategory")
            return
        }

        val batch = firestore.batch()
        snapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }

        batch.commit().await()
        Log.d(TAG, "Deleted whole category: $cleanCategoryType / $cleanMainCategory")
    }

    suspend fun seedDefaultCategoriesIfNeeded(userId: String) {
        val defaultCategories = listOf(
            // Expense defaults
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Breakfast", visualType = "emoji", visualValue = "🍳", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Lunch", visualType = "emoji", visualValue = "🥪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Dinner", visualType = "emoji", visualValue = "🍽️", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Snacks", visualType = "emoji", visualValue = "🍪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Restaurant", visualType = "emoji", visualValue = "🍴", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Drinks", visualType = "emoji", visualValue = "🥤", outlineColor = "#4CAF50"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Bus", visualType = "emoji", visualValue = "🚌", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Taxi", visualType = "emoji", visualValue = "🚕", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Train", visualType = "emoji", visualValue = "🚆", outlineColor = "#2196F3"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Rent", visualType = "emoji", visualValue = "🏠", outlineColor = "#795548"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Water", visualType = "emoji", visualValue = "💧", outlineColor = "#795548"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Electricity", visualType = "emoji", visualValue = "⚡", outlineColor = "#795548"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Movie", visualType = "emoji", visualValue = "🎬", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Games", visualType = "emoji", visualValue = "🎮", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Music", visualType = "emoji", visualValue = "🎵", outlineColor = "#9C27B0"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Education", subCategory = "School", visualType = "emoji", visualValue = "🏫", outlineColor = "#FF9800"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Education", subCategory = "Self-development", visualType = "emoji", visualValue = "📚", outlineColor = "#FF9800"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Healthcare", subCategory = "Hospital", visualType = "emoji", visualValue = "🏥", outlineColor = "#F44336"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Healthcare", subCategory = "Medicine", visualType = "emoji", visualValue = "💊", outlineColor = "#F44336"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Car", subCategory = "Fuel", visualType = "emoji", visualValue = "⛽", outlineColor = "#607D8B"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Car", subCategory = "Parking", visualType = "emoji", visualValue = "🅿️", outlineColor = "#607D8B"),

            // Income defaults
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Salary", subCategory = "Monthly Salary", visualType = "emoji", visualValue = "💼", outlineColor = "#139A43"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Salary", subCategory = "Part-time Job", visualType = "emoji", visualValue = "🕒", outlineColor = "#139A43"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Investments", subCategory = "Dividends", visualType = "emoji", visualValue = "📈", outlineColor = "#2E7D32"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Investments", subCategory = "Interest", visualType = "emoji", visualValue = "🏦", outlineColor = "#2E7D32"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Allowance", subCategory = "Family Allowance", visualType = "emoji", visualValue = "🤝", outlineColor = "#00A86B"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Allowance", subCategory = "Student Allowance", visualType = "emoji", visualValue = "🎓", outlineColor = "#00A86B"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Bonus", subCategory = "Work Bonus", visualType = "emoji", visualValue = "🎁", outlineColor = "#43A047"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Bonus", subCategory = "Gift Bonus", visualType = "emoji", visualValue = "💝", outlineColor = "#43A047"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Other", subCategory = "Other Income", visualType = "emoji", visualValue = "➕", outlineColor = "#607D8B")
        )

        defaultCategories.forEach { category ->
            insertCategory(userId, category)
        }

        Log.d(TAG, "Default category seeding checked for userId=$userId")
    }
}