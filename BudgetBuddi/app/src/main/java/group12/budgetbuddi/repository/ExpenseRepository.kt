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
        private const val EXPENSES_COLLECTION = "expenses"
    }


    private fun mapToExpense(id: String, data: Map<String, Any>): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            userEmail = data["userEmail"] as? String ?: "",
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
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }


    private fun expenseToMap(expense: ExpenseEntity): Map<String, Any?> {
        return mapOf(
            "userEmail" to expense.userEmail,
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
            "createdAt" to expense.createdAt
        )
    }

    // ─── Real-time listener as Flow ───────────────────────────────────────────

    fun getAllExpenses(userEmail: String): Flow<List<ExpenseEntity>> = callbackFlow {
        val listenerRegistration = firestore.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userEmail", userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to expenses: ${error.message}", error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { mapToExpense(doc.id, it) }
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getExpensesOnly(userEmail: String): Flow<List<ExpenseEntity>> =
        getAllExpenses(userEmail).map { list -> list.filter { it.type == "expense" } }

    fun getIncomeOnly(userEmail: String): Flow<List<ExpenseEntity>> =
        getAllExpenses(userEmail).map { list -> list.filter { it.type == "income" } }

    fun getExpensesByDateRange(
        userEmail: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userEmail).map { list ->
            list.filter { it.date >= fromDate && it.date <= toDate }
        }

    fun getExpensesByCategory(
        userEmail: String,
        category: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userEmail).map { list -> list.filter { it.category == category } }

    fun getExpensesByCategoryAndDateRange(
        userEmail: String,
        category: String,
        fromDate: String,
        toDate: String
    ): Flow<List<ExpenseEntity>> =
        getAllExpenses(userEmail).map { list ->
            list.filter {
                it.category == category && it.date >= fromDate && it.date <= toDate
            }
        }

    fun getTotalExpenses(userEmail: String): Flow<Double?> =
        getExpensesOnly(userEmail).map { list ->
            list.sumOf { it.amount }.takeIf { it > 0 }
        }

    fun getTotalIncome(userEmail: String): Flow<Double?> =
        getIncomeOnly(userEmail).map { list ->
            list.sumOf { it.amount }.takeIf { it > 0 }
        }


    suspend fun insertExpense(expense: ExpenseEntity): String {
        return try {
            val docRef = firestore.collection(EXPENSES_COLLECTION)
                .add(expenseToMap(expense))
                .await()
            Log.d(TAG, "Expense inserted with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        try {
            if (expense.id.isBlank()) throw IllegalArgumentException("Expense ID is blank")
            firestore.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .set(expenseToMap(expense))
                .await()
            Log.d(TAG, "Expense updated: ${expense.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        try {
            if (expense.id.isBlank()) throw IllegalArgumentException("Expense ID is blank")
            firestore.collection(EXPENSES_COLLECTION)
                .document(expense.id)
                .delete()
                .await()
            Log.d(TAG, "Expense deleted: ${expense.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun getExpenseById(id: String): ExpenseEntity? {
        return try {
            val doc = firestore.collection(EXPENSES_COLLECTION).document(id).get().await()
            doc.data?.let { mapToExpense(doc.id, it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get expense by ID: ${e.message}", e)
            null
        }
    }
}