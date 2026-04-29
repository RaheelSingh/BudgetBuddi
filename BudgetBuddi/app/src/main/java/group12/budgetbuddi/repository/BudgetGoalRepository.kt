package group12.budgetbuddi.repository

import group12.budgetbuddi.data.dao.BudgetGoalDao
import group12.budgetbuddi.data.entity.BudgetGoalEntity
import kotlinx.coroutines.flow.Flow

class BudgetGoalRepository(private val budgetGoalDao: BudgetGoalDao) {

    suspend fun saveGoal(goal: BudgetGoalEntity) =
        budgetGoalDao.insertOrReplace(goal)

    fun getGoalForMonth(userEmail: String, month: Int, year: Int): Flow<BudgetGoalEntity?> =
        budgetGoalDao.getGoalForMonth(userEmail, month, year)

    fun getAllGoals(userEmail: String): Flow<List<BudgetGoalEntity>> =
        budgetGoalDao.getAllGoals(userEmail)
}