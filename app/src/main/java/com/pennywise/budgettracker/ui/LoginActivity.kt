// File: com/pennywise/budgettracker/LoginActivity.kt
// Reference: Griffiths, D. (2021) 'Head First Android Development'

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
import com.pennywise.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        // Check if already logged in with valid session
        if (sessionManager.isLoggedIn() && sessionManager.isSessionValid()) {
            navigateToDashboard()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val btnLogin = findViewById<android.widget.Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<android.widget.TextView>(R.id.tvRegisterLink)
        val etUsername = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            when {
                username.isEmpty() -> {
                    etUsername.error = "Username required"
                }
                password.isEmpty() -> {
                    etPassword.error = "Password required"
                }
                else -> {
                    performLogin(username, password)
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val passwordHash = PasswordHasher.hashPassword(password)
                val user = database.userDao().loginUser(username, passwordHash)

                if (user != null) {
                    sessionManager.saveLoginState(user.userId, user.username)
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Login error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Login error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}