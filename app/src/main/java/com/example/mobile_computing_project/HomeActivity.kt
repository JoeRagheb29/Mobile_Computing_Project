package com.example.mobile_computing_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.ui.Daf3aBackground
import com.example.mobile_computing_project.ui.Daf3aPrimary

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        // Retrieve data passed via Explicit Intent (Lab 3)
        val username = intent.getStringExtra("USERNAME") ?: sessionManager.getUsername()
        val userId = intent.getIntExtra("USER_ID", sessionManager.getUserId())

        setContent {
            HomePlaceholderScreen(
                username = username,
                userId = userId,
                onLogoutClick = {
                    sessionManager.logout()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun HomePlaceholderScreen(
    username: String,
    userId: Int,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Daf3aBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $username!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Daf3aPrimary
        )
        Text(
            text = "Account ID: #$userId",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Daf3aPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}