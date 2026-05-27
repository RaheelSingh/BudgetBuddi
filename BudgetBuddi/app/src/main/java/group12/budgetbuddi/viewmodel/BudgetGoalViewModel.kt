package group12.budgetbuddi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.repository.CategoryRepository
import group12.budgetbuddi.repository.ExpenseRepository
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
    }

    private val auth = FirebaseAuth.getInstance()
    private val budgetGoalRepository = BudgetGoalRepository()
    private val categoryRepository = CategoryRepository()
    private val expenseRepository = ExpenseRepository()

    private val userId: String?
        get() = auth.currentUser?.uid

    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    private val _listUiState = MutableStateFlow(BudgetGoalsListUiState())
    val listUiState: StateFlow<BudgetGoalsListUiState> = _listUiState.asStateFlow()

    private val _addGoalUiState = MutableStateFlow(AddGoalUiState())
    val addGoalUiState: StateFlow<AddGoalUiState> = _addGoalUiState.asStateFlow()

    private val _deleteDialogState = MutableStateFlow(DeleteDialogState())
    val deleteDialogState: StateFlow<DeleteDialogState> = _deleteDialogState.asStateFlow()

    init {
        loadGoalsWithProgress()
        loadCategories()
    }


    private fun loadGoalsWithProgress() {
        val uid = userId ?: return
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true) }
            try {
                val firstDay = "%04d-%02d-01".format(currentYear, currentMonth)
                val cal = Calendar.getInstance()
                val lastDay = "%04d-%02d-%02d".format(
                    currentYear, currentMonth,
                    cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                combine(
                    budgetGoalRepository.getAllGoalsForMonth(uid, currentMonth, currentYear),
                    expenseRepository.getExpensesByDateRange(uid, firstDay, lastDay)
                ) { goals, expenses -> Pair(goals, expenses) }
                    .collect { (goals, expenses) ->
                        val spendingMap = expenses
                            .groupBy { it.category }
                            .mapValues { (_, list) -> list.sumOf { it.amount } }

                        val expenseGoals = goals
                            .filter { it.goalType == "expense" }
                            .map { goal ->
                                val spent = spendingMap[goal.categoryName] ?: 0.0
                                val fraction = if (goal.maxGoal > 0)
                                    (spent / goal.maxGoal).toFloat() else 0f
                                GoalWithProgress(goal, spent, fraction.coerceAtLeast(0f))
                            }
                            .sortedBy { it.goal.goalName }

                        val incomeGoals = goals
                            .filter { it.goalType == "income" }
                            .map { goal ->
                                val earned = spendingMap[goal.categoryName] ?: 0.0
                                val fraction = if (goal.minGoal > 0)
                                    (earned / goal.minGoal).toFloat() else 0f
                                GoalWithProgress(goal, earned, fraction.coerceAtLeast(0f))
                            }
                            .sortedBy { it.goal.goalName }

                        _listUiState.update {
                            it.copy(
                                expenseGoals = expenseGoals,
                                incomeGoals = incomeGoals,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading goals: ${e.message}", e)
                _listUiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }


    private fun loadCategories() {
        val uid = userId ?: return
        viewModelScope.launch {
            categoryRepository.observeAllCategories(uid).collect { categories ->
                _addGoalUiState.update {
                    it.copy(
                        expenseCategories = categories.filter { c -> c.categoryType == "expense" },
                        incomeCategories = categories.filter { c -> c.categoryType == "income" }
                    )
                }
            }
        }
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
        _addGoalUiState.update { it.copy(selectedCategory = category, error = null) }
    }

    fun updateMinGoal(value: String) {
        _addGoalUiState.update { it.copy(minGoal = value, error = null) }
    }

    fun updateMaxGoal(value: String) {
        _addGoalUiState.update { it.copy(maxGoal = value, error = null) }
    }

    fun toggleRecurring() {
        _addGoalUiState.update { it.copy(isRecurring = !it.isRecurring) }
    }

    fun resetForm() {
        _addGoalUiState.update {
            it.copy(
                selectedTab = 0,
                selectedCategory = null,
                minGoal = "",
                maxGoal = "",
                isRecurring = false,
                isSaved = false,
                error = null,
                editingGoalId = null,
                isEditMode = false
            )
        }
    }

    fun resetSavedFlag() {
        _addGoalUiState.update { it.copy(isSaved = false) }
    }


    fun startEditing(goalWithProgress: GoalWithProgress) {
        val goal = goalWithProgress.goal
        val tabIndex = if (goal.goalType == "income") 1 else 0
        val state = _addGoalUiState.value
        val categories = if (goal.goalType == "income")
            state.incomeCategories else state.expenseCategories
        val matchingCategory = categories.firstOrNull {
            it.mainCategory == goal.categoryName && it.subCategory == goal.subCategoryName
        }
        _addGoalUiState.update {
            it.copy(
                selectedTab = tabIndex,
                selectedCategory = matchingCategory,
                minGoal = if (goal.minGoal > 0) goal.minGoal.toString() else "",
                maxGoal = if (goal.maxGoal > 0) goal.maxGoal.toString() else "",
                isRecurring = goal.isRecurring,
                editingGoalId = goal.id,
                isEditMode = true,
                isSaved = false,
                error = null
            )
        }
    }


    fun saveGoal() {
        val uid = userId
        if (uid.isNullOrBlank()) {
            _addGoalUiState.update { it.copy(error = "No logged-in user") }
            return
        }
        val state = _addGoalUiState.value
        val category = state.selectedCategory
        if (category == null) {
            _addGoalUiState.update { it.copy(error = "Please select a category") }
            return
        }
        val goalType = if (state.selectedTab == 1) "income" else "expense"
        val min = state.minGoal.toDoubleOrNull() ?: 0.0
        val max = state.maxGoal.toDoubleOrNull() ?: 0.0

        if (goalType == "expense") {
            if (max <= 0) {
                _addGoalUiState.update { it.copy(error = "Maximum spending must be greater than 0") }
                return
            }
            if (min < 0) {
                _addGoalUiState.update { it.copy(error = "Minimum cannot be negative") }
                return
            }
            if (min > max) {
                _addGoalUiState.update { it.copy(error = "Minimum cannot exceed maximum") }
                return
            }
        } else {
            if (min <= 0) {
                _addGoalUiState.update { it.copy(error = "Minimum income must be greater than 0") }
                return
            }
        }

        viewModelScope.launch {
            _addGoalUiState.update { it.copy(isSaving = true, error = null) }
            try {
                val goal = BudgetGoalEntity(
                    id = state.editingGoalId ?: "",
                    userId = uid,
                    goalName = "${category.mainCategory} - ${category.subCategory}",
                    categoryName = category.mainCategory,
                    subCategoryName = category.subCategory,
                    goalType = goalType,
                    minGoal = min,
                    maxGoal = max,
                    month = currentMonth,
                    year = currentYear,
                    isRecurring = state.isRecurring
                )
                if (state.isEditMode && state.editingGoalId != null) {
                    budgetGoalRepository.updateGoal(uid, goal)
                } else {
                    budgetGoalRepository.saveGoal(uid, goal)
                }
                _addGoalUiState.update {
                    it.copy(isSaving = false, isSaved = true, error = null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving goal: ${e.message}", e)
                _addGoalUiState.update {
                    it.copy(isSaving = false, error = "Failed to save: ${e.message}")
                }
            }
        }
    }


    fun showDeleteDialog(goalId: String, goalName: String) {
        _deleteDialogState.update {
            it.copy(isVisible = true, goalId = goalId, goalName = goalName)
        }
    }

    fun dismissDeleteDialog() {
        _deleteDialogState.update { it.copy(isVisible = false, goalId = "", goalName = "") }
    }

    fun confirmDelete() {
        val uid = userId ?: return
        val goalId = _deleteDialogState.value.goalId
        if (goalId.isBlank()) return
        viewModelScope.launch {
            try {
                budgetGoalRepository.deleteGoal(uid, goalId)
                dismissDeleteDialog()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting goal: ${e.message}", e)
                _listUiState.update { it.copy(error = "Failed to delete: ${e.message}") }
                dismissDeleteDialog()
            }
        }
    }
}