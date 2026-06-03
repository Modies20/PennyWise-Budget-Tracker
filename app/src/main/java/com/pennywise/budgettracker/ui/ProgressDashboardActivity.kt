package com.pennywise.budgettracker.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProgressDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_dashboard)

        val database = AppDatabase.getInstance(this)
        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val year = DateUtils.getCurrentYear()
            val month = DateUtils.getCurrentMonth()
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)

            val categories = database.categoryDao().getCategoriesForUser(userId).first()
            var maxCompliant = 0

            for (cat in categories) {
                val spent = database.expenseDao().getTotalSpentByCategory(userId, cat.categoryId, start, end) ?: 0.0
                val budget = database.budgetDao().getCategoryBudget(userId, month, year, cat.categoryId)
                if (budget != null) {
                    if (spent <= budget.maxAmount) maxCompliant++
                }
            }

            val maxPercent = if (categories.isNotEmpty()) (maxCompliant.toFloat() / categories.size) * 100 else 100f

            findViewById<ProgressBar>(R.id.progressMax).progress = maxPercent.toInt()
            findViewById<TextView>(R.id.tvMaxScore).text = "%.0f%% categories stay under maximum".format(maxPercent)

            val smiley = findViewById<ImageView>(R.id.ivMinSmiley)
            val overall = maxPercent
            smiley.setImageResource(
                when {
                    overall >= 80 -> R.drawable.ic_happy
                    overall >= 50 -> R.drawable.ic_neutral
                    else -> R.drawable.ic_sad
                }
            )
        }
    }
}