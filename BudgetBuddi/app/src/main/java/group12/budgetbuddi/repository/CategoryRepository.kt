package group12.budgetbuddi.repository

import group12.budgetbuddi.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/*
Author: Android Developers
Description: Used as reference for separating database access from the UI layer using a repository-style data layer.
Reference: Android Developers. 2019. Data layer. [Online]. Available at: https://developer.android.com/topic/architecture/data-layer [Accessed 29 April 2026].
*/

class CategoryRepository(private val categoryDao: CategoryDao) {

    // Read functions keep the ViewModel away from direct SQL or DAO details.
    fun getAllCategories(userEmail: String): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(userEmail)
    }

    fun getMainCategories(userEmail: String): Flow<List<String>> {
        return categoryDao.getMainCategories(userEmail)
    }

    fun searchCategories(userEmail: String, query: String): Flow<List<CategoryEntity>> {
        return categoryDao.searchCategories(userEmail, query)
    }

    fun getSubCategories(
        userEmail: String,
        mainCategory: String
    ): Flow<List<CategoryEntity>> {
        return categoryDao.getSubCategoriesForMainCategory(userEmail, mainCategory)
    }

    // Create
    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<CategoryEntity>) {
        categoryDao.insertCategories(categories)
    }

    // Update
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    // Delete
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteWholeCategory(userEmail: String, mainCategory: String) {
        categoryDao.deleteWholeMainCategory(userEmail, mainCategory)
    }

    // Utility
    suspend fun getCategoryCountForUser(userEmail: String): Int {
        return categoryDao.getCategoryCountForUser(userEmail)
    }

    // Adds starter categories only once for each user.
    suspend fun seedDefaultCategoriesIfNeeded(userEmail: String) {
        if (categoryDao.getCategoryCountForUser(userEmail) > 0) return

        val defaultCategories = listOf(
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Breakfast", visualType = "emoji", visualValue = "🍳", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Lunch", visualType = "emoji", visualValue = "🥪", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Dinner", visualType = "emoji", visualValue = "🍽️", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Snacks", visualType = "emoji", visualValue = "🍪", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Restaurant", visualType = "emoji", visualValue = "🍴", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Eating Out", visualType = "emoji", visualValue = "🛍️", outlineColor = "#4CAF50"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Food & Drink", subCategory = "Drinks", visualType = "emoji", visualValue = "🥤", outlineColor = "#4CAF50"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Transport", subCategory = "Bus", visualType = "emoji", visualValue = "🚌", outlineColor = "#2196F3"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Transport", subCategory = "Subway", visualType = "emoji", visualValue = "🚇", outlineColor = "#2196F3"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Transport", subCategory = "Taxi", visualType = "emoji", visualValue = "🚕", outlineColor = "#2196F3"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Transport", subCategory = "Train", visualType = "emoji", visualValue = "🚆", outlineColor = "#2196F3"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Housing", subCategory = "Groceries", visualType = "emoji", visualValue = "🛒", outlineColor = "#795548"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Housing", subCategory = "Rent", visualType = "emoji", visualValue = "🏠", outlineColor = "#795548"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Housing", subCategory = "Water", visualType = "emoji", visualValue = "💧", outlineColor = "#795548"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Housing", subCategory = "Electricity", visualType = "emoji", visualValue = "⚡", outlineColor = "#795548"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Entertainment", subCategory = "Movie", visualType = "emoji", visualValue = "🎬", outlineColor = "#9C27B0"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Entertainment", subCategory = "Games", visualType = "emoji", visualValue = "🎮", outlineColor = "#9C27B0"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Entertainment", subCategory = "Music", visualType = "emoji", visualValue = "🎵", outlineColor = "#9C27B0"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Entertainment", subCategory = "Party", visualType = "emoji", visualValue = "🎉", outlineColor = "#9C27B0"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Education", subCategory = "School", visualType = "emoji", visualValue = "🏫", outlineColor = "#FF9800"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Education", subCategory = "Self-development", visualType = "emoji", visualValue = "📚", outlineColor = "#FF9800"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Healthcare", subCategory = "Hospital", visualType = "emoji", visualValue = "🏥", outlineColor = "#F44336"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Healthcare", subCategory = "Medicine", visualType = "emoji", visualValue = "💊", outlineColor = "#F44336"),

            CategoryEntity(userEmail = userEmail, mainCategory = "Car", subCategory = "Fuel", visualType = "emoji", visualValue = "⛽", outlineColor = "#607D8B"),
            CategoryEntity(userEmail = userEmail, mainCategory = "Car", subCategory = "Parking", visualType = "emoji", visualValue = "🅿️", outlineColor = "#607D8B")
        )

        categoryDao.insertCategories(defaultCategories)
    }
}