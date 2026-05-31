package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import group12.budgetbuddi.data.entity.CategoryEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "CategoryRepository"
        private const val USERS_COLLECTION = "users"
        private const val CATEGORIES_COLLECTION = "categories"

        private val categoryCacheByUser = mutableMapOf<String, List<CategoryEntity>>()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCachedCategories(userId: String): List<CategoryEntity> {
        return categoryCacheByUser[userId].orEmpty()
    }

    private fun categoriesCollection(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(CATEGORIES_COLLECTION)

    fun observeAllCategories(userId: String): Flow<List<CategoryEntity>> = callbackFlow {
        if (userId.isBlank()) {
            Log.e(TAG, "Cannot observe categories because userId is blank")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cached = getCachedCategories(userId)

        if (cached.isNotEmpty()) {
            trySend(cached)
        }

        Log.d(TAG, "Starting category listener for userId=$userId")

        val listener = categoriesCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing categories: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Category snapshot is null for userId=$userId")
                    if (cached.isEmpty()) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }

                val categories = snapshot.documents.mapNotNull { document ->
                    val mainCategory = document.getString("mainCategory")?.trim() ?: ""
                    val subCategory = document.getString("subCategory")?.trim() ?: ""

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
                        categoryType = when (document.getString("categoryType")?.trim()?.lowercase()) {
                            "income" -> "income"
                            else -> "expense"
                        },
                        mainCategory = mainCategory,
                        subCategory = subCategory,
                        visualType = document.getString("visualType") ?: "emoji",
                        visualValue = document.getString("visualValue") ?: "🍳",
                        outlineColor = document.getString("outlineColor") ?: "#139A43",
                        createdAt = document.getLong("createdAt") ?: 0L,
                        updatedAt = document.getLong("updatedAt") ?: 0L
                    )
                }
                    .distinctBy {
                        "${it.categoryType.lowercase()}|${it.mainCategory.lowercase()}|${it.subCategory.lowercase()}"
                    }
                    .sortedWith(
                        compareBy<CategoryEntity> { it.categoryType.lowercase() }
                            .thenBy { it.mainCategory.lowercase() }
                            .thenBy { it.subCategory.lowercase() }
                    )

                categoryCacheByUser[userId] = categories

                Log.d(TAG, "Observed valid categories: ${categories.size} for userId=$userId")
                trySend(categories)
            }

        awaitClose {
            Log.d(TAG, "Closing category listener for userId=$userId")
            listener.remove()
        }
    }

    suspend fun insertCategory(userId: String, category: CategoryEntity) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot insert category because userId is blank")
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

        val duplicateSnapshot = categoriesCollection(userId)
            .whereEqualTo("categoryType", cleanCategoryType)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .whereEqualTo("subCategory", cleanSubCategory)
            .get()
            .await()

        if (!duplicateSnapshot.isEmpty) {
            Log.d(
                TAG,
                "Duplicate category ignored for userId=$userId: $cleanCategoryType / $cleanMainCategory / $cleanSubCategory"
            )
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

        val updatedCache = categoryCacheByUser[userId].orEmpty()
            .plus(
                category.copy(
                    id = documentRef.id,
                    userId = userId,
                    categoryType = cleanCategoryType,
                    mainCategory = cleanMainCategory,
                    subCategory = cleanSubCategory,
                    createdAt = now,
                    updatedAt = now
                )
            )
            .distinctBy {
                "${it.categoryType.lowercase()}|${it.mainCategory.lowercase()}|${it.subCategory.lowercase()}"
            }
            .sortedWith(
                compareBy<CategoryEntity> { it.categoryType.lowercase() }
                    .thenBy { it.mainCategory.lowercase() }
                    .thenBy { it.subCategory.lowercase() }
            )

        categoryCacheByUser[userId] = updatedCache

        Log.d(TAG, "Category inserted with documentId=${documentRef.id} for userId=$userId")
    }

    suspend fun updateCategory(userId: String, category: CategoryEntity) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot update category because userId is blank")
        }

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

        val duplicateSnapshot = categoriesCollection(userId)
            .whereEqualTo("categoryType", cleanCategoryType)
            .whereEqualTo("mainCategory", cleanMainCategory)
            .whereEqualTo("subCategory", cleanSubCategory)
            .get()
            .await()

        val hasOtherDuplicate = duplicateSnapshot.documents.any { it.id != category.id }

        if (hasOtherDuplicate) {
            throw IllegalArgumentException("This category already exists")
        }

        val now = System.currentTimeMillis()

        val updates = mapOf(
            "userId" to userId,
            "categoryType" to cleanCategoryType,
            "mainCategory" to cleanMainCategory,
            "subCategory" to cleanSubCategory,
            "visualType" to category.visualType,
            "visualValue" to category.visualValue,
            "outlineColor" to category.outlineColor,
            "updatedAt" to now
        )

        categoriesCollection(userId)
            .document(category.id)
            .update(updates)
            .await()

        val updatedCategory = category.copy(
            userId = userId,
            categoryType = cleanCategoryType,
            mainCategory = cleanMainCategory,
            subCategory = cleanSubCategory,
            updatedAt = now
        )

        categoryCacheByUser[userId] = categoryCacheByUser[userId].orEmpty()
            .map { if (it.id == category.id) updatedCategory else it }
            .distinctBy {
                "${it.categoryType.lowercase()}|${it.mainCategory.lowercase()}|${it.subCategory.lowercase()}"
            }
            .sortedWith(
                compareBy<CategoryEntity> { it.categoryType.lowercase() }
                    .thenBy { it.mainCategory.lowercase() }
                    .thenBy { it.subCategory.lowercase() }
            )

        Log.d(TAG, "Category updated: ${category.id} for userId=$userId")
    }

    suspend fun updateCategoryAndMaybeRenameMainCategory(
        userId: String,
        oldMainCategory: String,
        newMainCategory: String,
        selectedCategory: CategoryEntity?
    ) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot rename category because userId is blank")
        }

        val cleanOldMain = oldMainCategory.trim()
        val cleanNewMain = newMainCategory.trim()

        if (cleanOldMain.isBlank() || cleanNewMain.isBlank()) {
            throw IllegalArgumentException("Main category name cannot be empty")
        }

        val selectedType = when (selectedCategory?.categoryType?.trim()?.lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        val now = System.currentTimeMillis()

        if (cleanOldMain == cleanNewMain) {
            selectedCategory?.let {
                updateCategory(
                    userId = userId,
                    category = it.copy(
                        userId = userId,
                        categoryType = selectedType,
                        mainCategory = cleanNewMain,
                        subCategory = it.subCategory.trim()
                    )
                )
            }
            return
        }

        val snapshot = categoriesCollection(userId)
            .whereEqualTo("categoryType", selectedType)
            .whereEqualTo("mainCategory", cleanOldMain)
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.w(TAG, "No documents found when renaming main category: $selectedType / $cleanOldMain")
            return
        }

        val batch = firestore.batch()

        snapshot.documents.forEach { document ->
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

        categoryCacheByUser.remove(userId)

        Log.d(TAG, "Renamed main category from '$cleanOldMain' to '$cleanNewMain' for userId=$userId")
    }

    suspend fun deleteCategory(userId: String, category: CategoryEntity) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot delete category because userId is blank")
        }

        if (category.id.isBlank()) {
            throw IllegalArgumentException("Cannot delete category without Firestore document ID")
        }

        categoriesCollection(userId)
            .document(category.id)
            .delete()
            .await()

        categoryCacheByUser[userId] = categoryCacheByUser[userId].orEmpty()
            .filterNot { it.id == category.id }

        Log.d(TAG, "Category deleted: ${category.id} for userId=$userId")
    }

    suspend fun deleteWholeMainCategory(
        userId: String,
        mainCategory: String,
        categoryType: String = "expense"
    ) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot delete main category because userId is blank")
        }

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

        categoryCacheByUser[userId] = categoryCacheByUser[userId].orEmpty()
            .filterNot {
                it.categoryType == cleanCategoryType &&
                        it.mainCategory == cleanMainCategory
            }

        Log.d(TAG, "Deleted whole category: $cleanCategoryType / $cleanMainCategory for userId=$userId")
    }

    suspend fun seedDefaultCategoriesIfNeeded(userId: String) {
        if (userId.isBlank()) {
            Log.e(TAG, "Cannot seed categories because userId is blank")
            return
        }

        val defaultCategories = listOf(
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Breakfast", visualType = "emoji", visualValue = "🍳", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Lunch", visualType = "emoji", visualValue = "🥪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Dinner", visualType = "emoji", visualValue = "🍽️", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Snacks", visualType = "emoji", visualValue = "🍪", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Restaurant", visualType = "emoji", visualValue = "🍴", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Eating Out", visualType = "emoji", visualValue = "🛍️", outlineColor = "#4CAF50"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Food & Drink", subCategory = "Drinks", visualType = "emoji", visualValue = "🥤", outlineColor = "#4CAF50"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Bus", visualType = "emoji", visualValue = "🚌", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Subway", visualType = "emoji", visualValue = "🚇", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Taxi", visualType = "emoji", visualValue = "🚕", outlineColor = "#2196F3"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Transport", subCategory = "Train", visualType = "emoji", visualValue = "🚆", outlineColor = "#2196F3"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Groceries", visualType = "emoji", visualValue = "🛒", outlineColor = "#795548"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Rent", visualType = "emoji", visualValue = "🏠", outlineColor = "#795548"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Water", visualType = "emoji", visualValue = "💧", outlineColor = "#795548"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Housing", subCategory = "Electricity", visualType = "emoji", visualValue = "⚡", outlineColor = "#795548"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Movie", visualType = "emoji", visualValue = "🎬", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Games", visualType = "emoji", visualValue = "🎮", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Music", visualType = "emoji", visualValue = "🎵", outlineColor = "#9C27B0"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Entertainment", subCategory = "Party", visualType = "emoji", visualValue = "🎉", outlineColor = "#9C27B0"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Education", subCategory = "School", visualType = "emoji", visualValue = "🏫", outlineColor = "#FF9800"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Education", subCategory = "Self-development", visualType = "emoji", visualValue = "📚", outlineColor = "#FF9800"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Healthcare", subCategory = "Hospital", visualType = "emoji", visualValue = "🏥", outlineColor = "#F44336"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Healthcare", subCategory = "Medicine", visualType = "emoji", visualValue = "💊", outlineColor = "#F44336"),

            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Car", subCategory = "Fuel", visualType = "emoji", visualValue = "⛽", outlineColor = "#607D8B"),
            CategoryEntity(userId = userId, categoryType = "expense", mainCategory = "Car", subCategory = "Parking", visualType = "emoji", visualValue = "🅿️", outlineColor = "#607D8B"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Salary", subCategory = "Monthly Salary", visualType = "emoji", visualValue = "💼", outlineColor = "#2E7D32"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Salary", subCategory = "Part-time Job", visualType = "emoji", visualValue = "🕒", outlineColor = "#388E3C"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Investments", subCategory = "Dividends", visualType = "emoji", visualValue = "📈", outlineColor = "#1565C0"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Investments", subCategory = "Interest", visualType = "emoji", visualValue = "🏦", outlineColor = "#1976D2"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Allowance", subCategory = "Family Allowance", visualType = "emoji", visualValue = "🤝", outlineColor = "#00897B"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Allowance", subCategory = "Student Allowance", visualType = "emoji", visualValue = "🎓", outlineColor = "#00ACC1"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Bonus", subCategory = "Work Bonus", visualType = "emoji", visualValue = "🎁", outlineColor = "#F9A825"),
            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Bonus", subCategory = "Gift Bonus", visualType = "emoji", visualValue = "💝", outlineColor = "#FB8C00"),

            CategoryEntity(userId = userId, categoryType = "income", mainCategory = "Other", subCategory = "Other Income", visualType = "emoji", visualValue = "➕", outlineColor = "#6A1B9A")
        )

        defaultCategories.forEach { category ->
            insertCategory(userId, category)
        }

        Log.d(TAG, "Default category seeding checked for userId=$userId")
    }
}