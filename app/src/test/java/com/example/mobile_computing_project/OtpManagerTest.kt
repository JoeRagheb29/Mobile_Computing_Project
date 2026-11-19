package com.example.mobile_computing_project

import com.example.mobile_computing_project.utils.OtpManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpManagerTest {

    @Test
    fun generateOtp_hasDefaultLengthOfSix() {
        val otp = OtpManager.generateOtp()
        assertEquals(6, otp.length)
    }

    @Test
    fun generateOtp_containsOnlyDigits() {
        val otp = OtpManager.generateOtp()
        assertTrue(otp.all { it.isDigit() })
    }

    @Test
    fun isOtpExpired_returnsFalse_forRecentOtp() {
        val now = System.currentTimeMillis()
        val isExpired = OtpManager.isOtpExpired(now)
        assertFalse(isExpired)
    }

    @Test
    fun isOtpExpired_returnsTrue_afterValidityPeriod() {
        val sixMinutesAgo = System.currentTimeMillis() - (6 * 60 * 1000)
        val isExpired = OtpManager.isOtpExpired(sixMinutesAgo)
        assertTrue(isExpired)
    }

    @Test
    fun verifyOtp_returnsTrue_forValidCode() {
        val now = System.currentTimeMillis()
        val isValid = OtpManager.verifyOtp("123456", "123456", now)
        assertTrue(isValid)
    }

    @Test
    fun verifyOtp_returnsFalse_forIncorrectCode() {
        val now = System.currentTimeMillis()
        val isValid = OtpManager.verifyOtp("000000", "123456", now)
        assertFalse(isValid)
    }

    @Test
    fun verifyOtp_returnsFalse_whenExpired() {
        val tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000)
        val isValid = OtpManager.verifyOtp("123456", "123456", tenMinutesAgo)
        assertFalse(isValid)
    }

    @Test
    fun verifyOtp_handlesLeadingAndTrailingSpaces() {
        val now = System.currentTimeMillis()
        val isValid = OtpManager.verifyOtp(" 123456 ", "123456", now)
        assertTrue(isValid)
    }

    @Test
    fun verifyOtp_returnsFalse_forBlankInput() {
        val now = System.currentTimeMillis()
        val isValid = OtpManager.verifyOtp("   ", "123456", now)
        assertFalse(isValid)
    }
}
