package com.example.mobile_computing_project
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_receipt)

        // ربط العناصر
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val receiptCard = findViewById<MaterialCardView>(R.id.receiptCard)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvAmount = findViewById<TextView>(R.id.tvAmount)
        val tvSender = findViewById<TextView>(R.id.tvSender)
        val tvReceiver = findViewById<TextView>(R.id.tvReceiver)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvTransactionId = findViewById<TextView>(R.id.tvTransactionId)
        val btnShareText = findViewById<MaterialButton>(R.id.btnShareText)
        val btnShareImage = findViewById<MaterialButton>(R.id.btnShareImage)

        // استقبال البيانات من الشاشة السابقة (مع قيم افتراضية للتجربة السريعة)
        val transactionId = intent.getStringExtra(EXTRA_TXN_ID) ?: "TXN-98451203"
        val amount = intent.getStringExtra(EXTRA_AMOUNT) ?: "1,250.00 EGP"
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: "ahmed@instapay"
        val receiver = intent.getStringExtra(EXTRA_RECEIVER) ?: "mohamed@instapay"
        val date = intent.getStringExtra(EXTRA_DATE) ?: "19 Nov 2026, 08:45 PM"
        val status = intent.getStringExtra(EXTRA_STATUS) ?: "Transfer Successful"

        // عرض البيانات في الواجهة
        tvStatus.text = status
        tvAmount.text = amount
        tvSender.text = sender
        tvReceiver.text = receiver
        tvDate.text = date
        tvTransactionId.text = "#$transactionId"

        btnBack.setOnClickListener { finish() }

        // مشاركة كنص
        btnShareText.setOnClickListener {
            val receiptText = """
                Mini InstaPay Receipt
                ---------------------
                Status: $status
                Amount: $amount
                From: $sender
                To: $receiver
                Date: $date
                Reference ID: #$transactionId
            """.trimIndent()

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Transaction Receipt #$transactionId")
                putExtra(Intent.EXTRA_TEXT, receiptText)
            }
            startActivity(Intent.createChooser(sendIntent, "Share Receipt via"))
        }

        // مشاركة كصورة (لقطة للبطاقة)
        btnShareImage.setOnClickListener {
            shareCardAsImage(receiptCard, transactionId)
        }
    }

    private fun shareCardAsImage(view: View, transactionId: String) {
        // 1. رسم الـ Bitmap لازم يكون على الـ UI Thread
        if (view.width <= 0 || view.height <= 0) {
            Toast.makeText(this, "Receipt is not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // 2. حفظ الملف وضغط الصورة في Worker Thread (Dispatchers.IO)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cachePath = File(cacheDir, "receipts").apply { mkdirs() }
                val imageFile = File(cachePath, "receipt_$transactionId.png")
                FileOutputStream(imageFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                val contentUri = FileProvider.getUriForFile(
                    this@TransactionReceiptActivity,
                    "${packageName}.fileprovider",
                    imageFile
                )

                // 3. العودة للـ Main Thread لفتح شاشة المشاركة
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share Receipt Image"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TransactionReceiptActivity,
                        "Failed to share receipt: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    companion object {
        const val EXTRA_TXN_ID = "extra_txn_id"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_RECEIVER = "extra_receiver"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_STATUS = "extra_status"

        // دالة جاهزة يناديها زمايلك من شاشة Send Money أو History لفتح الإيصال
        fun start(
            context: Context,
            txnId: String,
            amount: String,
            sender: String,
            receiver: String,
            date: String,
            status: String = "Transfer Successful"
        ) {
            val intent = Intent(context, TransactionReceiptActivity::class.java).apply {
                putExtra(EXTRA_TXN_ID, txnId)
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_RECEIVER, receiver)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_STATUS, status)
            }
            context.startActivity(intent)
        }
    }
}