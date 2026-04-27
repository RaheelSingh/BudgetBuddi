package group12.budgetbuddi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mainCategory: String,
    val subCategory: String,
    val visualType: String,
    val visualValue: String,
    val outlineColor: String
)