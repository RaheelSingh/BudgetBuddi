package group12.budgetbuddi.repository

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

    fun getExpensesByCategory(
        userEmail: String,
        category: String
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCategory(userEmail, category)

    fun getExpensesByCategoryAndDateRange(
        userEmail: String,
        category: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCategoryAndDateRange(userEmail, category, fromDate, toDate)

    fun getTotalExpenses(userEmail: String): Flow<Double?> =
        expenseDao.getTotalExpenses(userEmail)

    fun getTotalIncome(userEmail: String): Flow<Double?> =
        expenseDao.getTotalIncome(userEmail)

    suspend fun insertExpense(expense: ExpenseEntity): Long =
        expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) =
        expenseDao.deleteExpense(expense)

    suspend fun getExpenseById(id: Long): ExpenseEntity? =
        expenseDao.getExpenseById(id)
}