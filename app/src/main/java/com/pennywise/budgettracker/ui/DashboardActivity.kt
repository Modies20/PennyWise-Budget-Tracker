// File: com/pennywise/budgettracker/DashboardActivity.kt
// Reference: Phillips, B. and Hardy, B. (2019) 'Android Programming: The Big Nerd Ranch Guide'

package com.pennywise.budgettracker.ui



import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.adapters.CategorySpendingAdapter
import com.pennywise.budgettracker.adapters.CategoryWithSpending
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Category
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryAdapter: CategorySpendingAdapter

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

        findViewById<android.widget.TextView>(R.id.tvWelcome).text = "Welcome ${sessionManager.getUsername()}"

        categoryAdapter = CategorySpendingAdapter { category ->
            Toast.makeText(this, "Tap to edit ${category.name}", Toast.LENGTH_SHORT).show()
        }
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCategories).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = categoryAdapter
        }

        loadBudgetAndCategories()

        findViewById<android.widget.Button>(R.id.btnGraph).setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnProgressDashboard).setOnClickListener {
            startActivity(Intent(this, ProgressDashboardActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnSubscriptions).setOnClickListener {
            startActivity(Intent(this, SubscriptionDetectorActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadBudgetAndCategories() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val year = DateUtils.getCurrentYear()
            val month = DateUtils.getCurrentMonth()
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)

            val totalBudget = database.budgetDao().getTotalMonthlyBudget(userId, month, year)
            val totalSpent = database.expenseDao().getTotalSpentBetweenDates(userId, start, end) ?: 0.0
            val limit = totalBudget?.maxAmount ?: 1.0
            val percent = ((totalSpent / limit) * 100).toInt().coerceIn(0, 100)
            findViewById<android.widget.ProgressBar>(R.id.pbBudget).progress = percent
            findViewById<android.widget.TextView>(R.id.tvBudgetStatus).text = "Spent: R$totalSpent / R$limit"

            database.categoryDao().getCategoriesForUser(userId).collect { categories ->
                val categoryList = categories.map { cat ->
                    val spent = database.expenseDao().getTotalSpentByCategory(userId, cat.categoryId, start, end) ?: 0.0
                    val budget = database.budgetDao().getCategoryBudget(userId, month, year, cat.categoryId)
                    CategoryWithSpending(cat, spent, budget?.maxAmount ?: 0.0)
                }
                categoryAdapter.submitList(categoryList)
            }
        }
    }
}
