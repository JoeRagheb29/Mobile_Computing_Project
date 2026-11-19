package com.example.mobile_computing_project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_computing_project.DataBase.UserEntity

@Composable
fun ProfileScreen(
    user: UserEntity,

    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Daf3aBackground)
    ) {
        // Circle back button, top left
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Daf3aPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Username", fontSize = 12.sp, color = Color.Gray)
                    Text(user.username, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Phone", fontSize = 12.sp, color = Color.Gray)
                    Text(user.phone, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Balance", fontSize = 12.sp, color = Color.Gray)
                    Text("${user.balance} EGP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Daf3aPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Account ID", fontSize = 12.sp, color = Color.Gray)
                    Text("#${user.userId}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}