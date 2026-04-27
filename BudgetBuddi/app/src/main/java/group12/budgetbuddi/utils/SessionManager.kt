package group12.budgetbuddi.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveSession(email: String, username: String) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_email", email)
            putString("username", username)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}