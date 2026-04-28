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
        setupClickListeners()
    }

    private fun setupClickListeners() {
        val btnRegister = findViewById<android.widget.Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<android.widget.TextView>(R.id.tvLoginLink)

        btnRegister.setOnClickListener {
            performRegistration()
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegistration() {
        val etUsername = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword)

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validation
        when {
            username.isEmpty() -> {
                etUsername.error = "Username required"
                return
            }
            email.isEmpty() -> {
                etEmail.error = "Email required"
                return
            }
            password.isEmpty() -> {
                etPassword.error = "Password required"
                return
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                return
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                return
            }
        }

        lifecycleScope.launch {
            try {
                // Check if username already exists
                val existingUser = database.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val passwordHash = PasswordHasher.hashPassword(password)
                val user = User(username = username, email = email, passwordHash = passwordHash)
                val userId = database.userDao().insertUser(user)

                if (userId > 0) {
                    Toast.makeText(this@RegisterActivity, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("RegisterActivity", "Registration error: ${e.message}", e)
                Toast.makeText(this@RegisterActivity, "Error occurred during registration", Toast.LENGTH_SHORT).show()
            }
        }
    }
}