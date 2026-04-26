package group12.budgetbuddi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import group12.budgetbuddi.data.entity.User
import group12.budgetbuddi.data.entity.PasswordResetToken
@Dao
interface UserDao {
    // Get a user by their email
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    // Insert a new user
    @Insert
    suspend fun insertUser(user: User): Long

    // Update user's password
    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String)

    // Check if email already exists (returns 0 or 1+)
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int

    // Check if username already exists
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int

    // Save a reset token for a user
    @Insert
    suspend fun insertResetToken(token: PasswordResetToken)

    // Get a valid token (not expired)
    @Query("SELECT * FROM password_reset_tokens WHERE token = :token AND expiresAt > :currentTime")
    suspend fun getValidResetToken(token: String, currentTime: Long): PasswordResetToken?

    // Delete all reset tokens for a specific email
    @Query("DELETE FROM password_reset_tokens WHERE email = :email")
    suspend fun deleteResetTokensForEmail(email: String)

    // Clean up expired tokens
    @Query("DELETE FROM password_reset_tokens WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredTokens(currentTime: Long)
}