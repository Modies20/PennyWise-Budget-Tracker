package com.pennywise.budgettracker.data.dao

import androidx.room.*
import com.pennywise.budgettracker.data.models.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert
    suspend fun insertAchievement(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedDate DESC")
    fun getAchievementsForUser(userId: Long): Flow<List<Achievement>>
}