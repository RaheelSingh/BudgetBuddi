package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val amount: Double,
    val type: String,
    val category: String,
    val subCategory: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val note: String,
    val photoPath: String?,
    val paymentMethod: String,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)