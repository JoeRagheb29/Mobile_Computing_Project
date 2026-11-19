package com.example.mobile_computing_project.DataBase

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("Daf3aPrefs", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, username: String) {
        sharedPref.edit()
            .putInt("USER_ID", userId)
            .putString("USERNAME", username)
            .putBoolean("IS_LOGGED_IN", true)
            .apply()
    }

    fun getUserId(): Int = sharedPref.getInt("USER_ID", -1)

    fun getUsername(): String = sharedPref.getString("USERNAME", "User") ?: "User"

    fun isLoggedIn(): Boolean = sharedPref.getBoolean("IS_LOGGED_IN", false)

    fun logout() {
        sharedPref.edit().clear().apply()
    }

    private val txPref = context.getSharedPreferences("Daf3aTxPrefs", Context.MODE_PRIVATE)

    fun getLastSeenTransactionId(userId: Int): Int {
        return txPref.getInt("LAST_SEEN_TX_$userId", 0)
    }

    fun setLastSeenTransactionId(userId: Int, txId: Int) {
        txPref.edit().putInt("LAST_SEEN_TX_$userId", txId).apply()
    }
}