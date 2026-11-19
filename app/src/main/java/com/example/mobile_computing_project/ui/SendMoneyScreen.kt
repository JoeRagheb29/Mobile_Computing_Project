package com.example.mobile_computing_project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_computing_project.ui.Daf3aPrimary
import com.example.mobile_computing_project.ui.theme.Mobile_Computing_ProjectTheme

@Composable
fun SendMoneyScreen(
    balance: Long,
    onContactsClick: () -> Unit,
    onSendClick: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
    pickedPhone: String = "",
    clearTrigger: Int = 0,
    onBackClick: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Daf3a") }

    // Fill the phone field when a contact is picked
    LaunchedEffect(pickedPhone) {
        if (pickedPhone.isNotEmpty()) phone = pickedPhone
    }

    // Empty the text boxes after a successful transfer
    LaunchedEffect(clearTrigger) {
        if (clearTrigger > 0) {
            phone = ""
            amount = ""
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Send Money", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Daf3aPrimary)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Balance: $balance", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Receiver phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onContactsClick) {
                    Text(text = "Contacts")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = method == "Daf3a",
                    onClick = { method = "Daf3a" }
                )
                Text(text = "Daf3a")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = method == "Wallet",
                    onClick = { method = "Wallet" }
                )
                Text(text = "Wallet")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSendClick(phone, amount, method) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendMoneyScreenPreview() {
    Mobile_Computing_ProjectTheme {
        SendMoneyScreen(
            balance = 100,
            onContactsClick = {},
            onSendClick = { _, _, _ -> }
        )
    }
}