// File: com/pennywise/budgettracker/data/models/User.kt
// Reference: Android Developers (2024) 'Room Database - Defining entities'

package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String,
    val passwordHash: String   // hashed with SHA-256
)