package group12.budgetbuddi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/*
Author: Android Developers / Firebase
Description: Used as reference for ViewModel, StateFlow, and observing Firestore data reactively in a Compose app.
References:
- Android Developers. 2019. ViewModel Overview. [Online].
Available at: https://developer.android.com/topic/libraries/architecture/viewmodel
- Firebase. 2026. Listen to realtime updates with Cloud Firestore. [Online].
Available at: https://firebase.google.com/docs/firestore/query-data/listen
Accessed: 19 May 2026.
*/

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CategoryViewModel"
    }

    private val repository = CategoryRepository()

    private val currentUserId: String? = repository.getCurrentUserId()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>>
    val filteredCategories: StateFlow<List<CategoryEntity>>
    val mainCategories: StateFlow<List<String>>

    init {
        if (currentUserId == null) {
            Log.e(TAG, "No logged-in Firebase user found")

            allCategories = flowOf(emptyList<CategoryEntity>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            mainCategories = flowOf(emptyList<String>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            filteredCategories = flowOf(emptyList<CategoryEntity>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            _uiMessage.value = "No logged-in user found"
        } else {
            viewModelScope.launch {
                try {
                    repository.seedDefaultCategoriesIfNeeded(currentUserId)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to seed default categories: ${e.message}", e)
                    _uiMessage.value = "Failed to load default categories"
                }
            }

            allCategories = repository.observeAllCategories(currentUserId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            mainCategories = allCategories
                .map { categories ->
                    categories
                        .map { it.mainCategory }
                        .distinct()
                        .sorted()
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            filteredCategories = combine(
                allCategories,
                _searchQuery
            ) { categories, query ->
                if (query.isBlank()) {
                    categories
                } else {
                    categories.filter {
                        it.mainCategory.contains(query, ignoreCase = true) ||
                                it.subCategory.contains(query, ignoreCase = true)
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCategory(
        mainCategory: String,
        subCategory: String,
        visualType: String,
        visualValue: String,
        outlineColor: String
    ) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            return
        }

        if (mainCategory.isBlank() || subCategory.isBlank()) {
            _uiMessage.value = "Category and subcategory cannot be empty"
            return
        }

        val newCategory = CategoryEntity(
            userId = userId,
            mainCategory = mainCategory.trim(),
            subCategory = subCategory.trim(),
            visualType = visualType,
            visualValue = visualValue,
            outlineColor = outlineColor
        )

        viewModelScope.launch {
            try {
                repository.insertCategory(userId, newCategory)
                _uiMessage.value = "Category added"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add category: ${e.message}", e)
                _uiMessage.value = "Failed to add category"
            }
        }
    }

    fun updateCategory(updatedCategory: CategoryEntity) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            return
        }

        if (updatedCategory.mainCategory.isBlank() || updatedCategory.subCategory.isBlank()) {
            _uiMessage.value = "Fields cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategory(
                    userId,
                    updatedCategory.copy(
                        userId = userId,
                        mainCategory = updatedCategory.mainCategory.trim(),
                        subCategory = updatedCategory.subCategory.trim()
                    )
                )
                _uiMessage.value = "Category updated"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update category: ${e.message}", e)
                _uiMessage.value = "Failed to update category"
            }
        }
    }

    fun updateCategoryAndMaybeRenameMainCategory(
        oldMainCategory: String,
        newMainCategory: String,
        selectedCategory: CategoryEntity?
    ) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            return
        }

        if (oldMainCategory.isBlank() || newMainCategory.isBlank()) {
            _uiMessage.value = "Main category name cannot be empty"
            return
        }

        if (selectedCategory != null && selectedCategory.subCategory.isBlank()) {
            _uiMessage.value = "Subcategory cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategoryAndMaybeRenameMainCategory(
                    userId = userId,
                    oldMainCategory = oldMainCategory,
                    newMainCategory = newMainCategory,
                    selectedCategory = selectedCategory?.copy(
                        userId = userId,
                        mainCategory = newMainCategory.trim(),
                        subCategory = selectedCategory.subCategory.trim()
                    )
                )

                _uiMessage.value = "Category updated"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update category: ${e.message}", e)
                _uiMessage.value = "Failed to update category"
            }
        }
    }

    fun deleteSubCategory(category: CategoryEntity) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteCategory(userId, category)
                _uiMessage.value = "Subcategory deleted"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete subcategory: ${e.message}", e)
                _uiMessage.value = "Failed to delete subcategory"
            }
        }
    }

    fun deleteWholeCategory(mainCategory: String) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            return
        }

        if (mainCategory.isBlank()) {
            _uiMessage.value = "Please select a category"
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteWholeMainCategory(userId, mainCategory)
                _uiMessage.value = "Category deleted"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete category: ${e.message}", e)
                _uiMessage.value = "Failed to delete category"
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}