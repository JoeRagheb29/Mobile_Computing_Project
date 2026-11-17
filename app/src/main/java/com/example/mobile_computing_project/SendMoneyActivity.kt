package com.example.mobile_computing_project

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.mobile_computing_project.DataBase.Daf3aDatabase
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.DataBase.TransactionEntity
import com.example.mobile_computing_project.ui.theme.Mobile_Computing_ProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendMoneyActivity : ComponentActivity() {

    private lateinit var db: Daf3aDatabase
    private var userId = -1
    private var balance by mutableStateOf(0L)
    private var pickedPhone by mutableStateOf("")
    private var clearTrigger by mutableStateOf(0)

    // Receives the contact the user picked from the system contacts list
    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    pickedPhone = normalizePhone(cursor.getString(0) ?: "")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the logged in user id sent from the previous activity,
        // fall back to the saved session if it is missing
        val sessionManager = SessionManager(this)
        userId = intent.getIntExtra("EXTRA_USER_ID", sessionManager.getUserId())
        db = Daf3aDatabase.getInstance(this)

        // Load the sender balance in a different scope, update the UI on the main thread
        lifecycleScope.launch(Dispatchers.IO) {
            val sender = db.userDao().getById(userId)
            withContext(Dispatchers.Main) {
                if (sender == null) {
                    showMessage("User not found")
                    finish()
                } else {
                    balance = sender.balance
                }
            }
        }

        setContent {
            Mobile_Computing_ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SendMoneyScreen(
                        balance = balance,
                        onContactsClick = { openContacts() },
                        onSendClick = { phone, amount, method ->
                            onSendClicked(phone, amount, method)
                        },
                        modifier = Modifier.padding(innerPadding),
                        pickedPhone = pickedPhone,
                        clearTrigger = clearTrigger
                    )
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Opens the contacts list on top of the app so the user can choose a number
    private fun openContacts() {
        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )
        try {
            pickContactLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showMessage("No contacts app found")
        }
    }

    // Turns +20 100 123 4567 or 0020... into 0100... so it passes the validation
    private fun normalizePhone(raw: String): String {
        var number = raw.filter { it.isDigit() || it == '+' }
        if (number.startsWith("+20")) number = "0" + number.removePrefix("+20")
        else if (number.startsWith("0020")) number = "0" + number.removePrefix("0020")
        return number
    }

    // Wallet number structure: 11 digits starting with 010, 011, 012 or 015
    private fun isValidPhone(phone: String): Boolean {
        return Regex("^01[0125][0-9]{8}$").matches(phone)
    }

    // Returns the amount as a number, or null if the input is not valid
    private fun validateInput(phone: String, amountText: String): Long? {
        if (phone.isEmpty() || amountText.isEmpty()) {
            showMessage("Please fill all fields")
            return null
        }

        val amount = amountText.toLongOrNull()
        if (amount == null || amount <= 0) {
            showMessage("Enter a valid amount")
            return null
        }

        if (!isValidPhone(phone)) {
            showMessage("Invalid phone number")
            return null
        }

        return amount
    }

    private fun onSendClicked(phone: String, amountText: String, method: String) {
        val amount = validateInput(phone.trim(), amountText.trim()) ?: return

        if (method == "Daf3a") {
            sendByDaf3a(phone.trim(), amount)
        } else {
            sendByWallet(phone.trim(), amount)
        }
    }

    // Both users must be in the database
    private fun sendByDaf3a(phone: String, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            val sender = userDao.getById(userId)
            val receiver = userDao.getByPhone(phone)

            var message: String
            var newBalance = sender?.balance ?: 0L
            var success = false

            if (sender == null) {
                message = "Sender not found"
            } else if (receiver == null) {
                message = "Receiver is not registered in Daf3a"
            } else if (receiver.userId == sender.userId) {
                message = "You cannot send money to yourself"
            } else if (sender.balance < amount) {
                message = "Insufficient balance"
            } else {
                // take from the sender, give to the receiver, save the transaction
                userDao.updateBalance(sender.userId, sender.balance - amount)
                userDao.updateBalance(receiver.userId, receiver.balance + amount)
                db.transactionDao().insert(
                    TransactionEntity(
                        senderId = sender.userId,
                        receiverId = receiver.userId,
                        amount = amount
                    )
                )
                newBalance = sender.balance - amount
                message = "Sent $amount to ${receiver.username}"
                success = true
            }

            withContext(Dispatchers.Main) {
                balance = newBalance
                showMessage(message)
                if (success) {
                    pickedPhone = ""
                    clearTrigger++
                }
            }
        }
    }

    // The receiver does not need to be in the database, only the number structure is checked
    private fun sendByWallet(phone: String, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            val sender = userDao.getById(userId)

            var message: String
            var newBalance = sender?.balance ?: 0L
            var success = false

            if (sender == null) {
                message = "Sender not found"
            } else if (phone == sender.phone) {
                message = "You cannot send money to yourself"
            } else if (sender.balance < amount) {
                message = "Insufficient balance"
            } else {
                // only the sender balance changes
                userDao.updateBalance(sender.userId, sender.balance - amount)
                newBalance = sender.balance - amount
                message = "Sent $amount to wallet $phone"
                success = true
            }

            withContext(Dispatchers.Main) {
                balance = newBalance
                showMessage(message)
                if (success) {
                    pickedPhone = ""
                    clearTrigger++
                }
            }
        }
    }
}