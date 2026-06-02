// File: com/pennywise/budgettracker/data/dao/CategoryDao.kt
// Reference: Deitel and Deitel (2020) 'Android for Programmers'

package com.pennywise.budgettracker.data.dao

import androidx.room.*
import com.pennywise.budgettracker.data.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getCategoryById(userId: Long, categoryId: Long): Category?
}