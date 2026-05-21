package group12.budgetbuddi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.repository.BudgetGoalRepository
import group12.budgetbuddi.repository.ExpenseRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar


/**
 * References:
 * - Android ViewModel & StateFlow: https://developer.android.com/topic/libraries/architecture/viewmodel
 * - Room Database Flow Collections: https://developer.android.com/training/data-storage/room
 */
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

    private val TAG = "CategoryTotalsVM"

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

            try {
                expenseRepository.getExpensesByDateRange(userEmail, fromDate, toDate)
                    .collect { expenses ->
                        val expensesOnly = expenses.filter { it.type == "expense" }
                        val currentState = _totalsUiState.value

                        val grouped = expensesOnly
                            .groupBy { it.category }
                            .map { (cat, list) ->
                                val total = list.sumOf { it.amount }
                                val status = when {
                                    currentState.maxGoal > 0 && total > currentState.maxGoal -> BudgetStatus.OVER
                                    currentState.minGoal > 0 && total < currentState.minGoal -> BudgetStatus.UNDER
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
            } catch (e: Exception) {
                Log.e(TAG, "Error loading totals: ${e.message}")
                _totalsUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadGoalForCurrentMonth() {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        viewModelScope.launch {
            try {
                budgetGoalRepository.getGoalForMonth(userEmail, month, year)
                    .collect { goal ->
                        Log.d(TAG, "Goal flow emitted: ${goal?.minGoal}, ${goal?.maxGoal}")

                        if (goal != null) {
                            _goalUiState.update {
                                it.copy(
                                    minGoal = goal.minGoal.toString(),
                                    maxGoal = goal.maxGoal.toString(),
                                    month = goal.month,
                                    year = goal.year,
                                    isSaved = false,
                                    error = null
                                )
                            }
                            _totalsUiState.update {
                                it.copy(minGoal = goal.minGoal, maxGoal = goal.maxGoal)
                            }
                        } else {
                            _goalUiState.update {
                                it.copy(
                                    minGoal = "",
                                    maxGoal = "",
                                    month = month,
                                    year = year,
                                    isSaved = false,
                                    error = null
                                )
                            }
                            _totalsUiState.update {
                                it.copy(minGoal = 0.0, maxGoal = 0.0)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading goal: ${e.message}")
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

        val minValue = if (state.minGoal.isEmpty()) "0" else state.minGoal
        val maxValue = if (state.maxGoal.isEmpty()) "0" else state.maxGoal

        val min = minValue.toDoubleOrNull()
        val max = maxValue.toDoubleOrNull()

        Log.d(TAG, "Saving goal - min: $min, max: $max, month: ${state.month}, year: ${state.year}")

        when {
            min == null || max == null ->
                _goalUiState.update { it.copy(error = "Please enter valid numbers") }
            min < 0 || max < 0 ->
                _goalUiState.update { it.copy(error = "Values cannot be negative") }
            min > max && max != 0.0 ->
                _goalUiState.update { it.copy(error = "Minimum cannot exceed maximum") }
            else -> {
                viewModelScope.launch {
                    try {
                        val goal = BudgetGoalEntity(
                            userEmail = userEmail,
                            month = state.month,
                            year = state.year,
                            minGoal = min,
                            maxGoal = max
                        )
                        _goalUiState.update { it.copy(error = null) }

                        budgetGoalRepository.saveGoal(goal)

                        Log.d(TAG, "Goal saved successfully - Expected: min=$min, max=$max")

                        _goalUiState.update {
                            it.copy(
                                minGoal = min.toString(),
                                maxGoal = max.toString(),
                                isSaved = true,
                                error = null
                            )
                        }

                        _totalsUiState.update {
                            it.copy(minGoal = min, maxGoal = max)
                        }

                        val currentState = _totalsUiState.value
                        if (currentState.fromDate.isNotEmpty() && currentState.toDate.isNotEmpty()) {
                            loadTotalsForPeriod(currentState.fromDate, currentState.toDate)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving goal: ${e.message}")
                        _goalUiState.update {
                            it.copy(error = "Failed to save budget goal: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    fun resetSavedFlag() {
        _goalUiState.update { it.copy(isSaved = false) }
    }

    // Add this function to manually refresh the goal (useful for debugging)
    fun refreshGoal() {
        loadGoalForCurrentMonth()
    }
}