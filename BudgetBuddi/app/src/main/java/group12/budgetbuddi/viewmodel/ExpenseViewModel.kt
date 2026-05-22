package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.repository.ExpenseRepository
import group12.budgetbuddi.utils.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val sessionManager = SessionManager(application)
    val currentUserEmail: String = sessionManager.getUserEmail() ?: "guest"

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    val allExpenses: StateFlow<List<ExpenseEntity>>
    val expensesOnly: StateFlow<List<ExpenseEntity>>

    init {
        val dao = AppDatabase.getInstance(application).expenseDao()
        repository = ExpenseRepository(dao)

        allExpenses = repository.getAllExpenses(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        expensesOnly = repository.getExpensesOnly(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun saveExpense(
        amount: Double,
        type: String,
        category: String,
        subCategory: String,
        date: String,
        startTime: String,
        endTime: String,
        note: String,
        photoPath: String?,
        paymentMethod: String,
        isRecurring: Boolean
    ) {
        if (amount <= 0.0) {
            _uiMessage.value = "Please enter a valid amount"
            return
        }
        if (category.isBlank()) {
            _uiMessage.value = "Please select a category"
            return
        }
        if (date.isBlank()) {
            _uiMessage.value = "Please select a date"
            return
        }

        val expense = ExpenseEntity(
            userEmail = currentUserEmail,
            amount = amount,
            type = type,
            category = category,
            subCategory = subCategory,
            date = date,
            startTime = startTime,
            endTime = endTime,
            note = note,
            photoPath = photoPath,
            paymentMethod = paymentMethod,
            isRecurring = isRecurring
        )

        viewModelScope.launch {
            repository.insertExpense(expense)
            _uiMessage.value = "Saved successfully"
            _saveSuccess.value = true
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _uiMessage.value = "Expense deleted"
        }
    }
    fun setMessage(message: String) {
        _uiMessage.value = message
    }
    fun clearMessage() {
        _uiMessage.value = null
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}