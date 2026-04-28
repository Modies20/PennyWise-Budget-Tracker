// File: com/pennywise/budgettracker/utils/SessionManager.kt
// Reference: Howard and LeBlanc (2021) 'Android Security Internals'

package com.pennywise.budgettracker.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("PennyWisePrefs", Context.MODE_PRIVATE)

    /**
     * Save user login session after successful authentication
     */
    fun saveLoginState(userId: Long, username: String) {
        prefs.edit().apply {
            putLong("USER_ID", userId)
            putString("USERNAME", username)
            putBoolean("IS_LOGGED_IN", true)
            putLong("LOGIN_TIMESTAMP", System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Check if user session exists
     */
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    /**
     * Get stored user ID
     */
    fun getUserId(): Long = prefs.getLong("USER_ID", -1)

    /**
     * Get stored username
     */
    fun getUsername(): String = prefs.getString("USERNAME", "") ?: ""

    /**
     * Clear session on logout
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Check if session is still valid (7 days expiry as per NFR3.4)
     * Reference: OWASP Foundation (2024) 'Session Management Cheat Sheet'
     */
    fun isSessionValid(): Boolean {
        val loginTime = prefs.getLong("LOGIN_TIMESTAMP", 0)
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() - loginTime < sevenDaysInMillis
    }
}

/**
 * Simple password hashing utility
 * Reference: OWASP Foundation (2024) 'Password Storage Cheat Sheet'
 * Note: Production apps should use BCrypt for better security
 */
object PasswordHasher {
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}