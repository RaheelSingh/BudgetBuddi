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



    suspend fun register(
        email: String,
        username: String,
        password: String
    ): RegistrationResult = withContext(Dispatchers.IO) {

        if (userDao.isEmailExists(email.lowercase()) > 0) {
            return@withContext RegistrationResult(false, "Email already exists", null)
        }

        if (userDao.isUsernameExists(username) > 0) {
            return@withContext RegistrationResult(false, "Username already exists", null)
        }

        if (!PasswordUtils.isValidEmail(email)) {
            return@withContext RegistrationResult(false, "Invalid email format", null)
        }

        if (!PasswordUtils.isStrongPassword(password)) {
            return@withContext RegistrationResult(false, "Password must be at least 6 characters", null)
        }

        val passwordHash = PasswordUtils.createStoredHash(password)
        val user = User(
            email = email.lowercase(),
            username = username,
            passwordHash = passwordHash
        )

        val userId = userDao.insertUser(user)

        if (userId > 0) {
            return@withContext RegistrationResult(true, "Registration successful", null)
        } else {
            return@withContext RegistrationResult(false, "Registration failed", null)
        }
    }


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


    suspend fun sendPasswordResetCode(email: String): ResetResult = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.lowercase())

        if (user == null) {
            return@withContext ResetResult(false, "Email not found", "", null)
        }

        val token = PasswordUtils.generateResetToken()
        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)

        userDao.deleteResetTokensForEmail(email.lowercase())

        val resetToken = PasswordResetToken(
            email = email.lowercase(),
            token = token,
            expiresAt = expiresAt
        )
        userDao.insertResetToken(resetToken)

        return@withContext ResetResult(true, "Reset code generated", token, null)
    }


    suspend fun resetPassword(token: String, newPassword: String): ResetResult = withContext(Dispatchers.IO) {
        userDao.deleteExpiredTokens(System.currentTimeMillis())

        val resetToken = userDao.getValidResetToken(token, System.currentTimeMillis())

        if (resetToken == null) {
            return@withContext ResetResult(false, "Invalid or expired reset code", "", null)
        }

        if (!PasswordUtils.isStrongPassword(newPassword)) {
            return@withContext ResetResult(false, "Password must be at least 6 characters", "", null)
        }

        val newPasswordHash = PasswordUtils.createStoredHash(newPassword)
        userDao.updatePassword(resetToken.email, newPasswordHash)
        userDao.deleteResetTokensForEmail(resetToken.email)

        return@withContext ResetResult(true, "Password reset successful", "", null)
    }
}

data class RegistrationResult(
    val isSuccess: Boolean,
    val message: String,
    val user: User?
)

data class LoginResult(
    val isSuccess: Boolean,
    val message: String,
    val user: User?
)

data class ResetResult(
    val isSuccess: Boolean,
    val message: String,
    val token: String,
    val error: String?
)