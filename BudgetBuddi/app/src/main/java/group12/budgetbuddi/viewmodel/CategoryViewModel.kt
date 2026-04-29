package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.repository.CategoryRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/*
Author: Android Developers
Description: Used as reference for using ViewModel to store UI-related data outside composable functions.
Reference: Android Developers. 2019. ViewModel Overview  |  Android Developers. [Online]. Android Developers. Available at: https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 29 April 2026].
*/

/*
Author: Android Developers
Description: Used as reference for collecting Flow data as Compose state and keeping the UI updated from observable state.
Reference: Android Developers. 2024. State and Jetpack Compose. [Online]. Available at: https://developer.android.com/develop/ui/compose/state [Accessed 29 April 2026].
*/

/*
Author: Kotlin Documentation
Description: Used as reference for Flow, StateFlow, combine, and stateIn when managing reactive data streams.
Reference: Kotlin Help. 2022. Kotlin Help. [Online]. Available at: https://kotlinlang.org/docs/flow.html [Accessed 29 April 2026].
*/

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryRepository
    private val sessionManager = SessionManager(application)

    // If no saved email is found, categories are stored under a guest account.
    private val currentUserEmail: String =
        sessionManager.getUserEmail() ?: "guest"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    val allCategories: StateFlow<List<CategoryEntity>>
    val filteredCategories: StateFlow<List<CategoryEntity>>
    val mainCategories: StateFlow<List<String>>

    init {
        val dao = AppDatabase.getInstance(application).categoryDao()
        repository = CategoryRepository(dao)

        // Default categories are inserted once when the user has no categories yet.
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfNeeded(currentUserEmail)
        }

        allCategories = repository.getAllCategories(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        mainCategories = repository.getMainCategories(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // The displayed list changes whenever either the database list or search query changes.
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
        if (mainCategory.isBlank() || subCategory.isBlank()) {
            _uiMessage.value = "Category and subcategory cannot be empty"
            return
        }

        val newCategory = CategoryEntity(
            userEmail = currentUserEmail,
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

    fun deleteSubCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _uiMessage.value = "Subcategory deleted"
        }
    }

    fun deleteWholeCategory(mainCategory: String) {
        viewModelScope.launch {
            repository.deleteWholeCategory(currentUserEmail, mainCategory)
            _uiMessage.value = "Category deleted"
        }
    }

    fun updateCategory(updatedCategory: CategoryEntity) {
        if (updatedCategory.mainCategory.isBlank() || updatedCategory.subCategory.isBlank()) {
            _uiMessage.value = "Fields cannot be empty"
            return
        }

        viewModelScope.launch {
            repository.updateCategory(updatedCategory.copy(userEmail = currentUserEmail))
            _uiMessage.value = "Category updated"
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}