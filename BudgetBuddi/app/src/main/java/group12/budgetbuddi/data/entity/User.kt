package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Reference:
 * developer.android.com (2026) 'Room persistence library - Defining entities', Android Developers.
 * Available at: https://developer.android.com/training/data-storage/room/defining-data
 * (Accessed: 29 April 2026).
 */