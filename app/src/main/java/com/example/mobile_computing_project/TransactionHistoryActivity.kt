package com.example.mobile_computing_project


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.mobile_computing_project.DataBase.Daf3aDatabase
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.DataBase.TransactionEntity
import com.example.mobile_computing_project.ui.TransactionHistoryScreen
import kotlinx.coroutines.launch

class TransactionHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        val db = Daf3aDatabase.getInstance(this)

        setContent {
            var transactions by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    val list = db.transactionDao().getForUser(userId)
                    transactions = list
                }
            }

            TransactionHistoryScreen(
                transactions = transactions,
                currentUserId = userId,
                onBackClick = { finish() }
            )
        }
    }
}