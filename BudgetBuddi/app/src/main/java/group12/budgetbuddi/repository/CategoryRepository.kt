package group12.budgetbuddi.repository

import group12.budgetbuddi.data.dao.CategoryDao
import group12.budgetbuddi.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    // Read
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    fun getMainCategories(): Flow<List<String>> {
        return categoryDao.getMainCategories()
    }

    fun searchCategories(query: String): Flow<List<CategoryEntity>> {
        return categoryDao.searchCategories(query)
    }

    fun getSubCategories(mainCategory: String): Flow<List<CategoryEntity>> {
        return categoryDao.getSubCategoriesForMainCategory(mainCategory)
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

    suspend fun deleteWholeCategory(mainCategory: String) {
        categoryDao.deleteWholeMainCategory(mainCategory)
    }

    // Utility
    suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }

    suspend fun seedDefaultCategoriesIfNeeded() {
        if (categoryDao.getCategoryCount() > 0) return

        val defaultCategories = listOf(

            // Food & Drink
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Breakfast", visualType = "emoji", visualValue = "🍳", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Lunch", visualType = "emoji", visualValue = "🥪", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Dinner", visualType = "emoji", visualValue = "🍽️", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Snacks", visualType = "emoji", visualValue = "🍪", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Restaurant", visualType = "emoji", visualValue = "🍴", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Eating Out", visualType = "emoji", visualValue = "🛍️", outlineColor = "#4CAF50"),
            CategoryEntity(mainCategory = "Food & Drink", subCategory = "Drinks", visualType = "emoji", visualValue = "🥤", outlineColor = "#4CAF50"),

            // Transport
            CategoryEntity(mainCategory = "Transport", subCategory = "Bus", visualType = "emoji", visualValue = "🚌", outlineColor = "#2196F3"),
            CategoryEntity(mainCategory = "Transport", subCategory = "Subway", visualType = "emoji", visualValue = "🚇", outlineColor = "#2196F3"),
            CategoryEntity(mainCategory = "Transport", subCategory = "Taxi", visualType = "emoji", visualValue = "🚕", outlineColor = "#2196F3"),
            CategoryEntity(mainCategory = "Transport", subCategory = "Train", visualType = "emoji", visualValue = "🚆", outlineColor = "#2196F3"),

            // Housing
            CategoryEntity(mainCategory = "Housing", subCategory = "Groceries", visualType = "emoji", visualValue = "🛒", outlineColor = "#795548"),
            CategoryEntity(mainCategory = "Housing", subCategory = "Rent", visualType = "emoji", visualValue = "🏠", outlineColor = "#795548"),
            CategoryEntity(mainCategory = "Housing", subCategory = "Water", visualType = "emoji", visualValue = "💧", outlineColor = "#795548"),
            CategoryEntity(mainCategory = "Housing", subCategory = "Electricity", visualType = "emoji", visualValue = "⚡", outlineColor = "#795548"),

            // Entertainment
            CategoryEntity(mainCategory = "Entertainment", subCategory = "Movie", visualType = "emoji", visualValue = "🎬", outlineColor = "#9C27B0"),
            CategoryEntity(mainCategory = "Entertainment", subCategory = "Games", visualType = "emoji", visualValue = "🎮", outlineColor = "#9C27B0"),
            CategoryEntity(mainCategory = "Entertainment", subCategory = "Music", visualType = "emoji", visualValue = "🎵", outlineColor = "#9C27B0"),
            CategoryEntity(mainCategory = "Entertainment", subCategory = "Party", visualType = "emoji", visualValue = "🎉", outlineColor = "#9C27B0"),

            // Education
            CategoryEntity(mainCategory = "Education", subCategory = "School", visualType = "emoji", visualValue = "🏫", outlineColor = "#FF9800"),
            CategoryEntity(mainCategory = "Education", subCategory = "Self-development", visualType = "emoji", visualValue = "📚", outlineColor = "#FF9800"),

            // Healthcare
            CategoryEntity(mainCategory = "Healthcare", subCategory = "Hospital", visualType = "emoji", visualValue = "🏥", outlineColor = "#F44336"),
            CategoryEntity(mainCategory = "Healthcare", subCategory = "Medicine", visualType = "emoji", visualValue = "💊", outlineColor = "#F44336"),

            // Car
            CategoryEntity(mainCategory = "Car", subCategory = "Fuel", visualType = "emoji", visualValue = "⛽", outlineColor = "#607D8B"),
            CategoryEntity(mainCategory = "Car", subCategory = "Parking", visualType = "emoji", visualValue = "🅿️", outlineColor = "#607D8B")
        )

        categoryDao.insertCategories(defaultCategories)
    }
}