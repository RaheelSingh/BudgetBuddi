package group12.budgetbuddi.data.entity

data class BudgetGoalEntity(
    val id: String = "",
    val userId: String = "",
    val goalName: String = "",
    val categoryName: String = "",
    val subCategoryName: String = "",
    val goalType: String = "expense",
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)