// File: com/pennywise/budgettracker/data/database/AppDatabase.kt
// Reference: Android Developers (2024) 'Saving data with Room'
// Available at: https://developer.android.com/training/data-storage/room
// Accessed: 27 April 2026

package com.pennywise.budgettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pennywise.budgettracker.data.dao.*
import com.pennywise.budgettracker.data.models.*

@Database(
    entities = [User::class, Category::class, Expense::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pennywise_prototype"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}