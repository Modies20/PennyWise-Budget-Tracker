package com.pennywise.budgettracker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Category
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_management)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        findViewById<android.widget.Button>(R.id.btnAddCategory).setOnClickListener { showAddCategoryDialog() }

        adapter = CategoryAdapter(
            onEdit = { category ->
                showEditCategoryDialog(category)
            },
            onDelete = { category ->
                lifecycleScope.launch {
                    database.categoryDao().deleteCategory(category)
                    loadCategories()
                }
            }
        )
        findViewById<RecyclerView>(R.id.rvCategories).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvCategories).adapter = adapter
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            database.categoryDao().getCategoriesForUser(userId).collect { categories ->
                adapter.submitList(categories)
            }
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this).setTitle("New Category").setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val userId = sessionManager.getUserId()
                        database.categoryDao().insertCategory(Category(userId = userId, name = name))
                        loadCategories()
                    }
                } else Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val input = EditText(this).apply { setText(category.name) }
        AlertDialog.Builder(this).setTitle("Edit Category").setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        database.categoryDao().updateCategory(category.copy(name = newName))
                        loadCategories()
                    }
                }
            }.setNegativeButton("Cancel", null).show()
    }

    inner class CategoryAdapter(
        private val onEdit: (Category) -> Unit,
        private val onDelete: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        private var list = listOf<Category>()
        fun submitList(l: List<Category>) { list = l; notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            val cat = list[pos]
            holder.itemView.findViewById<android.widget.TextView>(android.R.id.text1).text = cat.name
            holder.itemView.setOnClickListener { onEdit(cat) }
            holder.itemView.setOnLongClickListener { onDelete(cat); true }
        }
        override fun getItemCount() = list.size
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view)
    }
}
