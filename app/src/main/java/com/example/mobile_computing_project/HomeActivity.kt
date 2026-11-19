package com.example.mobile_computing_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.mobile_computing_project.DataBase.Daf3aDatabase
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.ui.Daf3aBackground
import com.example.mobile_computing_project.ui.Daf3aPrimary
import com.example.mobile_computing_project.utils.SmsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : ComponentActivity() {

    private lateinit var db: Daf3aDatabase
    private lateinit var sessionManager: SessionManager
    private var scannerJob: Job? = null
    private var balanceState = mutableLongStateOf(0L)
    private var userPhone = ""
    private var currentUserId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Daf3aDatabase.getInstance(this)
        sessionManager = SessionManager(this)
        val username = intent.getStringExtra("USERNAME") ?: sessionManager.getUsername()
        currentUserId = intent.getIntExtra("USER_ID", sessionManager.getUserId())

        fetchUserData(currentUserId)
        startTransactionScanner(currentUserId)

        setContent {
            val balance by balanceState
            HomeScreen(
                username = username,
                userId = currentUserId,
                balance = balance,
                onSendMoneyClick = {
                    val intent = Intent(this, SendMoneyActivity::class.java).apply {
                        putExtra("EXTRA_USER_ID", currentUserId)
                    }
                    startActivity(intent)
                },
                onProfileClick = {
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("EXTRA_USER_ID", currentUserId)
                    }
                    startActivity(intent)
                },
                onTransactionsClick = {
                    val intent = Intent(this, TransactionHistoryActivity::class.java).apply {
                        putExtra("EXTRA_USER_ID", currentUserId)
                    }
                    startActivity(intent)
                },
                onLogoutClick = {
                    scannerJob?.cancel()
                    sessionManager.logout()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentUserId != -1) {
            fetchUserData(currentUserId)
            startTransactionScanner(currentUserId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerJob?.cancel()
    }

    private fun fetchUserData(userId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getById(userId)
            if (user != null) {
                userPhone = user.phone
                withContext(Dispatchers.Main) {
                    balanceState.longValue = user.balance
                }
            }
        }
    }

    private fun startTransactionScanner(userId: Int) {
        scannerJob?.cancel()
        scannerJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(4000)
                val lastSeen = sessionManager.getLastSeenTransactionId(userId)
                val newTransactions = db.transactionDao().getNewIncomingTransactions(userId, lastSeen)

                if (newTransactions.isNotEmpty()) {
                    val highestId = newTransactions.maxOf { it.transactionId }
                    sessionManager.setLastSeenTransactionId(userId, highestId)

                    val updatedUser = db.userDao().getById(userId)
                    val newBalance = updatedUser?.balance ?: 0L

                    val message = if (newTransactions.size == 1) {
                        val tx = newTransactions.first()
                        val sender = db.userDao().getById(tx.senderId)
                        val senderName = sender?.username ?: "Someone"
                        "Daf3a: You received ${tx.amount} EGP from $senderName! Your new balance is $newBalance EGP."
                    } else {
                        val detailsBuilder = StringBuilder()
                        for (tx in newTransactions) {
                            val sender = db.userDao().getById(tx.senderId)
                            val senderName = sender?.username ?: "Someone"
                            detailsBuilder.append("- ").append(tx.amount).append(" EGP from ").append(senderName).append("\n")
                        }
                        val details = detailsBuilder.toString().trimEnd()
                        "Daf3a: You received new transfers:\n$details\nYour new balance is $newBalance EGP."
                    }

                    withContext(Dispatchers.Main) {
                        balanceState.longValue = newBalance
                        Toast.makeText(this@HomeActivity, "New transfer received!", Toast.LENGTH_LONG).show()
                        SmsHelper.sendReceivedMoneySms(this@HomeActivity, userPhone, message)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    username: String,
    userId: Int,
    balance: Long,
    onSendMoneyClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Daf3aBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome, $username!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Daf3aPrimary
        )

        Text(
            text = "Account ID: #$userId",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Daf3aPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Available Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "EGP $balance",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Button(
            onClick = onSendMoneyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Daf3aPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Send Money", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onTransactionsClick,
            colors = ButtonDefaults.buttonColors(containerColor = Daf3aPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Transaction History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onProfileClick,
            colors = ButtonDefaults.buttonColors(containerColor = Daf3aPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Daf3aPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}