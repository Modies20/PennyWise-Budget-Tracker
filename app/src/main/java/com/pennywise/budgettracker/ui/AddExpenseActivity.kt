package com.pennywise.budgettracker.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.Expense
import com.pennywise.budgettracker.utils.DateUtils
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var selectedDate = DateUtils.now()
    private var receiptImagePath: String? = null
    private var categoryId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        loadCategories()

        findViewById<android.widget.Button>(R.id.btnDate).setOnClickListener {
            showDatePicker()
        }
        findViewById<android.widget.Button>(R.id.btnTakePhoto).setOnClickListener {
            ImagePicker.with(this).cameraOnly().start()
        }
        findViewById<android.widget.Button>(R.id.btnSaveExpense).setOnClickListener {
            saveExpense()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val categories = database.categoryDao().getCategoriesForUser(userId).first()
            val adapter = ArrayAdapter(this@AddExpenseActivity, android.R.layout.simple_spinner_item, categories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            findViewById<android.widget.Spinner>(R.id.spinnerCategory).adapter = adapter
            if (categories.isNotEmpty()) categoryId = categories[0].categoryId
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day)
            selectedDate = cal.timeInMillis
            findViewById<android.widget.TextView>(R.id.tvSelectedDate).text = DateUtils.formatDate(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                val path = saveImageToInternalStorage(it)
                receiptImagePath = path
                findViewById<android.widget.ImageView>(R.id.ivReceiptPreview).apply {
                    setImageURI(it)
                    visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun saveExpense() {
        val amount = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount).text.toString().toDoubleOrNull()
        val description = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDescription).text.toString()
        if (amount == null || amount <= 0 || description.isEmpty()) {
            Toast.makeText(this, "Valid amount and description required", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                date = selectedDate,
                description = description,
                receiptImagePath = receiptImagePath
            )
            database.expenseDao().insertExpense(expense)
            Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}