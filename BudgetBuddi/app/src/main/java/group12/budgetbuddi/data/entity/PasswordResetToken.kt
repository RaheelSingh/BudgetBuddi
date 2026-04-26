package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_reset_tokens")
data class PasswordResetToken(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val token: String,
    val expiresAt: Long
)