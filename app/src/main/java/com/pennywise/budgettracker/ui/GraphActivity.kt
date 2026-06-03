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

        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            finish()
            return
        }

        val calendar = Calendar.getInstance()
        startDate = DateUtils.getStartOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        endDate = DateUtils.getEndOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)

        findViewById<android.widget.Button>(R.id.btnStartDate).setOnClickListener { pickDate { startDate = it; updateTexts() } }
        findViewById<android.widget.Button>(R.id.btnEndDate).setOnClickListener { pickDate { endDate = it; updateTexts() } }
        findViewById<android.widget.Button>(R.id.btnLoadGraph).setOnClickListener { loadGraph() }

        lifecycleScope.launch {
            populateDummyDataIfEmpty()
            updateTexts()
            loadGraph()
        }
    }

    private suspend fun populateDummyDataIfEmpty() {
        val userId = sessionManager.getUserId()
        val categories = database.categoryDao().getCategoriesForUser(userId).first()
        val allExpenses = database.expenseDao().getAllExpenses(userId)

        if (categories.isEmpty() || allExpenses.isEmpty()) {
            val foodId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Food", colorCode = "#E91E63"))
            val transportId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Transport", colorCode = "#2196F3"))
            val entertainmentId = database.categoryDao().insertCategory(com.pennywise.budgettracker.data.models.Category(userId = userId, name = "Entertainment", colorCode = "#FF9800"))

            val now = System.currentTimeMillis()
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = foodId, amount = 450.0, date = now, description = "Grocery Shopping"))
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = transportId, amount = 120.0, date = now - 86400000, description = "Fuel"))
            database.expenseDao().insertExpense(com.pennywise.budgettracker.data.models.Expense(userId = userId, categoryId = entertainmentId, amount = 300.0, date = now, description = "Netflix & Cinema"))
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
        findViewById<android.widget.TextView>(R.id.tvStartDate).text = getString(R.string.label_start_date, DateUtils.formatDate(startDate))
        findViewById<android.widget.TextView>(R.id.tvEndDate).text = getString(R.string.label_end_date, DateUtils.formatDate(endDate))
    }

    private fun loadGraph() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val categories = database.categoryDao().getCategoriesForUser(userId).first()
            val expenses = database.expenseDao().getExpensesBetweenDates(userId, startDate, endDate).first()
            val spendingMap = expenses.groupBy { it.categoryId }.mapValues { it.value.sumOf { exp -> exp.amount } }

            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            var i = 0f
            for (cat in categories) {
                entries.add(BarEntry(i, spendingMap[cat.categoryId]?.toFloat() ?: 0f))
                labels.add(cat.name)
                i += 1f
            }

            val dataSet = BarDataSet(entries, getString(R.string.label_category_spending))
            dataSet.color = Color.parseColor("#4CAF50")
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 12f

            val barChart = findViewById<BarChart>(R.id.barChart)
            barChart.data = BarData(dataSet)
            
            // Chart Styling
            barChart.description.isEnabled = false
            barChart.setFitBars(true)
            barChart.animateY(1000)
            
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = -45f
            
            barChart.axisRight.isEnabled = false
            barChart.axisLeft.setDrawGridLines(true)
            barChart.axisLeft.axisMinimum = 0f
            
            barChart.invalidate()
        }
    }
}