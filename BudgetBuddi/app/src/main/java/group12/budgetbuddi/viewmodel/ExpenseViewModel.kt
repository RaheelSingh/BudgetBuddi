package group12.budgetbuddi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.ExpenseEntity
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

}