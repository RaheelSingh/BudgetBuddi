package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import group12.budgetbuddi.data.entity.ExpenseEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "ExpenseRepository"
        private const val USERS_COLLECTION = "users"
        private const val EXPENSES_COLLECTION = "expenses"
    }

    private fun expensesCollection(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(EXPENSES_COLLECTION)

    private fun mapToExpense(
        id: String,
        userId: String,
        data: Map<String, Any>
    ): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            userId = data["userId"] as? String ?: userId,
            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
            type = data["type"] as? String ?: "",
            category = data["category"] as? String ?: "",
            subCategory = data["subCategory"] as? String ?: "",
            date = data["date"] as? String ?: "",
            startTime = data["startTime"] as? String ?: "",
            endTime = data["endTime"] as? String ?: "",
            note = data["note"] as? String ?: "",
            photoPath = data["photoPath"] as? String,
            paymentMethod = data["paymentMethod"] as? String ?: "",
            isRecurring = data["isRecurring"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }

    private fun expenseToMap(expense: ExpenseEntity): Map<String, Any?> {
        return mapOf(
            "userId" to expense.userId,
            "amount" to expense.amount,
            "type" to expense.type,
            "category" to expense.category,
            "subCategory" to expense.subCategory,
            "date" to expense.date,
            "startTime" to expense.startTime,
            "endTime" to expense.endTime,
            "note" to expense.note,
            "photoPath" to expense.photoPath,
            "paymentMethod" to expense.paymentMethod,
            "isRecurring" to expense.isRecurring,
            "createdAt" to expense.createdAt,
            "updatedAt" to expense.updatedAt
        )
    }

    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
        if (userId.isBlank()) {
            Log.e(TAG, "Cannot observe expenses because userId is blank")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "Starting expenses listener for userId=$userId")

        val listenerRegistration = expensesCollection(userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error listening to expenses: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Expense snapshot is null for userId=$userId")
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents.mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null

                    mapToExpense(
                        id = document.id,
                        userId = userId,
                        data = data
                    )
                }

                Log.d(TAG, "Observed ${expenses.size} expenses for userId=$userId")

                trySend(expenses)
            }

        awaitClose {
            Log.d(TAG, "Closing expenses listener for userId=$userId")
            listenerRegistration.remove()
        }
    }

    fun getExpensesOnly(userId: String): Flow<List<ExpenseEntity>> =
        getAllExpenses(userId).map { list ->
            list.filter { it.type == "expense" }
        }

    fun getIncomeOnly(userId: String): Flow<List<ExpenseEntity>> =
        getAllExpenses(userId).map { list ->
            list.filter { it.type == "income" }
        }

    fun getExpensesByDateRange(
        userId: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userId).map { list ->
            list.filter {
                it.date >= fromDate && it.date <= toDate
            }
        }

    fun getExpensesByCategory(
        userId: String,
        category: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userId).map { list ->
            list.filter {
                it.category == category
            }
        }

    fun getExpensesByCategoryAndDateRange(
        userId: String,
        category: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userId).map { list ->
            list.filter {
                it.category == category &&
                        it.date >= fromDate &&
                        it.date <= toDate
            }
        }

    fun getTotalExpenses(userId: String): Flow<Double?> =
        getExpensesOnly(userId).map { list ->
            list.sumOf { it.amount }.takeIf { it > 0.0 }
        }

    fun getTotalIncome(userId: String): Flow<Double?> =
        getIncomeOnly(userId).map { list ->
            list.sumOf { it.amount }.takeIf { it > 0.0 }
        }

    suspend fun insertExpense(
        userId: String,
        expense: ExpenseEntity
    ): String {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot insert expense because userId is blank")
        }

        return try {
            val now = System.currentTimeMillis()

            val cleanExpense = expense.copy(
                userId = userId,
                createdAt = if (expense.createdAt == 0L) now else expense.createdAt,
                updatedAt = now
            )

            val documentRef = expensesCollection(userId)
                .add(expenseToMap(cleanExpense))
                .await()

            Log.d(TAG, "Expense inserted with ID: ${documentRef.id} for userId=$userId")

            documentRef.id

        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateExpense(
        userId: String,
        expense: ExpenseEntity
    ) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot update expense because userId is blank")
        }

        if (expense.id.isBlank()) {
            throw IllegalArgumentException("Cannot update expense because expense ID is blank")
        }

        try {
            val cleanExpense = expense.copy(
                userId = userId,
                updatedAt = System.currentTimeMillis()
            )

            expensesCollection(userId)
                .document(expense.id)
                .set(expenseToMap(cleanExpense))
                .await()

            Log.d(TAG, "Expense updated: ${expense.id} for userId=$userId")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteExpense(
        userId: String,
        expense: ExpenseEntity
    ) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot delete expense because userId is blank")
        }

        if (expense.id.isBlank()) {
            throw IllegalArgumentException("Cannot delete expense because expense ID is blank")
        }

        try {
            expensesCollection(userId)
                .document(expense.id)
                .delete()
                .await()

            Log.d(TAG, "Expense deleted: ${expense.id} for userId=$userId")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun getExpenseById(
        userId: String,
        expenseId: String
    ): ExpenseEntity? {
        if (userId.isBlank() || expenseId.isBlank()) {
            return null
        }

        return try {
            val document = expensesCollection(userId)
                .document(expenseId)
                .get()
                .await()

            val data = document.data ?: return null

            mapToExpense(
                id = document.id,
                userId = userId,
                data = data
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get expense by ID: ${e.message}", e)
            null
        }
    }
}