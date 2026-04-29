package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * References:
 * - Android Room Entities: https://developer.android.com/training/data-storage/room/defining-data
 */

@Entity(tableName = "budget_goals")
data class BudgetGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val month: Int,       // 1–12
    val year: Int,
    val minGoal: Double,
    val maxGoal: Double
)