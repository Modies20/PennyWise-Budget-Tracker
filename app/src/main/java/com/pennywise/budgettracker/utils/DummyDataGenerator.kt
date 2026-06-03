package com.pennywise.budgettracker.utils

import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Category
import com.pennywise.budgettracker.data.models.Expense
import com.pennywise.budgettracker.data.models.Budget
import kotlinx.coroutines.flow.first

object DummyDataGenerator {
    suspend fun populateIfEmpty(database: AppDatabase, userId: Long) {
        if (userId == -1L) return
        
        val categories = database.categoryDao().getCategoriesForUser(userId).first()
        if (categories.isEmpty()) {
            val foodId = database.categoryDao().insertCategory(Category(userId = userId, name = "Food", colorCode = "#E91E63"))
            val transportId = database.categoryDao().insertCategory(Category(userId = userId, name = "Transport", colorCode = "#2196F3"))
            val entertainmentId = database.categoryDao().insertCategory(Category(userId = userId, name = "Entertainment", colorCode = "#FF9800"))
            val healthId = database.categoryDao().insertCategory(Category(userId = userId, name = "Health", colorCode = "#4CAF50"))

            val now = System.currentTimeMillis()
            val month = DateUtils.getCurrentMonth()
            val year = DateUtils.getCurrentYear()

            // Insert Budgets
            database.budgetDao().insertBudget(Budget(userId = userId, categoryId = foodId, month = month, year = year, limitAmount = 1000.0))
            database.budgetDao().insertBudget(Budget(userId = userId, categoryId = transportId, month = month, year = year, limitAmount = 500.0))
            database.budgetDao().insertBudget(Budget(userId = userId, categoryId = entertainmentId, month = month, year = year, limitAmount = 300.0))
            database.budgetDao().insertBudget(Budget(userId = userId, categoryId = null, month = month, year = year, limitAmount = 3000.0)) // Total budget (categoryId IS NULL)

            // Insert Expenses
            database.expenseDao().insertExpense(Expense(userId = userId, categoryId = foodId, amount = 450.0, date = now, description = "Grocery Shopping"))
            database.expenseDao().insertExpense(Expense(userId = userId, categoryId = transportId, amount = 120.0, date = now - 86400000, description = "Fuel"))
            database.expenseDao().insertExpense(Expense(userId = userId, categoryId = entertainmentId, amount = 350.0, date = now, description = "Netflix & Cinema"))
            database.expenseDao().insertExpense(Expense(userId = userId, categoryId = healthId, amount = 200.0, date = now - 172800000, description = "Pharmacy"))

            // Insert starter badges
            database.achievementDao().insertAchievement(
                com.pennywise.budgettracker.data.models.Achievement(
                    userId = userId,
                    name = "PennyWise Explorer",
                    description = "Started your journey to better budgeting!",
                    earnedDate = now - 3600000,
                    iconResId = com.pennywise.budgettracker.R.drawable.ic_badge
                )
            )
            database.achievementDao().insertAchievement(
                com.pennywise.budgettracker.data.models.Achievement(
                    userId = userId,
                    name = "First Expense",
                    description = "Successfully logged your first transaction.",
                    earnedDate = now,
                    iconResId = com.pennywise.budgettracker.R.drawable.ic_badge
                )
            )
        }
    }
}