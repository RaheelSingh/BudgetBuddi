package group12.budgetbuddi.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.repository.ExpenseRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

data class BudgetGoalUiState @RequiresApi(Build.VERSION_CODES.O) constructor(
    val minGoal: String = "",
    val maxGoal: String = "",
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val isSaved: Boolean = false,
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
class CategoryTotalsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val expenseRepository = ExpenseRepository(db.expenseDao())
    private val budgetGoalRepository = BudgetGoalRepository(db.budgetGoalDao())
    private val sessionManager = SessionManager(application)
    private val userEmail: String = sessionManager.getUserEmail() ?: ""
    @RequiresApi(Build.VERSION_CODES.O)
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _totalsUiState = MutableStateFlow(CategoryTotalsUiState())
    val totalsUiState: StateFlow<CategoryTotalsUiState> = _totalsUiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _goalUiState = MutableStateFlow(BudgetGoalUiState())
    val goalUiState: StateFlow<BudgetGoalUiState> = _goalUiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val firstDay = today.withDayOfMonth(1).format(fmt)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth()).format(fmt)
        _totalsUiState.update { it.copy(fromDate = firstDay, toDate = lastDay) }

        loadGoalForCurrentMonth()
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

                    // Only expenses (not income)
                    val expensesOnly = expenses.filter { it.type == "expense" }

                    // Group by main category and sum amounts — sorted highest first
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


    private fun loadGoalForCurrentMonth() {
        val today = LocalDate.now()
        viewModelScope.launch {
            budgetGoalRepository
                .getGoalForMonth(userEmail, today.monthValue, today.year)
                .collect { goal ->
                    if (goal != null) {
                        _goalUiState.update {
                            it.copy(
                                minGoal = goal.minGoal.toString(),
                                maxGoal = goal.maxGoal.toString(),
                                month = goal.month,
                                year = goal.year
                            )
                        }
                        _totalsUiState.update {
                            it.copy(minGoal = goal.minGoal, maxGoal = goal.maxGoal)
                        }
                    }
                }
        }
    }

    fun updateMinGoal(value: String) {
        _goalUiState.update { it.copy(minGoal = value, error = null, isSaved = false) }
    }

    fun updateMaxGoal(value: String) {
        _goalUiState.update { it.copy(maxGoal = value, error = null, isSaved = false) }
    }

    fun saveBudgetGoal() {
        val state = _goalUiState.value
        val min = state.minGoal.toDoubleOrNull()
        val max = state.maxGoal.toDoubleOrNull()

        when {
            min == null || max == null ->
                _goalUiState.update { it.copy(error = "Please enter valid numbers") }
            min < 0 || max < 0 ->
                _goalUiState.update { it.copy(error = "Values cannot be negative") }
            min > max ->
                _goalUiState.update { it.copy(error = "Minimum cannot exceed maximum") }
            else -> {
                viewModelScope.launch {
                    budgetGoalRepository.saveGoal(
                        BudgetGoalEntity(
                            userEmail = userEmail,
                            month = state.month,
                            year = state.year,
                            minGoal = min,
                            maxGoal = max
                        )
                    )
                    _goalUiState.update { it.copy(isSaved = true, error = null) }
                    _totalsUiState.update { it.copy(minGoal = min, maxGoal = max) }
                }
            }
        }
    }

    fun resetSavedFlag() {
        _goalUiState.update { it.copy(isSaved = false) }
    }
}