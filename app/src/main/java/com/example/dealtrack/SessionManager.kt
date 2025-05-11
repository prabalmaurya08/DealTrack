package com.example.dealtrack



import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLoginState(isLoggedIn: Boolean, role: String, uid: String) {
        prefs.edit() {
            putBoolean("isLoggedIn", isLoggedIn)
                .putString("role", role)
                .putString("uid", uid)
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun getUserId(): String? {
        return prefs.getString("uid", null)
    }

    fun logout() {
        prefs.edit() { clear() }
    }
}
