package group12.budgetbuddi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import group12.budgetbuddi.data.entity.PasswordResetToken
import group12.budgetbuddi.data.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Insert
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String)

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int

    @Insert
    suspend fun insertResetToken(token: PasswordResetToken)

    @Query("SELECT * FROM password_reset_tokens WHERE token = :token AND expiresAt > :currentTime")
    suspend fun getValidResetToken(token: String, currentTime: Long): PasswordResetToken?

    @Query("DELETE FROM password_reset_tokens WHERE email = :email")
    suspend fun deleteResetTokensForEmail(email: String)

    @Query("DELETE FROM password_reset_tokens WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredTokens(currentTime: Long)
}