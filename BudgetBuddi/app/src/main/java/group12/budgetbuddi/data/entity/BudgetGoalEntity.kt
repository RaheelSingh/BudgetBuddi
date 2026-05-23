package group12.budgetbuddi.data.entity

data class BudgetGoalEntity(
    val id: String = "",
    val userId: String = "",
    val month: Int = 0,
    val year: Int = 0,
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)