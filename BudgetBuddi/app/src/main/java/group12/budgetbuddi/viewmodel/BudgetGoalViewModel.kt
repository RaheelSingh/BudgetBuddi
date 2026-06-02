package group12.budgetbuddi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.repository.CategoryRepository
import group12.budgetbuddi.repository.ExpenseRepository
import group12.budgetbuddi.utils.AchievementManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class GoalWithProgress(
    val goal: BudgetGoalEntity,
    val amountSpent: Double,
    val progressFraction: Float
)

data class BudgetGoalsListUiState(
    val expenseGoals: List<GoalWithProgress> = emptyList(),
    val incomeGoals: List<GoalWithProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddGoalUiState(
    val selectedTab: Int = 0,
    val selectedCategory: CategoryEntity? = null,
    val minGoal: String = "",
    val maxGoal: String = "",
    val isRecurring: Boolean = false,
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val incomeCategories: List<CategoryEntity> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val editingGoalId: String? = null,
    val isEditMode: Boolean = false
)

data class DeleteDialogState(
    val isVisible: Boolean = false,
    val goalId: String = "",
    val goalName: String = ""
)

class BudgetGoalViewModel : ViewModel() {

    companion object {
        private const val TAG = "BudgetGoalVM"

        private const val ENTIRE_EXPENSE_ID = "__entire_expense__"
        private const val ENTIRE_INCOME_ID = "__entire_income__"
    }

    private val auth = FirebaseAuth.getInstance()
    private val budgetGoalRepository = BudgetGoalRepository()
    private val categoryRepository = CategoryRepository()
    private val expenseRepository = ExpenseRepository()

    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    private val userIdFlow: StateFlow<String?> =
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.uid)
            }

            auth.addAuthStateListener(listener)
            trySend(auth.currentUser?.uid)

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

    private val _addGoalUiState = MutableStateFlow(AddGoalUiState())
    val addGoalUiState: StateFlow<AddGoalUiState> = _addGoalUiState.asStateFlow()

    private val _deleteDialogState = MutableStateFlow(DeleteDialogState())
    val deleteDialogState: StateFlow<DeleteDialogState> = _deleteDialogState.asStateFlow()

    val listUiState: StateFlow<BudgetGoalsListUiState> =
        userIdFlow
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(
                        BudgetGoalsListUiState(
                            expenseGoals = emptyList(),
                            incomeGoals = emptyList(),
                            isLoading = false,
                            error = "No logged-in user found"
                        )
                    )
                } else {
                    observeGoalsWithProgress(userId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = BudgetGoalsListUiState(isLoading = true)
            )

    init {
        observeCategories()
    }

    private fun observeGoalsWithProgress(userId: String): Flow<BudgetGoalsListUiState> {
        val firstDay = "%04d-%02d-01".format(currentYear, currentMonth)

        val cal = Calendar.getInstance()
        val lastDay = "%04d-%02d-%02d".format(
            currentYear,
            currentMonth,
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        )

        return combine(
            budgetGoalRepository.getAllGoalsForMonth(userId, currentMonth, currentYear),
            expenseRepository.getExpensesByDateRange(userId, firstDay, lastDay)
        ) { goals, transactions ->

            val expenseGoals = goals
                .filter { it.goalType == "expense" }
                .map { goal ->
                    val amount = calculateGoalAmount(goal, transactions)

                    val fraction = if (goal.maxGoal > 0.0) {
                        (amount / goal.maxGoal).toFloat()
                    } else {
                        0f
                    }

                    GoalWithProgress(
                        goal = goal,
                        amountSpent = amount,
                        progressFraction = fraction.coerceAtLeast(0f)
                    )
                }
                .sortedBy { it.goal.goalName.lowercase() }

            val incomeGoals = goals
                .filter { it.goalType == "income" }
                .map { goal ->
                    val amount = calculateGoalAmount(goal, transactions)

                    val fraction = if (goal.minGoal > 0.0) {
                        (amount / goal.minGoal).toFloat()
                    } else {
                        0f
                    }

                    GoalWithProgress(
                        goal = goal,
                        amountSpent = amount,
                        progressFraction = fraction.coerceAtLeast(0f)
                    )
                }
                .sortedBy { it.goal.goalName.lowercase() }

            BudgetGoalsListUiState(
                expenseGoals = expenseGoals,
                incomeGoals = incomeGoals,
                isLoading = false,
                error = null
            )
        }
            .onStart {
                emit(BudgetGoalsListUiState(isLoading = true))
            }
            .catch { e ->
                Log.e(TAG, "Error loading budget goals: ${e.message}", e)
                emit(
                    BudgetGoalsListUiState(
                        isLoading = false,
                        error = e.message
                    )
                )
            }
    }

    private fun observeCategories() {
        userIdFlow
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    flow {
                        try {
                            categoryRepository.seedDefaultCategoriesIfNeeded(userId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to seed categories: ${e.message}", e)
                        }

                        emitAll(categoryRepository.observeAllCategories(userId))
                    }
                }
            }
            .map { categories ->
                categories.distinctBy {
                    "${it.categoryType.lowercase()}|${it.mainCategory.lowercase()}|${it.subCategory.lowercase()}"
                }
            }
            .onEach { categories ->
                val expenseCategories = buildBudgetGoalCategoryOptions(
                    userId = userIdFlow.value.orEmpty(),
                    categoryType = "expense",
                    categories = categories.filter { it.categoryType == "expense" }
                )

                val incomeCategories = buildBudgetGoalCategoryOptions(
                    userId = userIdFlow.value.orEmpty(),
                    categoryType = "income",
                    categories = categories.filter { it.categoryType == "income" }
                )

                _addGoalUiState.update { state ->
                    state.copy(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories
                    )
                }
            }
            .catch { e ->
                Log.e(TAG, "Error loading categories for budget goals: ${e.message}", e)
                _addGoalUiState.update {
                    it.copy(error = "Failed to load categories: ${e.message}")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun buildBudgetGoalCategoryOptions(
        userId: String,
        categoryType: String,
        categories: List<CategoryEntity>
    ): List<CategoryEntity> {
        val entireOption = if (categoryType == "income") {
            CategoryEntity(
                id = ENTIRE_INCOME_ID,
                userId = userId,
                categoryType = "income",
                mainCategory = "Entire Income",
                subCategory = "",
                visualType = "emoji",
                visualValue = "💰",
                outlineColor = "#139A43"
            )
        } else {
            CategoryEntity(
                id = ENTIRE_EXPENSE_ID,
                userId = userId,
                categoryType = "expense",
                mainCategory = "Entire Expense",
                subCategory = "",
                visualType = "emoji",
                visualValue = "💸",
                outlineColor = "#139A43"
            )
        }

        val mainCategoryOptions = categories
            .groupBy { it.mainCategory }
            .mapNotNull { (mainCategory, items) ->
                val first = items.firstOrNull() ?: return@mapNotNull null

                CategoryEntity(
                    id = "__main_${categoryType}_${mainCategory.lowercase().replace(" ", "_")}__",
                    userId = userId,
                    categoryType = categoryType,
                    mainCategory = mainCategory,
                    subCategory = "",
                    visualType = first.visualType,
                    visualValue = first.visualValue,
                    outlineColor = first.outlineColor
                )
            }
            .sortedBy { it.mainCategory.lowercase() }

        val subCategoryOptions = categories.sortedWith(
            compareBy<CategoryEntity> { it.mainCategory.lowercase() }
                .thenBy { it.subCategory.lowercase() }
        )

        return listOf(entireOption) + mainCategoryOptions + subCategoryOptions
    }

    private fun calculateGoalAmount(
        goal: BudgetGoalEntity,
        transactions: List<ExpenseEntity>
    ): Double {
        val cleanGoalType = goal.goalType.trim().lowercase()
        val cleanCategory = goal.categoryName.trim()
        val cleanSubCategory = goal.subCategoryName.trim()

        return transactions
            .filter { it.type.trim().lowercase() == cleanGoalType }
            .filter { transaction ->
                when {
                    cleanCategory == "Entire Expense" && cleanGoalType == "expense" -> true
                    cleanCategory == "Entire Income" && cleanGoalType == "income" -> true

                    cleanSubCategory.isBlank() -> {
                        transaction.category == cleanCategory
                    }

                    else -> {
                        transaction.category == cleanCategory &&
                                transaction.subCategory == cleanSubCategory
                    }
                }
            }
            .sumOf { it.amount }
    }

    fun selectTab(index: Int) {
        _addGoalUiState.update {
            it.copy(
                selectedTab = index,
                selectedCategory = null,
                minGoal = "",
                maxGoal = "",
                error = null
            )
        }
    }

    fun selectCategory(category: CategoryEntity) {
        _addGoalUiState.update {
            it.copy(
                selectedCategory = category,
                error = null
            )
        }
    }

    fun updateMinGoal(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }

        if (filtered.count { it == '.' } <= 1) {
            _addGoalUiState.update {
                it.copy(
                    minGoal = filtered,
                    error = null
                )
            }
        }
    }

    fun updateMaxGoal(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }

        if (filtered.count { it == '.' } <= 1) {
            _addGoalUiState.update {
                it.copy(
                    maxGoal = filtered,
                    error = null
                )
            }
        }
    }

    fun toggleRecurring() {
        _addGoalUiState.update {
            it.copy(isRecurring = !it.isRecurring)
        }
    }

    fun resetForm() {
        _addGoalUiState.update {
            it.copy(
                selectedTab = 0,
                selectedCategory = null,
                minGoal = "",
                maxGoal = "",
                isRecurring = false,
                isSaving = false,
                isSaved = false,
                error = null,
                editingGoalId = null,
                isEditMode = false
            )
        }
    }

    fun resetSavedFlag() {
        _addGoalUiState.update {
            it.copy(isSaved = false)
        }
    }

    fun startEditing(goalWithProgress: GoalWithProgress) {
        val goal = goalWithProgress.goal
        val tabIndex = if (goal.goalType == "income") 1 else 0

        val categoryOptions = if (tabIndex == 1) {
            _addGoalUiState.value.incomeCategories
        } else {
            _addGoalUiState.value.expenseCategories
        }

        val matchingCategory = categoryOptions.firstOrNull {
            it.categoryType == goal.goalType &&
                    it.mainCategory == goal.categoryName &&
                    it.subCategory == goal.subCategoryName
        } ?: CategoryEntity(
            id = goal.id,
            userId = goal.userId,
            categoryType = goal.goalType,
            mainCategory = goal.categoryName,
            subCategory = goal.subCategoryName,
            visualType = "emoji",
            visualValue = if (goal.goalType == "income") "💰" else "💸",
            outlineColor = "#139A43"
        )

        _addGoalUiState.update {
            it.copy(
                selectedTab = tabIndex,
                selectedCategory = matchingCategory,
                minGoal = goal.minGoal.toString(),
                maxGoal = if (goal.maxGoal > 0.0) goal.maxGoal.toString() else "",
                isRecurring = goal.isRecurring,
                editingGoalId = goal.id,
                isEditMode = true,
                isSaving = false,
                isSaved = false,
                error = null
            )
        }
    }

    fun saveGoal() {
        val userId = userIdFlow.value
        val state = _addGoalUiState.value
        val category = state.selectedCategory

        if (userId.isNullOrBlank()) {
            _addGoalUiState.update {
                it.copy(error = "No logged-in user found")
            }
            return
        }

        if (category == null) {
            _addGoalUiState.update {
                it.copy(error = "Please select a category")
            }
            return
        }

        val goalType = if (state.selectedTab == 1) "income" else "expense"
        val min = state.minGoal.toDoubleOrNull()
        val max = state.maxGoal.toDoubleOrNull()

        if (min == null || min < 0.0) {
            _addGoalUiState.update {
                it.copy(error = "Enter a valid minimum amount")
            }
            return
        }

        if (goalType == "expense") {
            if (max == null || max <= 0.0) {
                _addGoalUiState.update {
                    it.copy(error = "Enter a valid maximum spending amount")
                }
                return
            }

            if (min > max) {
                _addGoalUiState.update {
                    it.copy(error = "Minimum cannot be greater than maximum")
                }
                return
            }
        }

        viewModelScope.launch {
            _addGoalUiState.update {
                it.copy(isSaving = true, error = null)
            }

            try {
                val cleanCategoryName = category.mainCategory.trim()
                val cleanSubCategoryName = category.subCategory.trim()

                val goalName = if (cleanSubCategoryName.isBlank()) {
                    cleanCategoryName
                } else {
                    "$cleanCategoryName - $cleanSubCategoryName"
                }

                val goal = BudgetGoalEntity(
                    id = state.editingGoalId ?: "",
                    userId = userId,
                    goalName = goalName,
                    categoryName = cleanCategoryName,
                    subCategoryName = cleanSubCategoryName,
                    goalType = goalType,
                    minGoal = min,
                    maxGoal = if (goalType == "expense") max ?: 0.0 else 0.0,
                    month = currentMonth,
                    year = currentYear,
                    isRecurring = state.isRecurring
                )

                if (state.isEditMode && !state.editingGoalId.isNullOrBlank()) {
                    budgetGoalRepository.updateGoal(userId, goal)
                } else {
                    budgetGoalRepository.saveGoal(userId, goal)
                }

                _addGoalUiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        error = null
                    )
                }

                // ===== ACHIEVEMENT TRACKING USING SINGLETON =====
                if (AchievementManager.isInitialized()) {
                    AchievementManager.userProfileViewModel.onBudgetGoalCreated()
                    Log.d(TAG, "Achievement: onBudgetGoalCreated called via Singleton")
                } else {
                    Log.e(TAG, "AchievementManager not initialized! Cannot track budget goal achievement.")
                }
                // ===== END ACHIEVEMENT TRACKING =====

            } catch (e: Exception) {
                Log.e(TAG, "Error saving budget goal: ${e.message}", e)

                _addGoalUiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save: ${e.message}"
                    )
                }
            }
        }
    }

    fun showDeleteDialog(goalId: String, goalName: String) {
        _deleteDialogState.update {
            it.copy(
                isVisible = true,
                goalId = goalId,
                goalName = goalName
            )
        }
    }

    fun dismissDeleteDialog() {
        _deleteDialogState.update {
            it.copy(
                isVisible = false,
                goalId = "",
                goalName = ""
            )
        }
    }

    fun confirmDelete() {
        val userId = userIdFlow.value
        val goalId = _deleteDialogState.value.goalId

        if (userId.isNullOrBlank()) {
            Log.e(TAG, "Cannot delete goal because userId is blank")
            dismissDeleteDialog()
            return
        }

        if (goalId.isBlank()) {
            dismissDeleteDialog()
            return
        }

        viewModelScope.launch {
            try {
                budgetGoalRepository.deleteGoal(userId, goalId)
                dismissDeleteDialog()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting goal: ${e.message}", e)
                dismissDeleteDialog()
            }
        }
    }
}