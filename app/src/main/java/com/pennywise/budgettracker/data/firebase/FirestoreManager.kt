package com.pennywise.budgettracker.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.pennywise.budgettracker.data.models.*
import kotlinx.coroutines.tasks.await

class FirestoreManager(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("users").document(userId).collection("expenses")
    private val categoriesCollection = db.collection("users").document(userId).collection("categories")
    private val budgetsCollection = db.collection("users").document(userId).collection("budgets")
    private val achievementsCollection = db.collection("users").document(userId).collection("achievements")

    // Expenses
    suspend fun uploadExpense(expense: Expense) {
        try {
            expensesCollection.document(expense.expenseId.toString()).set(expense).await()
            Log.d("Firestore", "Expense uploaded: ${expense.expenseId}")
        } catch (e: Exception) {
            Log.e("Firestore", "Upload expense failed", e)
        }
    }

    suspend fun getAllExpenses(): List<Expense> {
        return try {
            val snapshot = expensesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
        } catch (e: Exception) {
            Log.e("Firestore", "Get expenses failed", e)
            emptyList()
        }
    }

    suspend fun deleteExpense(expenseId: Long) {
        try {
            expensesCollection.document(expenseId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("Firestore", "Delete expense failed", e)
        }
    }

    // Categories
    suspend fun uploadCategory(category: Category) {
        categoriesCollection.document(category.categoryId.toString()).set(category).await()
    }

    suspend fun getAllCategories(): List<Category> {
        val snapshot = categoriesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
    }

    // Budgets
    suspend fun uploadBudget(budget: Budget) {
        budgetsCollection.document(budget.budgetId.toString()).set(budget).await()
    }

    suspend fun getAllBudgets(): List<Budget> {
        val snapshot = budgetsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Budget::class.java) }
    }

    // Achievements
    suspend fun uploadAchievement(achievement: Achievement) {
        achievementsCollection.document(achievement.id.toString()).set(achievement).await()
    }

    suspend fun getAllAchievements(): List<Achievement> {
        val snapshot = achievementsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Achievement::class.java) }
    }
}