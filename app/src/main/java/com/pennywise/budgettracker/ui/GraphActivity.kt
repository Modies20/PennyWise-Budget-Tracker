package com.pennywise.budgettracker.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.LimitLine
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class GraphActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var startDate = 0L
    private var endDate = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        val calendar = Calendar.getInstance()
        startDate = DateUtils.getStartOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1)
        endDate = DateUtils.getEndOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1)

        findViewById<android.widget.Button>(R.id.btnStartDate).setOnClickListener { pickDate { startDate = it; updateTexts() } }
        findViewById<android.widget.Button>(R.id.btnEndDate).setOnClickListener { pickDate { endDate = it; updateTexts() } }
        findViewById<android.widget.Button>(R.id.btnLoadGraph).setOnClickListener { loadGraph() }
        
        // Populate dummy data if user is logged in
        lifecycleScope.launch {
            populateDummyDataIfEmpty()
            updateTexts()
            loadGraph()
        }
    }

    private suspend fun populateDummyDataIfEmpty() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        val categories = database.categoryDao().getCategoriesForUser(userId).first()
        if (categories.isEmpty()) {
            val foodId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Food"))
            val transportId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Transport"))
            val entertainmentId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Entertainment"))

            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = foodId, amount = 150.0, date = System.currentTimeMillis(), description = "Lunch"))
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = foodId, amount = 50.0, date = System.currentTimeMillis() - 86400000, description = "Snacks"))
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = transportId, amount = 80.0, date = System.currentTimeMillis(), description = "Uber"))
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = entertainmentId, amount = 120.0, date = System.currentTimeMillis(), description = "Movie"))
        }
    }

    private fun pickDate(onResult: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onResult(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateTexts() {
        findViewById<android.widget.TextView>(R.id.tvStartDate).text = "Start: ${DateUtils.formatDate(startDate)}"
        findViewById<android.widget.TextView>(R.id.tvEndDate).text = "End: ${DateUtils.formatDate(endDate)}"
    }

    private fun loadGraph() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val categories = database.categoryDao().getCategoriesForUser(userId).first()
            val expenses = database.expenseDao().getExpensesBetweenDates(userId, startDate, endDate).first()
            val spendingMap = expenses.groupBy { it.categoryId }.mapValues { it.value.sumOf { exp: com.pennywise.budgettracker.data.models.Expense -> exp.amount } }

            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            val categoryIds = mutableListOf<Long>()
            var i = 0f
            for (cat in categories) {
                entries.add(BarEntry(i, spendingMap[cat.categoryId]?.toFloat() ?: 0f))
                labels.add(cat.name)
                categoryIds.add(cat.categoryId)
                i += 1f
            }

            val dataSet = BarDataSet(entries, "Spending")
            dataSet.color = Color.parseColor("#4CAF50")
            val barData = BarData(dataSet)

            val barChart = findViewById<BarChart>(R.id.barChart)
            barChart.data = barData
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.xAxis.granularity = 1f
            barChart.description.isEnabled = false
            barChart.invalidate()

            // Add min/max lines per category
            barChart.axisLeft.removeAllLimitLines()
            for ((idx, catId) in categoryIds.withIndex()) {
                val budget = database.budgetDao().getCategoryBudget(userId, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear(), catId)
                if (budget != null) {
                    if (budget.maxAmount > 0) {
                        val line = LimitLine(budget.maxAmount.toFloat(), "Max")
                        line.lineColor = Color.RED
                        line.lineWidth = 2f
                        barChart.axisLeft.addLimitLine(line)
                    }
                }
            }
        }
    }
}