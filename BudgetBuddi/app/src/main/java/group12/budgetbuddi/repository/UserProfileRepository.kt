package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import group12.budgetbuddi.data.AchievementsData
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.UserProfileEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val expenseRepository = ExpenseRepository()
    private val budgetGoalRepository = BudgetGoalRepository()

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
                    createdAt = null,
                    lastLogin = null,
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
                    totalPhotosAdded = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0,
                    budgetGoalsAchieved = snapshot.getLong("budgetGoalsAchieved")?.toInt() ?: 0,
                    monthlyLoginDays = snapshot.getLong("monthlyLoginDays")?.toInt() ?: 0,
                    currentMonth = snapshot.getLong("currentMonth")?.toInt() ?: 0,
                    currentYear = snapshot.getLong("currentYear")?.toInt() ?: 0,
                    totalNotesAdded = snapshot.getLong("totalNotesAdded")?.toInt() ?: 0
                )

                trySend(profile)
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        try {
            userDocument(userId).update(updates).await()
            Log.d(TAG, "Profile updated for $userId with updates: $updates")
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

        Log.d(TAG, "updateLoginStreak: lastLoginDate=$lastLoginDate, today=$today, yesterday=$yesterday, newStreak=$newStreak")

        updateUserProfile(userId, mapOf(
            "lastLoginDate" to today,
            "currentStreak" to newStreak,
            "longestStreak" to maxOf(longestStreak, newStreak)
        ))
    }

    suspend fun updateUserAvatar(userId: String, badgeId: String?) {
        val finalBadgeId = badgeId ?: ""
        updateUserProfile(userId, mapOf("selectedAvatarBadgeId" to finalBadgeId))
        Log.d(TAG, "User avatar updated for $userId to: $finalBadgeId")
    }

    suspend fun unlockAchievement(userId: String, achievementId: String): AchievementEntity? {
        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) return null

        val unlockedBadges = (snapshot.get("unlockedBadges") as? List<String>) ?: emptyList()

        if (unlockedBadges.contains(achievementId)) {
            Log.d(TAG, "Achievement $achievementId already unlocked")
            return null
        }

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

        if (unlockedBadges.isEmpty() && snapshot.getString("selectedAvatarBadgeId").isNullOrEmpty()) {
            updates["selectedAvatarBadgeId"] = achievementId
        }

        updateUserProfile(userId, updates)

        Log.d(TAG, "🎉 Achievement UNLOCKED: ${achievement.name} (+${achievement.xpReward} XP)")
        Log.d(TAG, "🏆 New Level: $newLevel - $newLevelTitle")

        return achievement
    }

    suspend fun checkAndUnlockAchievements(userId: String): AchievementEntity? {
        Log.d(TAG, "========== CHECKING ACHIEVEMENTS FOR USER: $userId ==========")

        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) {
            Log.d(TAG, "User document not found for $userId")
            return null
        }

        val unlockedBadges = (snapshot.get("unlockedBadges") as? List<String>) ?: emptySet()

        // TIER 1 & 2 COUNTERS
        val totalExpenses = snapshot.getLong("totalExpenses")?.toInt() ?: 0
        val totalIncome = snapshot.getLong("totalIncome")?.toInt() ?: 0
        val totalBudgetGoals = snapshot.getLong("totalBudgetGoals")?.toInt() ?: 0
        val totalCategoriesCreated = snapshot.getLong("totalCategoriesCreated")?.toInt() ?: 0
        val totalPhotosAdded = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0
        val currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0
        val budgetGoalsAchieved = snapshot.getLong("budgetGoalsAchieved")?.toInt() ?: 0
        val monthlyLoginDays = snapshot.getLong("monthlyLoginDays")?.toInt() ?: 0

        Log.d(TAG, "Current counters:")
        Log.d(TAG, "  totalExpenses: $totalExpenses")
        Log.d(TAG, "  totalIncome: $totalIncome")
        Log.d(TAG, "  totalBudgetGoals: $totalBudgetGoals")
        Log.d(TAG, "  totalCategoriesCreated: $totalCategoriesCreated")
        Log.d(TAG, "  totalPhotosAdded: $totalPhotosAdded")
        Log.d(TAG, "  currentStreak: $currentStreak")
        Log.d(TAG, "  budgetGoalsAchieved: $budgetGoalsAchieved")
        Log.d(TAG, "  monthlyLoginDays: $monthlyLoginDays")

        // ===== TIER 3 CALCULATIONS =====
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthPrefix = "$currentYear-${String.format("%02d", currentMonth)}"
        val firstDay = "$monthPrefix-01"
        val lastDay = "$monthPrefix-${daysInMonth}"

        var monthlySaved = 0.0
        var foodExpenses = 0
        var allCategoriesUnderBudget = true

        try {
            // Get all transactions for current month
            val transactionFlow = expenseRepository.getExpensesByDateRange(userId, firstDay, lastDay)
            val transactions = transactionFlow.first()

            Log.d(TAG, "Found ${transactions.size} transactions for period $firstDay to $lastDay")

            // Calculate monthly savings
            var monthlyExpensesTotal = 0.0
            var monthlyIncomeTotal = 0.0

            for (transaction in transactions) {
                if (transaction.type == "expense") {
                    monthlyExpensesTotal += transaction.amount
                } else if (transaction.type == "income") {
                    monthlyIncomeTotal += transaction.amount
                }
            }
            monthlySaved = monthlyIncomeTotal - monthlyExpensesTotal

            // Count food expenses for Foodie achievement
            foodExpenses = 0
            for (transaction in transactions) {
                if (transaction.type == "expense") {
                    val categoryLower = transaction.category.lowercase(Locale.getDefault())
                    if (categoryLower.contains("food") || categoryLower.contains("food & drink")) {
                        foodExpenses++
                    }
                }
            }


            val budgetGoals = budgetGoalRepository.getGoalsForMonth(userId, currentMonth, currentYear)
            val expenseGoals = mutableListOf<BudgetGoalEntity>()
            for (goal in budgetGoals) {
                if (goal.goalType == "expense") {
                    expenseGoals.add(goal)
                }
            }

            if (expenseGoals.isNotEmpty()) {
                // Group expenses by category
                val expensesByCategory = mutableMapOf<String, Double>()
                for (transaction in transactions) {
                    if (transaction.type == "expense") {
                        val currentAmount = expensesByCategory[transaction.category] ?: 0.0
                        expensesByCategory[transaction.category] = currentAmount + transaction.amount
                    }
                }

                // Check each budget goal
                for (goal in expenseGoals) {
                    val categorySpent = expensesByCategory[goal.categoryName] ?: 0.0
                    if (categorySpent > goal.maxGoal) {
                        allCategoriesUnderBudget = false
                        break
                    }
                }
            } else {
                allCategoriesUnderBudget = false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating Tier 3 achievements: ${e.message}")
        }

        Log.d(TAG, "Tier 3 values:")
        Log.d(TAG, "  monthlySaved: R$monthlySaved (need 1000 for Saver)")
        Log.d(TAG, "  foodExpenses: $foodExpenses (need 30 for Foodie)")
        Log.d(TAG, "  allCategoriesUnderBudget: $allCategoriesUnderBudget")

        val achievementsToUnlock = mutableListOf<AchievementEntity>()

        for (achievement in AchievementsData.achievements) {
            val alreadyUnlocked = unlockedBadges.contains(achievement.id)
            if (!alreadyUnlocked) {
                val shouldUnlock = when (achievement.criteriaType) {
                    // TIER 1
                    "expense_count" -> {
                        val result = totalExpenses >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: expense_count - $totalExpenses >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "income_count" -> {
                        val result = totalIncome >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: income_count - $totalIncome >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "budget_goal_created" -> {
                        val result = totalBudgetGoals >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: budget_goal_created - $totalBudgetGoals >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "category_created" -> {
                        val result = totalCategoriesCreated >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: category_created - $totalCategoriesCreated >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "transaction_with_photo" -> {
                        val result = totalPhotosAdded >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: transaction_with_photo - $totalPhotosAdded >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    // TIER 2
                    "streak" -> {
                        val result = currentStreak >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: streak - $currentStreak >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    // TIER 3
                    "budget_goals_achieved" -> {
                        val result = budgetGoalsAchieved >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: budget_goals_achieved - $budgetGoalsAchieved >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "monthly_login_completion" -> {
                        val result = monthlyLoginDays >= daysInMonth
                        Log.d(TAG, "  ${achievement.name}: monthly_login_completion - $monthlyLoginDays >= $daysInMonth = $result")
                        result
                    }
                    "monthly_saved" -> {
                        val result = monthlySaved >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: monthly_saved - $monthlySaved >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "category_expense_count" -> {
                        val result = foodExpenses >= achievement.criteriaValue
                        Log.d(TAG, "  ${achievement.name}: category_expense_count - $foodExpenses >= ${achievement.criteriaValue} = $result")
                        result
                    }
                    "all_categories_under_budget" -> {
                        Log.d(TAG, "  ${achievement.name}: all_categories_under_budget = $allCategoriesUnderBudget")
                        allCategoriesUnderBudget
                    }
                    else -> {
                        Log.d(TAG, "  ${achievement.name}: unknown criteria type = false")
                        false
                    }
                }
                if (shouldUnlock) {
                    achievementsToUnlock.add(achievement)
                    Log.d(TAG, "  ✅ ${achievement.name} will be unlocked!")
                }
            } else {
                Log.d(TAG, "  ${achievement.name} already unlocked")
            }
        }

        if (achievementsToUnlock.isNotEmpty()) {
            val achievementNames = achievementsToUnlock.map { it.name }
            Log.d(TAG, "🎯 ACHIEVEMENTS TO UNLOCK: $achievementNames")
        } else {
            Log.d(TAG, "No new achievements to unlock at this time")
        }

        var lastUnlocked: AchievementEntity? = null
        for (achievement in achievementsToUnlock) {
            lastUnlocked = unlockAchievement(userId, achievement.id)
        }

        return lastUnlocked
    }

    suspend fun getCounter(userId: String, counterName: String): Int {
        val snapshot = userDocument(userId).get().await()
        return snapshot.getLong(counterName)?.toInt() ?: 0
    }

    suspend fun incrementCounter(userId: String, counterName: String, hasPhoto: Boolean = false) {
        Log.d(TAG, "incrementCounter: $counterName for user $userId (hasPhoto=$hasPhoto)")

        val snapshot = userDocument(userId).get().await()
        if (!snapshot.exists()) {
            Log.e(TAG, "User document not found for incrementCounter")
            return
        }

        val currentValue = snapshot.getLong(counterName)?.toInt() ?: 0
        val updates = mutableMapOf<String, Any>(counterName to currentValue + 1)

        Log.d(TAG, "  $counterName: $currentValue -> ${currentValue + 1}")

        if (counterName == "totalExpenses" && hasPhoto) {
            val currentPhotos = snapshot.getLong("totalPhotosAdded")?.toInt() ?: 0
            updates["totalPhotosAdded"] = currentPhotos + 1
            Log.d(TAG, "  totalPhotosAdded: $currentPhotos -> ${currentPhotos + 1}")
        }

        updateUserProfile(userId, updates)
    }

    private fun getYesterdayString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}