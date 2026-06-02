// File: com/pennywise/budgettracker/data/dao/BudgetDao.kt
// Reference: Google (2024) 'Room Persistence Library Documentation'

package com.pennywise.budgettracker.data.dao

import androidx.room.*
import com.pennywise.budgettracker.data.models.Budget

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getBudgetsForMonth(userId: Long, month: Int, year: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year AND categoryId IS NULL")
    suspend fun getTotalMonthlyBudget(userId: Long, month: Int, year: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year AND categoryId = :categoryId")
    suspend fun getCategoryBudget(userId: Long, month: Int, year: Int, categoryId: Long): Budget?
}