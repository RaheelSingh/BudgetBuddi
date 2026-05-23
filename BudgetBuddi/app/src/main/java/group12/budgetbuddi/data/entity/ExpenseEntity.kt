package group12.budgetbuddi.data.entity

data class ExpenseEntity(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "expense" or "income"
    val category: String = "",
    val subCategory: String = "",
    val date: String = "",
    val note: String = "",
    val photoPath: String? = null,
    val paymentMethod: String = "",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)