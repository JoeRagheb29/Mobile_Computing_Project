package com.example.mobile_computing_project.utils

object OtpManager {

    private const val DEFAULT_VALIDITY_MINUTES = 5L

    fun generateOtp(length: Int = 6): String {
        val digits = "0123456789"
        return (1..length)
            .map { digits.random() }
            .joinToString("")
    }

    fun isOtpExpired(generatedAtMillis: Long, validityMinutes: Long = DEFAULT_VALIDITY_MINUTES): Boolean {
        val expirationTime = generatedAtMillis + (validityMinutes * 60 * 1000)
        return System.currentTimeMillis() > expirationTime
    }

    fun verifyOtp(enteredOtp: String, actualOtp: String, generatedAtMillis: Long): Boolean {
        if (enteredOtp.isBlank() || actualOtp.isBlank()) {
            return false
        }

        if (isOtpExpired(generatedAtMillis)) {
            return false
        }

        return enteredOtp.trim() == actualOtp.trim()
    }
}
