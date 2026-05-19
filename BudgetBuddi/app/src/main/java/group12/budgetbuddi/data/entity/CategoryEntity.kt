package group12.budgetbuddi.data.entity

/*
Author: Firebase / Google Developers
Description: Used as a plain Kotlin data model for category documents stored in Cloud Firestore.
Reference: Firebase. 2026. Cloud Firestore data model. [Online].
Available at: https://firebase.google.com/docs/firestore/data-model
Accessed: 19 May 2026.
*/

data class CategoryEntity(
    val id: String = "",
    val userId: String = "",
    val mainCategory: String = "",
    val subCategory: String = "",
    val visualType: String = "emoji",
    val visualValue: String = "🍳",
    val outlineColor: String = "#139A43",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)