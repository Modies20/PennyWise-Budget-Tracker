// File: com/pennywise/budgettracker/DashboardActivity.kt
// Reference: Phillips, B. and Hardy, B. (2019) 'Android Programming: The Big Nerd Ranch Guide'

package com.pennywise.budgettracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<android.widget.TextView>(R.id.tvWelcome).text = "Hello, ${sessionManager.getUsername()}"

        loadBudgetProgress()

        findViewById<android.widget.Button>(R.id.btnSetBudget).setOnClickListener {
            startActivity(Intent(this, BudgetSettingsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnManageCategories).setOnClickListener {
            startActivity(Intent(this, CategoryManagementActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnViewExpenses).setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnCategoryTotals).setOnClickListener {
            startActivity(Intent(this, CategoryTotalsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadBudgetProgress() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val year = DateUtils.getCurrentYear()
            val month = DateUtils.getCurrentMonth()
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)

            val totalBudget = database.budgetDao().getTotalMonthlyBudget(userId, month, year)
            val totalSpent = database.expenseDao().getTotalSpentBetweenDates(userId, start, end) ?: 0.0
            val limit = totalBudget?.limitAmount ?: 1.0
            val percent = ((totalSpent / limit) * 100).toInt().coerceIn(0, 100)
            findViewById<android.widget.ProgressBar>(R.id.pbBudget).progress = percent
            findViewById<android.widget.TextView>(R.id.tvBudgetStatus).text = "Spent: R$totalSpent / R$limit"
        }
    }
}

