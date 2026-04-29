package group12.budgetbuddi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import group12.budgetbuddi.data.dao.BudgetGoalDao
import group12.budgetbuddi.data.dao.CategoryDao
import group12.budgetbuddi.data.dao.ExpenseDao
import group12.budgetbuddi.data.dao.UserDao
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.CategoryEntity
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.data.entity.PasswordResetToken
import group12.budgetbuddi.data.entity.User

@Database(
    entities = [
        User::class,
        PasswordResetToken::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetGoalEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_buddi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}