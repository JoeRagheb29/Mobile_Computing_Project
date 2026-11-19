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
import com.example.mobile_computing_project.DataBase.UserEntity
import com.example.mobile_computing_project.ui.TwoFactorAuthDialog
import com.example.mobile_computing_project.ui.theme.Mobile_Computing_ProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                        clearTrigger = clearTrigger,
                        onBackClick = { finish() }
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

    private fun sendByDaf3a(phone: String, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            val sender = userDao.getById(userId)
            val receiver = userDao.getByPhone(phone)

            if (sender == null) {
                withContext(Dispatchers.Main) { showMessage("Sender not found") }
                return@launch
            }
            if (receiver == null) {
                withContext(Dispatchers.Main) { showMessage("Receiver is not registered in Daf3a") }
                return@launch
            }
            if (receiver.userId == sender.userId) {
                withContext(Dispatchers.Main) { showMessage("You cannot send money to yourself") }
                return@launch
            }
            if (sender.balance < amount) {
                withContext(Dispatchers.Main) { showMessage("Insufficient balance") }
                return@launch
            }

            withContext(Dispatchers.Main) {
                TwoFactorAuthDialog(this@SendMoneyActivity, sender.phone) {
                    performDaf3aTransfer(sender, receiver, amount)
                }.show()
            }
        }
    }

    private fun performDaf3aTransfer(sender: UserEntity, receiver: UserEntity, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            userDao.updateBalance(sender.userId, sender.balance - amount)
            userDao.updateBalance(receiver.userId, receiver.balance + amount)
            val txnId = db.transactionDao().insert(
                TransactionEntity(
                    senderId = sender.userId,
                    receiverId = receiver.userId,
                    amount = amount
                )
            )
            val newBalance = sender.balance - amount
            val formattedDate = getCurrentFormattedDate()
            withContext(Dispatchers.Main) {
                balance = newBalance
                pickedPhone = ""
                clearTrigger++
                TransactionReceiptActivity.start(
                    context = this@SendMoneyActivity,
                    txnId = System.currentTimeMillis().toString(),
                    amount = "$amount EGP",
                    sender = sender.phone,
                    receiver = receiver.phone,
                    date = formattedDate
                )
            }
        }
    }

    private fun sendByWallet(phone: String, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            val sender = userDao.getById(userId)

            if (sender == null) {
                withContext(Dispatchers.Main) { showMessage("Sender not found") }
                return@launch
            }
            if (phone == sender.phone) {
                withContext(Dispatchers.Main) { showMessage("You cannot send money to yourself") }
                return@launch
            }
            if (sender.balance < amount) {
                withContext(Dispatchers.Main) { showMessage("Insufficient balance") }
                return@launch
            }

            withContext(Dispatchers.Main) {
                TwoFactorAuthDialog(this@SendMoneyActivity, sender.phone) {
                    performWalletTransfer(sender, phone, amount)
                }.show()
            }
        }
    }

    private fun performWalletTransfer(sender: UserEntity, phone: String, amount: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            userDao.updateBalance(sender.userId, sender.balance - amount)
            val newBalance = sender.balance - amount
            val formattedDate = getCurrentFormattedDate()
            withContext(Dispatchers.Main) {
                balance = newBalance
                pickedPhone = ""
                clearTrigger++
                TransactionReceiptActivity.start(
                    context = this@SendMoneyActivity,
                    txnId = System.currentTimeMillis().toString(),
                    amount = "$amount EGP",
                    sender = sender.phone,
                    receiver = phone,
                    date = formattedDate
                )
            }
        }
    }

    private fun getCurrentFormattedDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return formatter.format(Date())
    }
}