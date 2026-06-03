// Reference: Android Developers (2024) 'Room Database entities'
package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String
)