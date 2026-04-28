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
}