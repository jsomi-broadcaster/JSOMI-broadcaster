package com.jsomi.broadcaster

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// ============================================
// JSTV / JSOMI Email System - Powered by Brevo
// SAFE VERSION - No API Key Inside
// Key comes from local.properties -> BuildConfig
// ============================================

object EmailSender {

    private const val TAG = "JSTV-Email"
    private const val SENDER_NAME = "JSTV"
    private const val SENDER_EMAIL = "info.jsomitv@gmail.com"
    private const val JSTV_WEBSITE = "https://jstv.com"

    private val client = OkHttpClient()

    // --- 1. MAIN SENDER ---
    suspend fun sendEmail(
        toEmail: String,
        toName: String,
        subject: String,
        htmlContent: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.BREVO_API_KEY

            val json = JSONObject().apply {
                put("sender", JSONObject().apply {
                    put("name", SENDER_NAME)
                    put("email", SENDER_EMAIL)
                })
                put("to", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("email", toEmail)
                        put("name", toName)
                    })
                })
                put("subject", subject)
                put("htmlContent", htmlContent)
            }

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("api-key", apiKey)
                .addHeader("content-type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Email Sent to: $toEmail")
                true
            } else {
                Log.e(TAG, "❌ Email Failed: $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Email Error: ${e.message}")
            false
        }
    }

    // --- 2. WELCOME EMAIL ---
    suspend fun sendWelcomeEmail(toEmail: String, toName: String): Boolean {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
              <h1 style="color:#000;">Welcome to JSTV 🎉</h1>
              <p>Hello <b>$toName</b>,</p>
              <p>Thanks for joining <b>JSTV</b>. Your account is now active.</p>
              <a href="$JSTV_WEBSITE" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block; margin-top:10px;">Open JSTV</a>
              <br><br>
              <p style="color:#888; font-size:12px;">- JSTV Team<br>info.jsomitv@gmail.com</p>
            </div>
        """.trimIndent()
        return sendEmail(toEmail, toName, "Welcome to JSTV 🎉", html)
    }

    // --- 3. OTP / VERIFICATION CODE ---
    suspend fun sendOTPEmail(toEmail: String, toName: String, otpCode: String): Boolean {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px; text-align:center;">
              <h2>Hello $toName</h2>
              <p>Your JSTV verification code is:</p>
              <h1 style="font-size:45px; letter-spacing:8px; background:#f5f5f5; padding:15px; border-radius:10px;">$otpCode</h1>
              <p>This code expires in <b>10 minutes</b>. Do not share it.</p>
              <p style="color:#888; font-size:12px;">- JSTV Team</p>
            </div>
        """.trimIndent()
        return sendEmail(toEmail, toName, "Your JSTV Code: $otpCode", html)
    }

    // --- 4. PASSWORD RESET ---
    suspend fun sendPasswordResetEmail(toEmail: String, toName: String, resetLink: String): Boolean {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
              <h2>Hello $toName</h2>
              <p>You requested to reset your JSTV password.</p>
              <a href="$resetLink" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block;">Reset Password</a>
              <p style="margin-top:20px;">Or copy this link:<br>$resetLink</p>
              <p>If you did not request this, just ignore this email.</p>
              <p style="color:#888; font-size:12px;">- JSTV Team</p>
            </div>
        """.trimIndent()
        return sendEmail(toEmail, toName, "Reset Your JSTV Password", html)
    }
}
