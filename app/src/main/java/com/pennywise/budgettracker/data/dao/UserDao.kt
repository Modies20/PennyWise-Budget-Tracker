// File: com/pennywise/budgettracker/data/dao/UserDao.kt
// Reference: Android Developers (2024) 'Room Database - Data Access Objects'

package com.pennywise.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pennywise.budgettracker.data.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun loginUser(username: String, passwordHash: String): User?
}