package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String,
    val earnedDate: Long,
    val iconResId: Int
)