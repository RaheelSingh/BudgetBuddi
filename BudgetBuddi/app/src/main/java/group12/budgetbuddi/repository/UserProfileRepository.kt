package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import group12.budgetbuddi.data.AchievementsData
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.data.entity.UserProfileEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "UserProfileRepository"
        private const val USERS_COLLECTION = "users"
    }

    fun userDocument(userId: String) = firestore.collection(USERS_COLLECTION).document(userId)

    fun observeUserProfile(userId: String): Flow<UserProfileEntity?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = userDocument(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val profile = UserProfileEntity(
                    userId = snapshot.id,
                    email = snapshot.getString("email") ?: "",
                    username = snapshot.getString("username") ?: "",
                    createdAt = snapshot.getTimestamp("createdAt"),
                    lastLogin = snapshot.getTimestamp("lastLogin"),
                    xp = snapshot.getLong("xp")?.toInt() ?: 0,
                    level = snapshot.getLong("level")?.toInt() ?: 1,
                    levelTitle = snapshot.getString("levelTitle") ?: "Novice Budgeter",
                    currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                    longestStreak = snapshot.getLong("longestStreak")?.toInt() ?: 0,
                    lastLoginDate = snapshot.getString("lastLoginDate"),
                    selectedAvatarBadgeId = snapshot.getString("selectedAvatarBadgeId"),
                    unlockedBadges = snapshot.get("unlockedBadges") as? List<String> ?: emptyList(),
                    totalExpenses = snapshot.getLong("totalExpenses")?.toInt() ?: 0,
                    totalIncome = snapshot.getLong("totalIncome")?.toInt() ?: 0,
                    totalBudgetGoals = snapshot.getLong("totalBudgetGoals")?.toInt() ?: 0,
                    totalCategoriesCreated = snapshot.getLong("totalCategoriesCreated")?.toInt() ?: 0,
                    totalPhotosAdded = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0
                )

                trySend(profile)
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        try {
            userDocument(userId).update(updates).await()
            Log.d(TAG, "Profile updated for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}", e)
        }
    }

    suspend fun updateLoginStreak(userId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = getYesterdayString()

        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) return

        val lastLoginDate = snapshot.getString("lastLoginDate")
        val currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0
        val longestStreak = snapshot.getLong("longestStreak")?.toInt() ?: 0

        val newStreak = when {
            lastLoginDate == null || lastLoginDate.isEmpty() -> 1
            lastLoginDate == today -> currentStreak
            lastLoginDate == yesterday -> currentStreak + 1
            else -> 1
        }

        updateUserProfile(userId, mapOf(
            "lastLoginDate" to today,
            "currentStreak" to newStreak,
            "longestStreak" to maxOf(longestStreak, newStreak),
            "lastLogin" to com.google.firebase.Timestamp.now()
        ))
    }

    suspend fun unlockAchievement(userId: String, achievementId: String): AchievementEntity? {
        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) return null

        val unlockedBadges = (snapshot.get("unlockedBadges") as? List<String>) ?: emptyList()

        if (unlockedBadges.contains(achievementId)) return null

        val achievement = AchievementsData.getAchievementById(achievementId) ?: return null
        val currentXP = snapshot.getLong("xp")?.toInt() ?: 0
        val newXP = currentXP + achievement.xpReward
        val newLevel = AchievementsData.calculateLevel(newXP)
        val newLevelTitle = AchievementsData.getLevelTitle(newLevel)

        val updates = mutableMapOf<String, Any>(
            "unlockedBadges" to (unlockedBadges + achievementId),
            "xp" to newXP,
            "level" to newLevel,
            "levelTitle" to newLevelTitle
        )

        // If this is the first unlocked badge, set it as default avatar
        if (unlockedBadges.isEmpty() && snapshot.getString("selectedAvatarBadgeId").isNullOrEmpty()) {
            updates["selectedAvatarBadgeId"] = achievementId
        }

        updateUserProfile(userId, updates)

        Log.d(TAG, "🎉 Achievement UNLOCKED: ${achievement.name} (+${achievement.xpReward} XP)")
        Log.d(TAG, "🏆 New Level: $newLevel - $newLevelTitle")

        return achievement
    }

    suspend fun checkAndUnlockAchievements(userId: String): AchievementEntity? {
        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) return null

        val unlockedBadges = (snapshot.get("unlockedBadges") as? List<String>) ?: emptySet()

        val totalExpenses = snapshot.getLong("totalExpenses")?.toInt() ?: 0
        val totalIncome = snapshot.getLong("totalIncome")?.toInt() ?: 0
        val totalBudgetGoals = snapshot.getLong("totalBudgetGoals")?.toInt() ?: 0
        val totalCategoriesCreated = snapshot.getLong("totalCategoriesCreated")?.toInt() ?: 0
        val totalPhotosAdded = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0

        val achievementsToUnlock = AchievementsData.achievements.filter { achievement ->
            !unlockedBadges.contains(achievement.id) && when (achievement.criteriaType) {
                "expense_count" -> totalExpenses >= achievement.criteriaValue
                "income_count" -> totalIncome >= achievement.criteriaValue
                "budget_goal_created" -> totalBudgetGoals >= achievement.criteriaValue
                "category_created" -> totalCategoriesCreated >= achievement.criteriaValue
                "transaction_with_photo" -> totalPhotosAdded >= achievement.criteriaValue
                else -> false
            }
        }

        achievementsToUnlock.forEach { achievement ->
            unlockAchievement(userId, achievement.id)
        }

        return achievementsToUnlock.firstOrNull()
    }

    suspend fun incrementCounter(userId: String, counterName: String, hasPhoto: Boolean = false) {
        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) return

        val currentValue = snapshot.getLong(counterName)?.toInt() ?: 0
        val updates = mutableMapOf<String, Any>(counterName to currentValue + 1)

        if (counterName == "totalExpenses" && hasPhoto) {
            val currentPhotos = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0
            updates["totalPhotosAdded"] = currentPhotos + 1
        }

        updateUserProfile(userId, updates)
    }

    private fun getYesterdayString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}