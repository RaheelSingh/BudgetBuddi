package group12.budgetbuddi.repository

import group12.budgetbuddi.data.dao.ExpenseDao
import group12.budgetbuddi.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpenses(userEmail: String): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses(userEmail)

    fun getExpensesOnly(userEmail: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesOnly(userEmail)

    fun getIncomeOnly(userEmail: String): Flow<List<ExpenseEntity>> =
        expenseDao.getIncomeOnly(userEmail)

    fun getExpensesByDateRange(
        userEmail: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByDateRange(userEmail, fromDate, toDate)

}