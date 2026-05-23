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

    private fun goalDocumentId(month: Int, year: Int): String {
        return "%04d_%02d".format(year, month)
    }

    fun getGoalForMonth(
        userId: String,
        month: Int,
        year: Int
    ): Flow<BudgetGoalEntity?> = callbackFlow {

        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val documentId = goalDocumentId(month, year)

        val listener = budgetGoalsCollection(userId)
            .document(documentId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error observing budget goal: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val data = snapshot.data

                if (data == null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val goal = BudgetGoalEntity(
                    id = snapshot.id,
                    userId = data["userId"] as? String ?: userId,
                    month = (data["month"] as? Number)?.toInt() ?: month,
                    year = (data["year"] as? Number)?.toInt() ?: year,
                    minGoal = (data["minGoal"] as? Number)?.toDouble() ?: 0.0,
                    maxGoal = (data["maxGoal"] as? Number)?.toDouble() ?: 0.0,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                )

                trySend(goal)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun saveGoal(
        userId: String,
        goal: BudgetGoalEntity
    ) {
        if (userId.isBlank()) {
            throw IllegalArgumentException("Cannot save budget goal because userId is blank")
        }

        val documentId = goalDocumentId(goal.month, goal.year)
        val now = System.currentTimeMillis()

        val cleanGoal = goal.copy(
            userId = userId,
            createdAt = if (goal.createdAt == 0L) now else goal.createdAt,
            updatedAt = now
        )

        val data = mapOf(
            "userId" to cleanGoal.userId,
            "month" to cleanGoal.month,
            "year" to cleanGoal.year,
            "minGoal" to cleanGoal.minGoal,
            "maxGoal" to cleanGoal.maxGoal,
            "createdAt" to cleanGoal.createdAt,
            "updatedAt" to cleanGoal.updatedAt
        )

        budgetGoalsCollection(userId)
            .document(documentId)
            .set(data)
            .await()

        Log.d(TAG, "Budget goal saved: $documentId for userId=$userId")
    }
}