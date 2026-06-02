// File: com/pennywise/budgettracker/data/models/Category.kt
// Reference: Griffiths, D. (2021) 'Head First Android Development'

package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long = 0,
    val userId: Long,
    val name: String,
    val colorCode: String = "#4CAF50"
)