package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.repository.ExpenseRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryTotalItem(
    val categoryName: String,
    val totalSpent: Double,
    val budgetStatus: BudgetStatus
)

enum class BudgetStatus { UNDER, OK, OVER }

data class CategoryTotalsUiState(
    val items: List<CategoryTotalItem> = emptyList(),
    val grandTotal: Double = 0.0,
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val fromDate: String = "",
    val toDate: String = "",
    val isLoading: Boolean = false
)

data class BudgetGoalUiState(
    val minGoal: String = "",
    val maxGoal: String = "",
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isSaved: Boolean = false,
    val error: String? = null
)

class CategoryTotalsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val expenseRepository = ExpenseRepository(db.expenseDao())
    private val budgetGoalRepository = BudgetGoalRepository(db.budgetGoalDao())
    private val sessionManager = SessionManager(application)
    private val userEmail: String = sessionManager.getUserEmail() ?: ""

    private val _totalsUiState = MutableStateFlow(CategoryTotalsUiState())
    val totalsUiState: StateFlow<CategoryTotalsUiState> = _totalsUiState.asStateFlow()

    private val _goalUiState = MutableStateFlow(BudgetGoalUiState())
    val goalUiState: StateFlow<BudgetGoalUiState> = _goalUiState.asStateFlow()

    init {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val firstDay = "%04d-%02d-01".format(year, month)
        val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lastDay = "%04d-%02d-%02d".format(year, month, lastDayOfMonth)

        _totalsUiState.update { it.copy(fromDate = firstDay, toDate = lastDay) }
        loadTotalsForPeriod(firstDay, lastDay)
    }

    fun setFromDate(date: String) {
        _totalsUiState.update { it.copy(fromDate = date) }
        refreshTotals()
    }

    fun setToDate(date: String) {
        _totalsUiState.update { it.copy(toDate = date) }
        refreshTotals()
    }

    private fun refreshTotals() {
        val state = _totalsUiState.value
        if (state.fromDate.isNotEmpty() && state.toDate.isNotEmpty()) {
            loadTotalsForPeriod(state.fromDate, state.toDate)
        }
    }

     private fun loadTotalsForPeriod(fromDate: String, toDate: String) {
        viewModelScope.launch {
            _totalsUiState.update { it.copy(isLoading = true) }

            expenseRepository
                .getExpensesByDateRange(userEmail, fromDate, toDate)
                .combine(_totalsUiState) { expenses, state -> Pair(expenses, state) }
                .collect { (expenses, state) ->

                    val expensesOnly = expenses.filter { it.type == "expense" }

                    val grouped = expensesOnly
                        .groupBy { it.category }
                        .map { (cat, list) ->
                            val total = list.sumOf { it.amount }
                            val status = when {
                                state.maxGoal > 0 && total > state.maxGoal -> BudgetStatus.OVER
                                state.minGoal > 0 && total < state.minGoal -> BudgetStatus.UNDER
                                else -> BudgetStatus.OK
                            }
                            CategoryTotalItem(cat, total, status)
                        }
                        .sortedByDescending { it.totalSpent }

                    val grand = expensesOnly.sumOf { it.amount }

                    _totalsUiState.update {
                        it.copy(
                            items = grouped,
                            grandTotal = grand,
                            isLoading = false
                        )
                    }
                }
        }
    }
}