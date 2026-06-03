package group12.budgetbuddi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.repository.CategoryRepository
import group12.budgetbuddi.utils.AchievementManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CategoryViewModel"
    }

    private val repository = CategoryRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val userIdFlow: StateFlow<String?> =
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val uid = firebaseAuth.currentUser?.uid
                Log.d(TAG, "Auth state changed, userId: $uid")
                trySend(uid)
            }

            auth.addAuthStateListener(listener)
            val currentUid = auth.currentUser?.uid
            Log.d(TAG, "Initial userId: $currentUid")
            trySend(currentUid)

            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = auth.currentUser?.uid
            )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>> =
        userIdFlow
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    flow {
                        val cached = repository.getCachedCategories(userId)
                        Log.d(TAG, "Cached categories for userId $userId: ${cached.size}")

                        if (cached.isNotEmpty()) {
                            emit(cached)
                        }

                        viewModelScope.launch {
                            try {
                                repository.seedDefaultCategoriesIfNeeded(userId)
                                Log.d(TAG, "Default categories seeded for userId: $userId")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to seed default categories: ${e.message}", e)
                                _uiMessage.value = "Failed to load default categories: ${e.message}"
                            }
                        }

                        emitAll(repository.observeAllCategories(userId))
                    }
                }
            }
            .map { categories ->
                categories
                    .distinctBy {
                        "${it.categoryType.lowercase()}|${it.mainCategory.lowercase()}|${it.subCategory.lowercase()}"
                    }
                    .sortedWith(
                        compareBy<CategoryEntity> { it.categoryType.lowercase() }
                            .thenBy { it.mainCategory.lowercase() }
                            .thenBy { it.subCategory.lowercase() }
                    )
            }
            .catch { e ->
                Log.e(TAG, "Failed to observe categories: ${e.message}", e)
                _uiMessage.value = "Failed to load categories: ${e.message}"
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = auth.currentUser?.uid
                    ?.let { repository.getCachedCategories(it) }
                    .orEmpty()
            )

    val filteredCategories: StateFlow<List<CategoryEntity>> =
        combine(allCategories, _searchQuery) { categories, query ->
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = allCategories.value
            )

    val mainCategories: StateFlow<List<String>> =
        allCategories
            .map { categories ->
                categories
                    .map { it.mainCategory }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = allCategories.value
                    .map { it.mainCategory }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            )

    val expenseCategories: StateFlow<List<CategoryEntity>> =
        allCategories
            .map { categories ->
                categories.filter { it.categoryType == "expense" }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = allCategories.value.filter { it.categoryType == "expense" }
            )

    val incomeCategories: StateFlow<List<CategoryEntity>> =
        allCategories
            .map { categories ->
                categories.filter { it.categoryType == "income" }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = allCategories.value.filter { it.categoryType == "income" }
            )

    init {
        Log.d(TAG, "CategoryViewModel initialized")

        viewModelScope.launch {
            delay(500)
            Log.d(TAG, "AchievementManager initialized: ${AchievementManager.isInitialized()}")
        }
    }

    private fun currentUserId(): String? {
        return userIdFlow.value
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
        val category = CategoryEntity(
            categoryType = when (categoryType.trim().lowercase()) {
                "income" -> "income"
                else -> "expense"
            },
            mainCategory = mainCategory.trim(),
            subCategory = subCategory.trim(),
            visualType = visualType,
            visualValue = visualValue,
            outlineColor = outlineColor
        )

        insertCategory(category)
    }

    fun insertCategory(category: CategoryEntity) {
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot insert category because userId is null")
            return
        }

        Log.d(TAG, "insertCategory called for user: $userId, category: ${category.mainCategory}/${category.subCategory}")

        viewModelScope.launch {
            _isSaving.value = true
            try {
                Log.d(TAG, "Starting category insertion for: ${category.mainCategory}/${category.subCategory}")

                repository.insertCategory(
                    userId = userId,
                    category = category.copy(userId = userId)
                )

                Log.d(TAG, "Category insertion completed successfully")
                _uiMessage.value = "Category added successfully"


                Log.d(TAG, "AchievementManager.isInitialized() = ${AchievementManager.isInitialized()}")

                if (AchievementManager.isInitialized()) {
                    Log.d(TAG, "Calling onCategoryCreated for category: ${category.mainCategory}/${category.subCategory}")
                    AchievementManager.userProfileViewModel.onCategoryCreated()
                    Log.d(TAG, "Achievement: onCategoryCreated called via Singleton")

                    // Add delay to ensure achievement check completes
                    delay(1000)
                    Log.d(TAG, "Achievement check should have completed for category creation")
                } else {
                    Log.e(TAG, "AchievementManager not initialized! Cannot track category achievement.")
                    Log.e(TAG, "Make sure MainActivity initializes AchievementManager after user logs in.")
                }


            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert category: ${e.message}", e)
                _uiMessage.value = "Failed to add category: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateCategory(category: CategoryEntity) {
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot update category because userId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategory(
                    userId = userId,
                    category = category.copy(userId = userId)
                )
                _uiMessage.value = "Category updated successfully"
                Log.d(TAG, "Category updated: ${category.mainCategory}/${category.subCategory}")
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
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot rename category because userId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCategoryAndMaybeRenameMainCategory(
                    userId = userId,
                    oldMainCategory = oldMainCategory,
                    newMainCategory = newMainCategory,
                    selectedCategory = selectedCategory?.copy(userId = userId)
                )
                _uiMessage.value = "Category updated successfully"
                Log.d(TAG, "Category renamed: $oldMainCategory -> $newMainCategory")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update category: ${e.message}", e)
                _uiMessage.value = "Failed to update category: ${e.message}"
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot delete category because userId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteCategory(userId, category)
                _uiMessage.value = "Category deleted successfully"
                Log.d(TAG, "Category deleted: ${category.mainCategory}/${category.subCategory}")
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
        val userId = currentUserId()

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot delete main category because userId is null")
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
                Log.d(TAG, "Whole main category deleted: $mainCategory ($categoryType)")
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