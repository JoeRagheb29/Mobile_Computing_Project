package com.example.mobile_computing_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.mobile_computing_project.DataBase.Daf3aDatabase
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.DataBase.UserEntity
import com.example.mobile_computing_project.ui.LoginScreen
import com.example.mobile_computing_project.ui.RegisterScreen
import com.example.mobile_computing_project.ui.TwoFactorAuthDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var db: Daf3aDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Daf3aDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        // Skip login if user already has an active session (Lab 4 SharedPreferences)
        if (sessionManager.isLoggedIn()) {
            navigateToHome(sessionManager.getUserId(), sessionManager.getUsername())
            return
        }

        setContent {
            var isLoginScreen by remember { mutableStateOf(true) }

            if (isLoginScreen) {
                LoginScreen(
                    onLoginClick = { username, password ->
                        handleLogin(username, password)
                    },
                    onNavigateToRegister = { isLoginScreen = false }
                )
            } else {
                RegisterScreen(
                    onRegisterClick = { username, phone, password, balance ->
                        handleRegister(username, phone, password, balance) {
                            isLoginScreen = true
                        }
                    },
                    onNavigateToLogin = { isLoginScreen = true }
                )
            }
        }
    }

    private fun handleLogin(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getByUsername(username)
            withContext(Dispatchers.Main) {
                if (user != null && user.password == password) {
                    TwoFactorAuthDialog(
                        context = this@MainActivity,
                        userPhone = user.phone,
                        onVerified = {
                            sessionManager.saveUserSession(user.userId, user.username)
                            Toast.makeText(this@MainActivity, "Welcome ${user.username}!", Toast.LENGTH_SHORT).show()
                            navigateToHome(user.userId, user.username)
                        }
                    ).show()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleRegister(
        username: String,
        phone: String,
        password: String,
        balance: Long,
        onSuccess: () -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getByUsername(username)
            val existingPhone = db.userDao().getByPhone(phone)

            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            if (existingPhone != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Phone number already registered", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                TwoFactorAuthDialog(
                    context = this@MainActivity,
                    userPhone = phone,
                    onVerified = {
                        completeRegistration(username, phone, password, balance, onSuccess)
                    }
                ).show()
            }
        }
    }

    private fun completeRegistration(
        username: String,
        phone: String,
        password: String,
        balance: Long,
        onSuccess: () -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val newUser = UserEntity(
                username = username,
                password = password,
                phone = phone,
                balance = balance
            )
            db.userDao().insert(newUser)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        }
    }

    private fun navigateToHome(userId: Int, username: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
        }
        startActivity(intent)
        finish()
    }
}