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
    ): RegisterResult = withContext(Dispatchers.IO) {
        // Check if email exists
        if (userDao.isEmailExists(email.lowercase()) > 0) {
            return@withContext RegisterResult(false, "Email already exists", null)
        }
        // Check if username exists
        if (userDao.isUsernameExists(username) > 0) {
            return@withContext RegisterResult(false, "Username already exists", null)
        }
        // Validate email format
        if (!PasswordUtils.isValidEmail(email)) {
            return@withContext RegisterResult(false, "Invalid email format", null)
        }

        // NEW: Validate password strength with comprehensive rules
        val passwordValidation = PasswordUtils.isValidPassword(password)
        if (!passwordValidation.isValid) {
            return@withContext RegisterResult(false, passwordValidation.errors.first(), null)
        }

        // Create user
        val passwordHash = PasswordUtils.createStoredHash(password)
        val user = User(
            email = email.lowercase(),
            username = username,
            passwordHash = passwordHash
        )
        val userId = userDao.insertUser(user)
        if (userId > 0) {
            return@withContext RegisterResult(true, "Registration successful", user)
        } else {
            return@withContext RegisterResult(false, "Registration failed", null)
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
        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours
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

        // NEW: Validate new password strength with comprehensive rules
        val passwordValidation = PasswordUtils.isValidPassword(newPassword)
        if (!passwordValidation.isValid) {
            return@withContext ResetResult(false, passwordValidation.errors.first(), "", null)
        }

        val newPasswordHash = PasswordUtils.createStoredHash(newPassword)
        userDao.updatePassword(resetToken.email, newPasswordHash)
        userDao.deleteResetTokensForEmail(resetToken.email)
        return@withContext ResetResult(true, "Password reset successful", "", null)
    }
}


data class RegisterResult(
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