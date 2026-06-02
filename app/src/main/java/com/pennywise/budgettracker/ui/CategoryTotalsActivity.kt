package com.pennywise.budgettracker.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*

class CategoryTotalsActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var startDate = 0L
    private var endDate = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_totals)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        val cal = Calendar.getInstance()
        startDate = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1)
        endDate = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1)

        findViewById<android.widget.Button>(R.id.btnTotalsStartDate).setOnClickListener { pickDate { startDate = it; loadTotals() } }
        findViewById<android.widget.Button>(R.id.btnTotalsEndDate).setOnClickListener { pickDate { endDate = it; loadTotals() } }
        findViewById<android.widget.Button>(R.id.btnLoadTotals).setOnClickListener { loadTotals() }
        loadTotals()
    }

    private fun pickDate(onResult: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onResult(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadTotals() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            database.categoryDao().getCategoriesForUser(userId).collect { categories ->
                val spendingMap = mutableMapOf<String, Double>()
                for (cat in categories) {
                    val spent = database.expenseDao().getTotalSpentByCategory(userId, cat.categoryId, startDate, endDate) ?: 0.0
                    spendingMap[cat.name] = spent
                }
                val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    private val items = spendingMap.entries.toList()
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                        return object : RecyclerView.ViewHolder(view) {}
                    }
                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val (name, total) = items[position]
                        holder.itemView.findViewById<android.widget.TextView>(android.R.id.text1).text = name
                        holder.itemView.findViewById<android.widget.TextView>(android.R.id.text2).text = "R${DecimalFormat("#,##0.00").format(total)}"
                    }
                    override fun getItemCount() = items.size
                }
                findViewById<RecyclerView>(R.id.rvCategoryTotals).layoutManager = LinearLayoutManager(this@CategoryTotalsActivity)
                findViewById<RecyclerView>(R.id.rvCategoryTotals).adapter = adapter
            }
        }
    }
}
