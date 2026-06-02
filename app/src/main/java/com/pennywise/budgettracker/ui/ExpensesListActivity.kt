package com.pennywise.budgettracker.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Expense
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

data class ExpenseWithCategoryName(val expense: Expense, val categoryName: String)

class ExpensesListActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var startDate = 0L
    private var endDate = 0L
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        val cal = Calendar.getInstance()
        startDate = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1)
        endDate = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1)

        findViewById<android.widget.Button>(R.id.btnStartDate).setOnClickListener { pickDate { startDate = it; filterExpenses() } }
        findViewById<android.widget.Button>(R.id.btnEndDate).setOnClickListener { pickDate { endDate = it; filterExpenses() } }
        findViewById<android.widget.Button>(R.id.btnFilter).setOnClickListener { filterExpenses() }

        adapter = ExpenseAdapter { expense ->
            expense.receiptImagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val uri = Uri.fromFile(file)
                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, "image/*")
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Receipt image not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<RecyclerView>(R.id.rvExpenses).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvExpenses).adapter = adapter

        filterExpenses()
    }

    private fun pickDate(onResult: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onResult(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun filterExpenses() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            database.expenseDao().getExpensesBetweenDates(userId, startDate, endDate).collect { expenses ->
                val expensesWithNames = mutableListOf<ExpenseWithCategoryName>()
                for (exp in expenses) {
                    val cat = database.categoryDao().getCategoryById(userId, exp.categoryId)
                    expensesWithNames.add(ExpenseWithCategoryName(exp, cat?.name ?: "Unknown"))
                }
                adapter.submitList(expensesWithNames)
            }
        }
    }

    inner class ExpenseAdapter(private val onReceiptClick: (Expense) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {
        private var items = listOf<ExpenseWithCategoryName>()
        fun submitList(list: List<ExpenseWithCategoryName>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_expense, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            val item = items[pos]
            holder.tvCategory.text = item.categoryName
            holder.tvDescription.text = item.expense.description
            holder.tvAmount.text = "R${item.expense.amount}"
            holder.tvDate.text = DateUtils.formatDate(item.expense.date)
            if (item.expense.receiptImagePath != null) {
                holder.ivReceiptThumb.visibility = android.view.View.VISIBLE
                holder.ivReceiptThumb.setOnClickListener { onReceiptClick(item.expense) }
            } else {
                holder.ivReceiptThumb.visibility = android.view.View.GONE
            }
        }
        override fun getItemCount() = items.size
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val tvCategory = view.findViewById<android.widget.TextView>(R.id.tvCategory)
            val tvDescription = view.findViewById<android.widget.TextView>(R.id.tvDescription)
            val tvAmount = view.findViewById<android.widget.TextView>(R.id.tvAmount)
            val tvDate = view.findViewById<android.widget.TextView>(R.id.tvDate)
            val ivReceiptThumb = view.findViewById<android.widget.ImageView>(R.id.ivReceiptThumb)
        }
    }
}
