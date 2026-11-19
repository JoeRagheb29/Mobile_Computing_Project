package com.example.mobile_computing_project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_computing_project.DataBase.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionHistoryScreen(
    transactions: List<TransactionEntity>,
    currentUserId: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Daf3aBackground)
    ) {
        // Circle back button, top left
        // Circle back button, top left: white inside, black outline
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = Daf3aPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // leaves room for the back button above the title
            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "Transaction History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Daf3aPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions yet", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(transactions) { tx ->
                        val isSent = tx.senderId == currentUserId
                        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(Date(tx.timestamp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isSent) "Sent to User #${tx.receiverId}" else "Received from User #${tx.senderId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(date, fontSize = 12.sp, color = Color.Gray)
                                }
                                Text(
                                    text = if (isSent) "- ${tx.amount} EGP" else "+ ${tx.amount} EGP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSent) Color.Red else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}