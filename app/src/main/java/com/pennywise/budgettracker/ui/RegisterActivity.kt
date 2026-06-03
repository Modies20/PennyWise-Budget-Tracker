// File: com/pennywise/budgettracker/RegisterActivity.kt
// Reference: Burnette, E. (2021) 'Hello, Android'

package com.pennywise.budgettracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pennywise.budgettracker.R
import com.pennywise.budgettracker.data.database.AppDatabase
import com.pennywise.budgettracker.data.models.User
import com.pennywise.budgettracker.utils.PasswordHasher
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        database = AppDatabase.getInstance(this)

        findViewById<android.widget.Button>(R.id.btnRegister).setOnClickListener {
            val username = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsername).text.toString().trim()
            val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword).text.toString()
            val confirm = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword).text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val existing = database.userDao().getUserByUsername(username)
                if (existing != null) {
                    Toast.makeText(this@RegisterActivity, "Username taken", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val hash = PasswordHasher.hashPassword(password)
                val user = User(username = username, email = email, passwordHash = hash)
                database.userDao().insertUser(user)
                Toast.makeText(this@RegisterActivity, "Registered! Please login", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}