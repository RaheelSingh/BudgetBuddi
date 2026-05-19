package group12.budgetbuddi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import group12.budgetbuddi.data.dao.BudgetGoalDao
import group12.budgetbuddi.data.dao.ExpenseDao
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import group12.budgetbuddi.data.entity.ExpenseEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetGoalEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

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

/**
 * reference:
 * developer.android.com (2026) 'Room persistence library - Database setup', Android Developers.
 * Available at: https://developer.android.com/training/data-storage/room/defining-data
 * Accessed: 29 April 2026.
 */