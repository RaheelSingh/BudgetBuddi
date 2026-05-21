package group12.budgetbuddi.data.entity

data class ExpenseEntity(
    val id: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val subCategory: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val note: String = "",
    val photoPath: String? = null,
    val paymentMethod: String = "",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)