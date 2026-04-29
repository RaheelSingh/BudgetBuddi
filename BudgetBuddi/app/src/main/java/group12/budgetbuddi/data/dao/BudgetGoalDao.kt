package group12.budgetbuddi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * References:
 * - Android Room DAO: https://developer.android.com/training/data-storage/room/accessing-data
 * - Room Delete-Insert Pattern: https://developer.android.com/training/data-storage/room/relationships
 */
@Dao
interface BudgetGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(goal: BudgetGoalEntity)

    @Query("DELETE FROM budget_goals WHERE userEmail = :userEmail AND month = :month AND year = :year")
    suspend fun deleteGoalForMonth(userEmail: String, month: Int, year: Int)

    @Query("""
        SELECT * FROM budget_goals
        WHERE userEmail = :userEmail
        AND month = :month
        AND year = :year
        LIMIT 1
    """)
    fun getGoalForMonth(
        userEmail: String,
        month: Int,
        year: Int
    ): Flow<BudgetGoalEntity?>

    @Query("SELECT * FROM budget_goals WHERE userEmail = :userEmail ORDER BY year DESC, month DESC")
    fun getAllGoals(userEmail: String): Flow<List<BudgetGoalEntity>>
}