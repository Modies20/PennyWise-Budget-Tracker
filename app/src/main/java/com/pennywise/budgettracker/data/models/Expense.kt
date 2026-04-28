// File: com/pennywise/budgettracker/data/models/Expense.kt
// Reference: Phillips, B. and Hardy, B. (2019) 'Android Programming: The Big Nerd Ranch Guide'

package com.pennywise.budgettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val date: Long,                    // Timestamp in milliseconds
    val description: String,
    val receiptImagePath: String? = null   // URI or file path of receipt photo
)