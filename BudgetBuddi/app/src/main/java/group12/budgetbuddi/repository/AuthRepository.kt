package group12.budgetbuddi.repository

import android.content.Context
import group12.budgetbuddi.data.database.AppDatabase
import group12.budgetbuddi.data.entity.PasswordResetToken
import group12.budgetbuddi.data.entity.User
import group12.budgetbuddi.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val userDao = database.userDao()

    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.lowercase())
        if (user == null) {
            return@withContext LoginResult(false, "User not found", null)
        }
        if (PasswordUtils.verifyPassword(password, user.passwordHash)) {
            return@withContext LoginResult(true, "Login successful", user)
        } else {
            return@withContext LoginResult(false, "Wrong password", null)
        }
    }
}

data class LoginResult(
    val isSuccess: Boolean,
    val message: String,
    val user: User?
)