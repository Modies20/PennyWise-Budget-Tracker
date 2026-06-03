package com.pennywise.budgettracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Achievement
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import android.view.ViewGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AchievementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val database = AppDatabase.getInstance(this)
        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            // Check and award achievements on the fly
            val year = DateUtils.getCurrentYear()
            val month = DateUtils.getCurrentMonth()
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)

            val totalSpent = database.expenseDao().getTotalSpentBetweenDates(userId, start, end) ?: 0.0
            val totalBudget = database.budgetDao().getTotalMonthlyBudget(userId, month, year)
            if (totalBudget != null && totalSpent <= totalBudget.maxAmount) {
                val existing = database.achievementDao().getAchievementsForUser(userId).first().any { it.name == "Budget Beginner" }
                if (existing != true) {
                    database.achievementDao().insertAchievement(Achievement(userId = userId, name = "Budget Beginner",
                        description = "Stayed under max budget for the month", earnedDate = System.currentTimeMillis(), iconResId = R.drawable.ic_badge))
                    Toast.makeText(this@AchievementsActivity, "New badge: Budget Beginner!", Toast.LENGTH_LONG).show()
                }
            }

            val allAchievements = database.achievementDao().getAchievementsForUser(userId).first()
            findViewById<RecyclerView>(R.id.rvAchievements).apply {
                layoutManager = LinearLayoutManager(this@AchievementsActivity)
                adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                        return object : RecyclerView.ViewHolder(view) {}
                    }
                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val a = allAchievements[position]
                        holder.itemView.findViewById<android.widget.TextView>(android.R.id.text1).text = a.name
                        holder.itemView.findViewById<android.widget.TextView>(android.R.id.text2).text = a.description
                    }
                    override fun getItemCount() = allAchievements.size
                }
            }
        }
    }
}