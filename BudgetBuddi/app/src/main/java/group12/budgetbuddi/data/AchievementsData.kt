package group12.budgetbuddi.data

import group12.budgetbuddi.data.entity.AchievementEntity

object AchievementsData {

    // REPLACE with your Supabase bucket URL
    private const val BADGE_BASE_URL = "https://raygwqqsmrwtlaviqvei.supabase.co/storage/v1/object/public/achievement-badges/"

    val achievements = listOf(
        AchievementEntity(
            id = "first_expense",
            name = "First Drop",
            description = "Add your first expense",
            badgeUrl = "${BADGE_BASE_URL}achievement_first_expense.png",
            xpReward = 50,
            tier = 1,
            criteriaType = "expense_count",
            criteriaValue = 1
        ),
        AchievementEntity(
            id = "first_income",
            name = "First Flow",
            description = "Add your first income",
            badgeUrl = "${BADGE_BASE_URL}achievement_first_income.png",
            xpReward = 50,
            tier = 1,
            criteriaType = "income_count",
            criteriaValue = 1
        ),
        AchievementEntity(
            id = "first_budget_goal",
            name = "Goal Setter",
            description = "Create your first budget goal",
            badgeUrl = "${BADGE_BASE_URL}achievement_first_budget_goal.png",
            xpReward = 75,
            tier = 1,
            criteriaType = "budget_goal_created",
            criteriaValue = 1
        ),
        AchievementEntity(
            id = "first_category",
            name = "Organizer",
            description = "Create your own category",
            badgeUrl = "${BADGE_BASE_URL}achievement_first_category.png",
            xpReward = 50,
            tier = 1,
            criteriaType = "category_created",
            criteriaValue = 1
        ),
        AchievementEntity(
            id = "first_photo",
            name = "Snapshot",
            description = "Add a photo to a transaction",
            badgeUrl = "${BADGE_BASE_URL}achievement_first_photo.png",
            xpReward = 25,
            tier = 1,
            criteriaType = "transaction_with_photo",
            criteriaValue = 1
        )
    )

    fun getAchievementById(id: String): AchievementEntity? = achievements.find { it.id == id }

    // Calculate level based on XP (increasing XP requirements)
    fun calculateLevel(xp: Int): Int {
        return when {
            xp >= 6700 -> 15
            xp >= 5700 -> 14
            xp >= 4800 -> 13
            xp >= 4000 -> 12
            xp >= 3300 -> 11
            xp >= 2700 -> 10
            xp >= 2200 -> 9
            xp >= 1750 -> 8
            xp >= 1350 -> 7
            xp >= 1000 -> 6
            xp >= 700 -> 5
            xp >= 450 -> 4
            xp >= 250 -> 3
            xp >= 100 -> 2
            else -> 1
        }
    }

    fun getXpRequiredForLevel(level: Int): Int {
        return when (level) {
            2 -> 100
            3 -> 250
            4 -> 450
            5 -> 700
            6 -> 1000
            7 -> 1350
            8 -> 1750
            9 -> 2200
            10 -> 2700
            11 -> 3300
            12 -> 4000
            13 -> 4800
            14 -> 5700
            15 -> 6700
            else -> 0
        }
    }

    fun getXpToNextLevel(currentXP: Int, currentLevel: Int): Int {
        val nextLevelXP = getXpRequiredForLevel(currentLevel + 1)
        return if (nextLevelXP > 0) nextLevelXP - currentXP else 0
    }

    fun getLevelProgress(currentXP: Int, currentLevel: Int): Float {
        val currentLevelXP = getXpRequiredForLevel(currentLevel)
        val nextLevelXP = getXpRequiredForLevel(currentLevel + 1)

        if (nextLevelXP == 0) return 1f

        val xpInCurrentLevel = currentXP - currentLevelXP
        val xpNeededForNext = nextLevelXP - currentLevelXP

        return if (xpNeededForNext > 0) xpInCurrentLevel.toFloat() / xpNeededForNext else 1f
    }

    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Novice Budgeter"
            2 -> "Penny Pincher"
            3 -> "Saver Starter"
            4 -> "Cash Conscious"
            5 -> "Budget Buddy"
            6 -> "Money Minder"
            7 -> "Finance Fighter"
            8 -> "Wealth Watcher"
            9 -> "Dollar Defender"
            10 -> "Savings Samurai"
            11 -> "Budget Baron"
            12 -> "Money Master"
            13 -> "Finance Guru"
            14 -> "Wealth Wizard"
            15 -> "Budget Legend"
            else -> "Financial Master"
        }
    }
}