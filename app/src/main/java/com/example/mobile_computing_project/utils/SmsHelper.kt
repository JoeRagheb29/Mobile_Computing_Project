package com.example.mobile_computing_project.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SmsHelper {

    fun sendSms(context: Context, phone: String, message: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun sendOtpSms(context: Context, phone: String, otp: String): Boolean {
        return sendSms(context, phone, "Daf3a 2FA Code: $otp\nValid for 5 minutes. Do not share this code.")
    }

    fun sendReceivedMoneySms(context: Context, phone: String, message: String): Boolean {
        return sendSms(context, phone, message)
    }
}