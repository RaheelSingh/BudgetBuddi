package group12.budgetbuddi.data.entity

data class AchievementEntity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val badgeUrl: String = "",
    val xpReward: Int = 0,
    val tier: Int = 1,
    val criteriaType: String = "",
    val criteriaValue: Int = 0,
    val criteriaCategory: String? = null
)