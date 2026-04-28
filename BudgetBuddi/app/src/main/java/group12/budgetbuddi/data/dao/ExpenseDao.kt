package group12.budgetbuddi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import group12.budgetbuddi.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE userEmail = :userEmail ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(userEmail: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userEmail = :userEmail AND type = 'expense' ORDER BY date DESC, createdAt DESC")
    fun getExpensesOnly(userEmail: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userEmail = :userEmail AND type = 'income' ORDER BY date DESC, createdAt DESC")
    fun getIncomeOnly(userEmail: String): Flow<List<ExpenseEntity>>

}