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
    val categoryId: Long? = null,
    val month: Int,
    val year: Int,
    val limitAmount: Double = 0.0
) {
    // Adding these properties for compatibility with existing code that uses maxAmount
    val maxAmount: Double get() = limitAmount
}