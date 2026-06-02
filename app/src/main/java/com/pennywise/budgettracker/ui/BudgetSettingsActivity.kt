package com.pennywise.budgettracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Budget
import com.pennywise.budgettracker.data.models.Category
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BudgetSettingsActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter
    private var categories = listOf<Category>()
    private var categoryBudgets = mutableMapOf<Long, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_settings)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        findViewById<Button>(R.id.btnSaveBudgets).setOnClickListener { saveAllBudgets() }

        categoryBudgetAdapter = CategoryBudgetAdapter { catId, limit ->
            categoryBudgets[catId] = limit
        }
        findViewById<RecyclerView>(R.id.rvCategoryBudgets).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvCategoryBudgets).adapter = categoryBudgetAdapter

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            database.categoryDao().getCategoriesForUser(userId).collect { cats ->
                categories = cats
                val month = DateUtils.getCurrentMonth()
                val year = DateUtils.getCurrentYear()
                val existingBudgets = database.budgetDao().getBudgetsForMonth(userId, month, year)
                val totalBudget = existingBudgets.find { it.categoryId == null }
                if (totalBudget != null) {
                    findViewById<EditText>(R.id.etTotalBudget).setText(totalBudget.limitAmount.toString())
                }
                for (cat in categories) {
                    val catBudget = existingBudgets.find { it.categoryId == cat.categoryId }
                    categoryBudgets[cat.categoryId] = catBudget?.limitAmount ?: 0.0
                }
                categoryBudgetAdapter.submitList(categories, categoryBudgets)
            }
        }
    }

    private fun saveAllBudgets() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val month = DateUtils.getCurrentMonth()
            val year = DateUtils.getCurrentYear()

            val totalText = findViewById<EditText>(R.id.etTotalBudget).text.toString()
            val totalLimit = totalText.toDoubleOrNull()
            if (totalLimit != null && totalLimit > 0) {
                val existingTotal = database.budgetDao().getTotalMonthlyBudget(userId, month, year)
                if (existingTotal != null) {
                    database.budgetDao().updateBudget(existingTotal.copy(limitAmount = totalLimit))
                } else {
                    database.budgetDao().insertBudget(Budget(userId = userId, categoryId = null, month = month, year = year, limitAmount = totalLimit))
                }
            }

            for ((catId, limit) in categoryBudgets) {
                if (limit > 0) {
                    val existing = database.budgetDao().getCategoryBudget(userId, month, year, catId)
                    if (existing != null) {
                        database.budgetDao().updateBudget(existing.copy(limitAmount = limit))
                    } else {
                        database.budgetDao().insertBudget(Budget(userId = userId, categoryId = catId, month = month, year = year, limitAmount = limit))
                    }
                }
            }
            Toast.makeText(this@BudgetSettingsActivity, "Budgets saved", Toast.LENGTH_SHORT).show()
        }
    }

    inner class CategoryBudgetAdapter(private val onLimitChange: (Long, Double) -> Unit) :
        RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {
        private var categories = listOf<Category>()
        private var limits = mutableMapOf<Long, Double>()

        fun submitList(cats: List<Category>, lim: Map<Long, Double>) {
            categories = cats
            limits = lim.toMutableMap()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spending, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            val cat = categories[pos]
            holder.tvName.text = cat.name
            holder.etLimit.visibility = View.VISIBLE
            val limit = limits[cat.categoryId] ?: 0.0
            holder.etLimit.setText(if (limit > 0) limit.toString() else "")
            holder.etLimit.setOnFocusChangeListener { _, _ ->
                val newLimit = holder.etLimit.text.toString().toDoubleOrNull() ?: 0.0
                onLimitChange(cat.categoryId, newLimit)
            }
        }
        override fun getItemCount() = categories.size
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName = view.findViewById<TextView>(R.id.tvCatName)
            val etLimit = view.findViewById<EditText>(R.id.etLimit)
        }
    }
}
