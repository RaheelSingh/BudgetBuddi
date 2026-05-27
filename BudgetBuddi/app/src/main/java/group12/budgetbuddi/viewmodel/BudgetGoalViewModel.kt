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
}