// File: com/pennywise/budgettracker/adapters/CategorySpendingAdapter.kt
// Reference: Phillips, B. and Hardy, B. (2019) 'Android Programming: The Big Nerd Ranch Guide'

package com.pennywise.budgettracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.models.Category
import java.text.DecimalFormat

data class CategoryWithSpending(
    val category: Category,
    val spent: Double,
    val budgetLimit: Double
)

class CategorySpendingAdapter(
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategorySpendingAdapter.CategoryViewHolder>() {

    private var categories = listOf<CategoryWithSpending>()
    private val currencyFormat = DecimalFormat("R #,##0.00")

    fun submitList(list: List<CategoryWithSpending>) {
        categories = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCategory: CardView = itemView.findViewById(R.id.cardCategory)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvSpentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        private val tvBudgetLimit: TextView = itemView.findViewById(R.id.tvBudgetLimit)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarCategory)

        fun bind(item: CategoryWithSpending) {
            tvCategoryName.text = item.category.name
            tvSpentAmount.text = currencyFormat.format(item.spent)

            val percentage = if (item.budgetLimit > 0) {
                (item.spent / item.budgetLimit * 100).toInt()
            } else 0

            tvBudgetLimit.text = "Budget: ${currencyFormat.format(item.budgetLimit)} (${percentage}%)"
            progressBar.progress = percentage.coerceIn(0, 100)

            // Change color if overspending
            if (percentage > 100) {
                tvSpentAmount.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                )
                progressBar.progressTintList = ContextCompat.getColorStateList(
                    itemView.context, android.R.color.holo_red_dark
                )
            } else {
                tvSpentAmount.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.black)
                )
            }

            cardCategory.setOnClickListener {
                onItemClick(item.category)
            }
        }
    }
}