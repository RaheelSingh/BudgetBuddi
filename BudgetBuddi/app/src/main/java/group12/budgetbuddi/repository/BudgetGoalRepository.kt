package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BudgetGoalRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "BudgetGoalRepository"
        private const val USERS_COLLECTION = "users"
        private const val BUDGET_GOALS_COLLECTION = "budget_goals"
    }

    private fun budgetGoalsCollection(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(BUDGET_GOALS_COLLECTION)

    private fun mapToGoal(
        id: String,
        userId: String,
        data: Map<String, Any>
    ): BudgetGoalEntity {
        return BudgetGoalEntity(
            id = id,
            userId = data["userId"] as? String ?: userId,
            goalName = data["goalName"] as? String ?: "",
            categoryName = data["categoryName"] as? String ?: "",
            subCategoryName = data["subCategoryName"] as? String ?: "",
            goalType = data["goalType"] as? String ?: "expense",
            minGoal = (data["minGoal"] as? Number)?.toDouble() ?: 0.0,
            maxGoal = (data["maxGoal"] as? Number)?.toDouble() ?: 0.0,
            month = (data["month"] as? Number)?.toInt() ?: 0,
            year = (data["year"] as? Number)?.toInt() ?: 0,
            isRecurring = data["isRecurring"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }

    fun getAllGoalsForMonth(
        userId: String,
        month: Int,
        year: Int
    ): Flow<List<BudgetGoalEntity>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = budgetGoalsCollection(userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing goals: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val goals = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    mapToGoal(doc.id, userId, data)
                }
                trySend(goals)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveGoal(userId: String, goal: BudgetGoalEntity): String {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot save goal: userId is blank")
        }
        val now = System.currentTimeMillis()
        val data = mapOf(
            "userId" to userId,
            "goalName" to goal.goalName,
            "categoryName" to goal.categoryName,
            "subCategoryName" to goal.subCategoryName,
            "goalType" to goal.goalType,
            "minGoal" to goal.minGoal,
            "maxGoal" to goal.maxGoal,
            "month" to goal.month,
            "year" to goal.year,
            "isRecurring" to goal.isRecurring,
            "createdAt" to now,
            "updatedAt" to now
        )
        val docRef = budgetGoalsCollection(userId).add(data).await()
        Log.d(TAG, "Goal saved: ${docRef.id} for userId=$userId")
        return docRef.id
    }

    suspend fun updateGoal(userId: String, goal: BudgetGoalEntity) {
        if (userId.isBlank() || goal.id.isBlank()) {
            throw IllegalArgumentException("Cannot update goal: userId or goalId is blank")
        }
        val now = System.currentTimeMillis()
        val data = mapOf(
            "userId" to userId,
            "goalName" to goal.goalName,
            "categoryName" to goal.categoryName,
            "subCategoryName" to goal.subCategoryName,
            "goalType" to goal.goalType,
            "minGoal" to goal.minGoal,
            "maxGoal" to goal.maxGoal,
            "month" to goal.month,
            "year" to goal.year,
            "isRecurring" to goal.isRecurring,
            "updatedAt" to now
        )
        budgetGoalsCollection(userId).document(goal.id).update(data).await()
        Log.d(TAG, "Goal updated: ${goal.id} for userId=$userId")
    }

    suspend fun deleteGoal(userId: String, goalId: String) {
        if (userId.isBlank() || goalId.isBlank()) {
            throw IllegalArgumentException("Cannot delete goal: userId or goalId is blank")
        }
        budgetGoalsCollection(userId).document(goalId).delete().await()
        Log.d(TAG, "Goal deleted: $goalId for userId=$userId")
    }
}