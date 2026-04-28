package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryRepository

    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    // Data flows
    val allCategories: StateFlow<List<CategoryEntity>>

    val filteredCategories: StateFlow<List<CategoryEntity>>

    val mainCategories: StateFlow<List<String>>

    // Init
    init {
        val dao = AppDatabase.getInstance(application).categoryDao()
        repository = CategoryRepository(dao)

        // Seed default categories
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfNeeded()
        }

        // All categories
        allCategories = repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Main categories for dropdowns
        mainCategories = repository.getMainCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Filtered categories (search)
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

    // Search
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Add category
    fun addCategory(
        mainCategory: String,
        subCategory: String,
        visualType: String,
        visualValue: String,
        outlineColor: String
    ) {
        if (mainCategory.isBlank() || subCategory.isBlank()) {
            _uiMessage.value = "Category and subcategory cannot be empty"
            return
        }

        val newCategory = CategoryEntity(
            mainCategory = mainCategory.trim(),
            subCategory = subCategory.trim(),
            visualType = visualType,
            visualValue = visualValue,
            outlineColor = outlineColor
        )

        viewModelScope.launch {
            repository.insertCategory(newCategory)
            _uiMessage.value = "Category added"
        }
    }

    // Delete
    fun deleteSubCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _uiMessage.value = "Subcategory deleted"
        }
    }

    fun deleteWholeCategory(mainCategory: String) {
        viewModelScope.launch {
            repository.deleteWholeCategory(mainCategory)
            _uiMessage.value = "Category deleted"
        }
    }

    // Update
    fun updateCategory(updatedCategory: CategoryEntity) {
        if (updatedCategory.mainCategory.isBlank() || updatedCategory.subCategory.isBlank()) {
            _uiMessage.value = "Fields cannot be empty"
            return
        }

        viewModelScope.launch {
            repository.updateCategory(updatedCategory)
            _uiMessage.value = "Category updated"
        }
    }

    // Clear message
    fun clearMessage() {
        _uiMessage.value = null
    }
}