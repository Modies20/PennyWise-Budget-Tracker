package com.pennywise.budgettracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.SessionManager
import android.view.ViewGroup
import kotlinx.coroutines.launch

data class Subscription(val description: String, val amount: Double, val occurrences: Int)

class SubscriptionDetectorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription_detector)

        val database = AppDatabase.getInstance(this)
        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val expenses = database.expenseDao().getAllExpenses(userId)
            val grouped = expenses.groupBy { it.description.lowercase().trim() }
            val subscriptions = mutableListOf<Subscription>()
            for ((desc, list) in grouped) {
                if (list.size >= 2) {
                    val uniqueAmounts = list.map { it.amount }.distinct()
                    if (uniqueAmounts.size == 1) {
                        subscriptions.add(Subscription(desc, uniqueAmounts.first(), list.size))
                    }
                }
            }

            val rv = findViewById<RecyclerView>(R.id.rvSubscriptions)
            rv.layoutManager = LinearLayoutManager(this@SubscriptionDetectorActivity)
            rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val sub = subscriptions[position]
                    holder.itemView.findViewById<android.widget.TextView>(android.R.id.text1).text = sub.description
                    holder.itemView.findViewById<android.widget.TextView>(android.R.id.text2).text = "R${sub.amount} - ${sub.occurrences} times"
                }
                override fun getItemCount() = subscriptions.size
            }
            if (subscriptions.isEmpty()) Toast.makeText(this@SubscriptionDetectorActivity, "No subscriptions detected", Toast.LENGTH_SHORT).show()
        }
    }
}