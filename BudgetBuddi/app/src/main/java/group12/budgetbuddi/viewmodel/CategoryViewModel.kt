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
    val expenseCategories: StateFlow<List<CategoryEntity>>
    val incomeCategories: StateFlow<List<CategoryEntity>>

    init {
        Log.d(TAG, "CategoryViewModel created")
        Log.d(TAG, "Current Firebase userId = $currentUserId")

        if (currentUserId == null) {
            Log.e(TAG, "No logged-in Firebase user found")

            allCategories = flowOf(emptyList<CategoryEntity>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            filteredCategories = flowOf(emptyList<CategoryEntity>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            mainCategories = flowOf(emptyList<String>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            expenseCategories = flowOf(emptyList<CategoryEntity>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            incomeCategories = flowOf(emptyList<CategoryEntity>())
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
                .onEach { categories ->
                    Log.d(TAG, "allCategories emitted ${categories.size} categories")
                    categories.forEach {
                        Log.d(
                            TAG,
                            "Category emitted: id=${it.id}, type=${it.categoryType}, main=${it.mainCategory}, sub=${it.subCategory}"
                        )
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )

            filteredCategories = combine(
                allCategories,
                _searchQuery
            ) { categories, query ->
                if (query.isBlank()) {
                    categories
                } else {
                    categories.filter {
                        it.mainCategory.contains(query, ignoreCase = true) ||
                                it.subCategory.contains(query, ignoreCase = true) ||
                                it.categoryType.contains(query, ignoreCase = true)
                    }
                }
            }
                .onEach { categories ->
                    Log.d(TAG, "filteredCategories emitted ${categories.size} categories")
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )

            mainCategories = allCategories
                .map { categories ->
                    categories
                        .map { it.mainCategory }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                }
                .onEach { mainCategories ->
                    Log.d(TAG, "mainCategories emitted ${mainCategories.size}: $mainCategories")
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )

            expenseCategories = allCategories
                .map { categories ->
                    categories.filter { it.categoryType == "expense" }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )

            incomeCategories = allCategories
                .map { categories ->
                    categories.filter { it.categoryType == "income" }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCategory(
        categoryType: String,
        mainCategory: String,
        subCategory: String,
        visualType: String,
        visualValue: String,
        outlineColor: String
    ) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot add category because currentUserId is null")
            return
        }

        if (mainCategory.isBlank() || subCategory.isBlank()) {
            _uiMessage.value = "Category and subcategory cannot be empty"
            return
        }

        val cleanCategoryType = when (categoryType.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        val newCategory = CategoryEntity(
            userId = userId,
            categoryType = cleanCategoryType,
            mainCategory = mainCategory.trim(),
            subCategory = subCategory.trim(),
            visualType = visualType,
            visualValue = visualValue,
            outlineColor = outlineColor
        )

        viewModelScope.launch {
            try {
                repository.insertCategory(userId, newCategory)
                _uiMessage.value = "Category added successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add category: ${e.message}", e)
                _uiMessage.value = "Failed to add category: ${e.message}"
            }
        }
    }

    fun updateCategory(
        category: CategoryEntity
    ) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot update category because currentUserId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategory(userId, category)
                _uiMessage.value = "Category updated successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update category: ${e.message}", e)
                _uiMessage.value = "Failed to update category: ${e.message}"
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
            Log.e(TAG, "Cannot rename category because currentUserId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategoryAndMaybeRenameMainCategory(
                    userId = userId,
                    oldMainCategory = oldMainCategory,
                    newMainCategory = newMainCategory,
                    selectedCategory = selectedCategory
                )
                _uiMessage.value = "Category updated successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update category: ${e.message}", e)
                _uiMessage.value = "Failed to update category: ${e.message}"
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot delete category because currentUserId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteCategory(userId, category)
                _uiMessage.value = "Category deleted successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete category: ${e.message}", e)
                _uiMessage.value = "Failed to delete category: ${e.message}"
            }
        }
    }

    fun deleteWholeMainCategory(
        mainCategory: String,
        categoryType: String = "expense"
    ) {
        val userId = currentUserId

        if (userId == null) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot delete main category because currentUserId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteWholeMainCategory(
                    userId = userId,
                    mainCategory = mainCategory,
                    categoryType = categoryType
                )
                _uiMessage.value = "Category deleted successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete main category: ${e.message}", e)
                _uiMessage.value = "Failed to delete category: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}