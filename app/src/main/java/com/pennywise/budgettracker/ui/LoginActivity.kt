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

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<android.widget.Button>(R.id.btnLogin).setOnClickListener {
            val username = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUsername).text.toString().trim()
            val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword).text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val hash = PasswordHasher.hashPassword(password)
                val user = database.userDao().loginUser(username, hash)
                if (user != null) {
                    sessionManager.saveLoginState(user.userId, user.username)
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.tvRegisterLink).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
