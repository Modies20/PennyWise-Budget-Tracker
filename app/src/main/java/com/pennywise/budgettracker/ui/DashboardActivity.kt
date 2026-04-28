// File: com/pennywise/budgettracker/DashboardActivity.kt
// Reference: Phillips, B. and Hardy, B. (2019) 'Android Programming: The Big Nerd Ranch Guide'

package com.pennywise.budgettracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.adapters.CategorySpendingAdapter
import com.pennywise.budgettracker.adapters.CategoryWithSpending
import com.pennywise.budgettracker.adapters.RecentExpensesAdapter
import com.pennywise.budgettracker.adapters.ExpenseWithCategory
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryAdapter: CategorySpendingAdapter
    private lateinit var expenseAdapter: RecentExpensesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerViews()
        setupBottomNavigation()
        setupNavigationDrawer()

        loadDashboardData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvWelcome = findViewById<android.widget.TextView>(R.id.tvWelcome)
        val username = sessionManager.getUsername()
        tvWelcome.text = "Welcome back, $username!"
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategorySpendingAdapter { category ->
            // Navigate to category details
            Toast.makeText(this, "Viewing: ${category.name}", Toast.LENGTH_SHORT).show()
        }

        expenseAdapter = RecentExpensesAdapter { expense ->
            Toast.makeText(this, "${expense.categoryName}: R${expense.expense.amount}", Toast.LENGTH_SHORT).show()
        }

        val rvCategories = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = categoryAdapter

        val rvRecentExpenses = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRecentExpenses)
        rvRecentExpenses.layoutManager = LinearLayoutManager(this)
        rvRecentExpenses.adapter = expenseAdapter
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            if (userId == -1L) {
                // Not logged in, redirect to login
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
                return@launch
            }

            val currentYear = DateUtils.getCurrentYear()
            val currentMonth = DateUtils.getCurrentMonth()
            val startOfMonth = DateUtils.getStartOfMonth(currentYear, currentMonth)
            val endOfMonth = DateUtils.getEndOfMonth(currentYear, currentMonth)

            // Load budget data
            loadBudgetData(userId, currentMonth, currentYear, startOfMonth, endOfMonth)

            // Load category spending
            loadCategorySpending(userId, startOfMonth, endOfMonth)

            // Load recent expenses
            loadRecentExpenses(userId)
        }
    }

    private suspend fun loadBudgetData(userId: Long, month: Int, year: Int, startDate: Long, endDate: Long) {
        val totalBudget = database.budgetDao().getTotalMonthlyBudget(userId, month, year)
        val totalSpent = database.expenseDao().getTotalSpentBetweenDates(userId, startDate, endDate) ?: 0.0

        val budgetLimit = totalBudget?.limitAmount ?: 0.0
        val percentage = if (budgetLimit > 0) ((totalSpent / budgetLimit) * 100).toInt() else 0

        val tvBudgetStatus = findViewById<android.widget.TextView>(R.id.tvBudgetStatus)
        val pbBudget = findViewById<android.widget.ProgressBar>(R.id.pbBudget)

        tvBudgetStatus.text = String.format("Spent: R%.2f / R%.2f", totalSpent, budgetLimit)
        pbBudget.progress = percentage.coerceIn(0, 100)

        if (percentage > 100) {
            tvBudgetStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            Toast.makeText(this, "⚠️ Budget exceeded for this month!", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun loadCategorySpending(userId: Long, startDate: Long, endDate: Long) {
        val categories = database.categoryDao().getCategoriesForUser(userId).first()
        val categoryList = mutableListOf<CategoryWithSpending>()

        for (category in categories) {
            val spent = database.expenseDao().getTotalSpentByCategory(userId, category.categoryId, startDate, endDate) ?: 0.0
            val budget = database.budgetDao().getCategoryBudget(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear(), category.categoryId)

            categoryList.add(CategoryWithSpending(
                category = category,
                spent = spent,
                budgetLimit = budget?.limitAmount ?: 0.0
            ))
        }

        categoryAdapter.submitList(categoryList)
    }

    private suspend fun loadRecentExpenses(userId: Long) {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (30L * 24 * 60 * 60 * 1000)

        val expenses = database.expenseDao().getExpensesBetweenDates(userId, startDate, endDate).first()
        val expensesWithCategories = mutableListOf<ExpenseWithCategory>()

        for (expense in expenses.take(10)) {
            val category = database.categoryDao().getCategoryById(userId, expense.categoryId)
            expensesWithCategories.add(ExpenseWithCategory(expense, category?.name ?: "Unknown"))
        }

        expenseAdapter.submitList(expensesWithCategories)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_expenses -> {
                    // Navigate to expenses list
                    Toast.makeText(this, "Expenses list (Coming soon)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_categories -> {
                    Toast.makeText(this, "Categories (Coming soon)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_budget -> {
                    Toast.makeText(this, "Budget settings (Coming soon)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationDrawer() {
        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawerLayout)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> Toast.makeText(this, "Profile (Coming Soon)", Toast.LENGTH_SHORT).show()
                R.id.nav_achievements -> Toast.makeText(this, "Achievements (Final POE)", Toast.LENGTH_SHORT).show()
                R.id.nav_export -> Toast.makeText(this, "Export Data (Coming Soon)", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    sessionManager.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
