// File: com/pennywise/budgettracker/data/dao/ExpenseDao.kt
// Reference: Roman, E. (2018) 'Mastering Android Development'

package com.pennywise.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.pennywise.budgettracker.data.models.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getExpensesBetweenDates(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Expense>>

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalSpentByCategory(
        userId: Long,
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Double?

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalSpentBetweenDates(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): Double?

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    suspend fun getSpendingByCategoryGrouped(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): List<CategorySpending>
}

// Helper data class for grouped results
data class CategorySpending(
    val categoryId: Long,
    val total: Double
)