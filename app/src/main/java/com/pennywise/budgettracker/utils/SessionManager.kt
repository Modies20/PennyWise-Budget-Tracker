// File: com/pennywise/budgettracker/utils/SessionManager.kt
// Reference: Howard and LeBlanc (2021) 'Android Security Internals'

package com.pennywise.budgettracker.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PennyWisePrefs", Context.MODE_PRIVATE)

    fun saveLoginState(userId: Long, username: String) {
        prefs.edit().putLong("USER_ID", userId).putString("USERNAME", username)
            .putBoolean("IS_LOGGED_IN", true).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)
    fun getUserId(): Long = prefs.getLong("USER_ID", -1)
    fun getUsername(): String = prefs.getString("USERNAME", "") ?: ""
    fun clearSession() = prefs.edit().clear().apply()
}

object PasswordHasher {
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}