package com.example.mobile_computing_project.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobile_computing_project.R
import com.example.mobile_computing_project.utils.OtpManager
import com.example.mobile_computing_project.utils.SmsHelper

class TwoFactorAuthDialog(
    private val context: Context,
    private val userPhone: String,
    private val onVerified: () -> Unit
) {

    private var currentOtp: String = ""
    private var otpGeneratedAt: Long = 0L
    private var countDownTimer: CountDownTimer? = null
    private var dialog: AlertDialog? = null

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_two_factor_auth, null)

        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val etOtp = view.findViewById<EditText>(R.id.etOtpCode)
        val tvTimer = view.findViewById<TextView>(R.id.tvTimer)
        val btnVerify = view.findViewById<Button>(R.id.btnVerifyOtp)
        val btnResend = view.findViewById<Button>(R.id.btnResendOtp)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelOtp)

        tvSubtitle.text = context.getString(R.string.two_factor_auth_subtitle, userPhone)

        generateAndDispatchOtp()
        startCountdown(tvTimer, btnResend)

        val builder = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)

        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnVerify.setOnClickListener {
            val inputCode = etOtp.text.toString()
            if (OtpManager.verifyOtp(inputCode, currentOtp, otpGeneratedAt)) {
                countDownTimer?.cancel()
                dialog?.dismiss()
                Toast.makeText(context, context.getString(R.string.otp_verified_success), Toast.LENGTH_SHORT).show()
                onVerified()
            } else {
                if (OtpManager.isOtpExpired(otpGeneratedAt)) {
                    etOtp.error = context.getString(R.string.otp_expired_error)
                } else {
                    etOtp.error = context.getString(R.string.otp_invalid_error)
                }
            }
        }

        btnResend.setOnClickListener {
            generateAndDispatchOtp()
            startCountdown(tvTimer, btnResend)
            etOtp.text.clear()
            etOtp.error = null
        }

        btnCancel.setOnClickListener {
            countDownTimer?.cancel()
            dialog?.dismiss()
        }

        dialog?.show()
    }

    private fun generateAndDispatchOtp() {
        currentOtp = OtpManager.generateOtp()
        otpGeneratedAt = System.currentTimeMillis()
        SmsHelper.sendOtpSms(context, userPhone, currentOtp)
    }

    private fun startCountdown(tvTimer: TextView, btnResend: Button) {
        countDownTimer?.cancel()
        btnResend.isEnabled = false

        countDownTimer = object : CountDownTimer(300000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                tvTimer.text = "00:00"
                btnResend.isEnabled = true
            }
        }.start()
    }
}