package group12.budgetbuddi.data

import group12.budgetbuddi.data.entity.AchievementEntity

object AchievementsData {

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
        ),
        AchievementEntity(
            id = "streak_7",
            name = "Week Warrior",
            description = "Login for 7 days in a row",
            badgeUrl = "${BADGE_BASE_URL}badge_streak_7.png",
            xpReward = 100,
            tier = 2,
            criteriaType = "streak",
            criteriaValue = 7
        ),
        AchievementEntity(
            id = "streak_30",
            name = "Monthly Master",
            description = "Login for 30 days in a row",
            badgeUrl = "${BADGE_BASE_URL}badge_streak_30.png",
            xpReward = 500,
            tier = 2,
            criteriaType = "streak",
            criteriaValue = 30
        ),
        AchievementEntity(
            id = "expense_master_10",
            name = "Budget Beginner",
            description = "Track 10 expenses",
            badgeUrl = "${BADGE_BASE_URL}badge_expense_10.png",
            xpReward = 100,
            tier = 2,
            criteriaType = "expense_count",
            criteriaValue = 10
        ),
        AchievementEntity(
            id = "expense_master_25",
            name = "Budget Builder",
            description = "Track 25 expenses",
            badgeUrl = "${BADGE_BASE_URL}badge_expense_25.png",
            xpReward = 150,
            tier = 2,
            criteriaType = "expense_count",
            criteriaValue = 25
        ),
        AchievementEntity(
            id = "income_master_10",
            name = "Earning Early",
            description = "Track 10 income entries",
            badgeUrl = "${BADGE_BASE_URL}badge_income_10.png",
            xpReward = 100,
            tier = 2,
            criteriaType = "income_count",
            criteriaValue = 10
        ),
        AchievementEntity(
            id = "saver_1000",
            name = "Saver",
            description = "Save R1000 in a single month",
            badgeUrl = "${BADGE_BASE_URL}badge_saver_1000.png",
            xpReward = 200,
            tier = 3,
            criteriaType = "monthly_saved",
            criteriaValue = 1000
        ),
        AchievementEntity(
            id = "budget_stay_under",
            name = "Budget Master",
            description = "Stay under budget across ALL categories for a month",
            badgeUrl = "${BADGE_BASE_URL}badge_budget_master.png",
            xpReward = 300,
            tier = 3,
            criteriaType = "all_categories_under_budget",
            criteriaValue = 1
        ),
        AchievementEntity(
            id = "goal_achiever_3",
            name = "Goal Crusher",
            description = "Achieve 3 budget goals",
            badgeUrl = "${BADGE_BASE_URL}badge_goal_crusher.png",
            xpReward = 200,
            tier = 3,
            criteriaType = "budget_goals_achieved",
            criteriaValue = 3
        ),
        AchievementEntity(
            id = "perfect_month",
            name = "Perfect Month",
            description = "Log every single day of a month",
            badgeUrl = "${BADGE_BASE_URL}badge_perfect_month.png",
            xpReward = 1000,
            tier = 3,
            criteriaType = "monthly_login_completion",
            criteriaValue = 100
        ),
        AchievementEntity(
            id = "category_food_master",
            name = "Foodie",
            description = "Add 30 expenses in Food category",
            badgeUrl = "${BADGE_BASE_URL}badge_foodie.png",
            xpReward = 150,
            tier = 3,
            criteriaType = "category_expense_count",
            criteriaValue = 30,
            criteriaCategory = "Food"
        )
    )

    fun getAchievementById(id: String): AchievementEntity? = achievements.find { it.id == id }

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

        if (nextLevelXP <= 0) {
            return 0
        }

        return (nextLevelXP - currentXP).coerceAtLeast(0)
    }

    fun getLevelProgress(currentXP: Int, currentLevel: Int): Float {
        val nextLevelXP = getXpRequiredForLevel(currentLevel + 1)

        if (nextLevelXP <= 0) {
            return 1f
        }

        return (currentXP.toFloat() / nextLevelXP.toFloat()).coerceIn(0f, 1f)
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
