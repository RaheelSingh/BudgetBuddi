package group12.budgetbuddi.data.entity

import com.google.firebase.Timestamp

data class UserProfileEntity(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,

    // Gamification fields
    val xp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Novice Budgeter",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: String? = null,
    val selectedAvatarBadgeId: String? = null,
    val unlockedBadges: List<String> = emptyList(),

    // Achievement counters
    val totalExpenses: Int = 0,
    val totalIncome: Int = 0,
    val totalBudgetGoals: Int = 0,
    val totalCategoriesCreated: Int = 0,
    val totalPhotosAdded: Int = 0,

    val budgetGoalsAchieved: Int = 0,  // For Goal Crusher
    val monthlyLoginDays: Int = 0,  // For Perfect Month
    val currentMonth: Int = 0,  // Track current month for reset
    val currentYear: Int = 0,  // Track current year for reset
    val totalNotesAdded: Int = 0
)