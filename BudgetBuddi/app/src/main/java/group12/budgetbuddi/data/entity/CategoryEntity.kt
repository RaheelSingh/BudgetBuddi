package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Author: Android Developers
Description: Used as reference for implementing a Room entity with a table name and auto-generated primary key.
Reference: Android Developers. 2019. Save data in a local database using Room  |  Android Developers. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 29 April 2026].
*/

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val mainCategory: String,
    val subCategory: String,
    val visualType: String,
    val visualValue: String,
    val outlineColor: String
)