package group12.budgetbuddi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.repository.ExpenseRepository
import kotlinx.coroutines.Job
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
    val fromDate: String = "",
    val toDate: String = "",
    val isLoading: Boolean = false
)

class CategoryTotalsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CategoryTotalsVM"
    }

    private val auth = FirebaseAuth.getInstance()
    private val expenseRepository = ExpenseRepository()

    private val userId: String?
        get() = auth.currentUser?.uid

    private var totalsJob: Job? = null

    private val _totalsUiState = MutableStateFlow(CategoryTotalsUiState())
    val totalsUiState: StateFlow<CategoryTotalsUiState> = _totalsUiState.asStateFlow()

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
        totalsJob?.cancel()
        totalsJob = viewModelScope.launch {
            _totalsUiState.update { it.copy(isLoading = true) }
            val uid = userId
            if (uid.isNullOrBlank()) {
                Log.e(TAG, "Cannot load totals because userId is blank")
                _totalsUiState.update {
                    it.copy(items = emptyList(), grandTotal = 0.0, isLoading = false)
                }
                return@launch
            }
            try {
                expenseRepository.getExpensesByDateRange(
                    userId = uid,
                    fromDate = fromDate,
                    toDate = toDate
                ).collect { expenses ->
                    val expensesOnly = expenses.filter { it.type == "expense" }
                    val grouped = expensesOnly
                        .groupBy { it.category }
                        .map { (category, list) ->
                            val total = list.sumOf { it.amount }
                            CategoryTotalItem(
                                categoryName = category,
                                totalSpent = total,
                                budgetStatus = BudgetStatus.OK
                            )
                        }
                        .sortedByDescending { it.totalSpent }
                    val grandTotal = expensesOnly.sumOf { it.amount }
                    _totalsUiState.update {
                        it.copy(
                            items = grouped,
                            grandTotal = grandTotal,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading totals: ${e.message}", e)
                _totalsUiState.update { it.copy(isLoading = false) }
            }
        }
    }
}