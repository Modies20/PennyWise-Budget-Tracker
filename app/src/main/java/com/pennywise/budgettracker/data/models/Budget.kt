// File: com/pennywise/budgettracker/data/models/Budget.kt
// Reference: Phillips and Stewart (2021) 'Kotlin for Android Development'

package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Long = 0,
    val userId: Long,
    val categoryId: Long? = null,    // null means total monthly budget
    val month: Int,                   // 1-12 (January = 1)
    val year: Int,
    val limitAmount: Double
)