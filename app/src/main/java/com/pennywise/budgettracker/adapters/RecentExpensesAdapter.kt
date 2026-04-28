// File: com/pennywise/budgettracker/adapters/RecentExpensesAdapter.kt

package com.pennywise.budgettracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.models.Expense
import com.pennywise.budgettracker.utils.DateUtils
import java.text.DecimalFormat

data class ExpenseWithCategory(
    val expense: Expense,
    val categoryName: String
)

class RecentExpensesAdapter(
    private val onItemClick: (ExpenseWithCategory) -> Unit
) : RecyclerView.Adapter<RecentExpensesAdapter.ExpenseViewHolder>() {

    private var expenses = listOf<ExpenseWithCategory>()
    private val currencyFormat = DecimalFormat("R #,##0.00")

    fun submitList(list: List<ExpenseWithCategory>) {
        expenses = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvExpenseDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvExpenseDate)

        fun bind(item: ExpenseWithCategory) {
            tvCategory.text = item.categoryName
            tvAmount.text = currencyFormat.format(item.expense.amount)
            tvDescription.text = item.expense.description
            tvDate.text = DateUtils.formatDate(item.expense.date)

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}