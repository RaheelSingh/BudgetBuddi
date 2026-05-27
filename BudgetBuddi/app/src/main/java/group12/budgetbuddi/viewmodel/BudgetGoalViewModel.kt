package group12.budgetbuddi.viewmodel

import androidx.lifecycle.ViewModel
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.data.entity.BudgetGoalEntity

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
}