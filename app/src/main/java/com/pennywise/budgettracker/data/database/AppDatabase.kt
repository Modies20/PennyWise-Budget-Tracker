// File: com/pennywise/budgettracker/data/database/AppDatabase.kt
// Reference: Android Developers (2024) 'Saving data with Room'
// Available at: https://developer.android.com/training/data-storage/room
// Accessed: 27 April 2026

package com.pennywise.budgettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pennywise.budgettracker.data.dao.UserDao
import com.pennywise.budgettracker.data.dao.CategoryDao
import com.pennywise.budgettracker.data.dao.ExpenseDao
import com.pennywise.budgettracker.data.dao.BudgetDao
import com.pennywise.budgettracker.data.models.User
import com.pennywise.budgettracker.data.models.Category
import com.pennywise.budgettracker.data.models.Expense
import com.pennywise.budgettracker.data.models.Budget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    "pennywise_database"
                ).addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }

            suspend fun populateDefaultData(database: AppDatabase) {
                val defaultCategories = listOf(
                    Category(name = "Groceries", colorCode = "#4CAF50", isDefault = true, userId = 1),
                    Category(name = "Transport", colorCode = "#2196F3", isDefault = true, userId = 1),
                    Category(name = "Entertainment", colorCode = "#FF9800", isDefault = true, userId = 1),
                    Category(name = "Utilities", colorCode = "#9C27B0", isDefault = true, userId = 1),
                    Category(name = "Dining Out", colorCode = "#F44336", isDefault = true, userId = 1),
                    Category(name = "Shopping", colorCode = "#E91E63", isDefault = true, userId = 1),
                    Category(name = "Health", colorCode = "#00BCD4", isDefault = true, userId = 1),
                    Category(name = "Education", colorCode = "#FF5722", isDefault = true, userId = 1)
                )

                for (category in defaultCategories) {
                    database.categoryDao().insertCategory(category)
                }
            }
        }
    }
}