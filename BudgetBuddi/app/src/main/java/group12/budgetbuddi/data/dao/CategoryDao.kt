package group12.budgetbuddi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import group12.budgetbuddi.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("""
        SELECT * FROM categories
        WHERE userEmail = :userEmail
        ORDER BY mainCategory ASC, subCategory ASC
    """)
    fun getAllCategories(userEmail: String): Flow<List<CategoryEntity>>

    @Query("""
        SELECT * FROM categories
        WHERE userEmail = :userEmail
        AND mainCategory = :mainCategory
        ORDER BY subCategory ASC
    """)
    fun getSubCategoriesForMainCategory(
        userEmail: String,
        mainCategory: String
    ): Flow<List<CategoryEntity>>

    @Query("""
        SELECT DISTINCT mainCategory FROM categories
        WHERE userEmail = :userEmail
        ORDER BY mainCategory ASC
    """)
    fun getMainCategories(userEmail: String): Flow<List<String>>

    @Query("""
        SELECT * FROM categories
        WHERE userEmail = :userEmail
        AND (
            mainCategory LIKE '%' || :searchQuery || '%'
            OR subCategory LIKE '%' || :searchQuery || '%'
        )
        ORDER BY mainCategory ASC, subCategory ASC
    """)
    fun searchCategories(
        userEmail: String,
        searchQuery: String
    ): Flow<List<CategoryEntity>>

    @Query("""
        DELETE FROM categories
        WHERE userEmail = :userEmail
        AND mainCategory = :mainCategory
    """)
    suspend fun deleteWholeMainCategory(
        userEmail: String,
        mainCategory: String
    )

    @Query("""
        SELECT COUNT(*) FROM categories
        WHERE userEmail = :userEmail
    """)
    suspend fun getCategoryCountForUser(userEmail: String): Int
}