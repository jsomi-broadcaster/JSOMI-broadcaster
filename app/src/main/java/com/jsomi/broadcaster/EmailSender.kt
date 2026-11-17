package com.jsomi.broadcaster

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ============================================
// JSTV EMAIL SYSTEM - Powered by Brevo
// File: EmailSender.kt
// SAFE VERSION - Key comes from BuildConfig, not here
// ============================================

object EmailSender {

    private const val TAG = "JSTV_Email"
    private const val BREVO_URL = "https://api.brevo.com/v3/smtp/email"
    private const val SENDER_NAME = "JSTV"
    private const val SENDER_EMAIL = "info.jsomitv@gmail.com"
    private const val JSTV_WEBSITE = "https://jstv.com"

    // Key is injected from local.properties -> BuildConfig
    // Never paste the real key in this file
    private val apiKey: String
        get() = BuildConfig.BREVO_API_KEY

    fun sendEmail(
        toEmail: String,
        toName: String,
        subject: String,
        htmlContent: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        Thread {
            try {
                val url = URL(BREVO_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("accept", "application/json")
                connection.setRequestProperty("api-key", apiKey)
                connection.setRequestProperty("content-type", "application/json")
                connection.doOutput = true

                val body = JSONObject().apply {
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

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(body.toString())
                writer.flush()
                writer.close()

                val code = connection.responseCode
                Log.d(TAG, "Email to $toEmail -> HTTP $code")
                onResult(code in 200..299)
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Email failed: ${e.message}", e)
                onResult(false)
            }
        }.start()
    }

    fun sendWelcomeEmail(toEmail: String, toName: String, onResult: (Boolean) -> Unit = {}) {
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
        sendEmail(toEmail, toName, "Welcome to JSTV 🎉", html, onResult)
    }

    fun sendOTPEmail(toEmail: String, toName: String, otpCode: String, onResult: (Boolean) -> Unit = {}) {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px; text-align:center;">
              <h2>Hello $toName</h2>
              <p>Your JSTV verification code is:</p>
              <h1 style="font-size:45px; letter-spacing:8px; background:#f5f5f5; padding:15px; border-radius:10px;">$otpCode</h1>
              <p>This code expires in <b>10 minutes</b>. Do not share it.</p>
              <p style="color:#888; font-size:12px;">- JSTV Team</p>
            </div>
        """.trimIndent()
        sendEmail(toEmail, toName, "Your JSTV Code: $otpCode", html, onResult)
    }

    fun sendPasswordResetEmail(toEmail: String, toName: String, resetLink: String, onResult: (Boolean) -> Unit = {}) {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
              <h2>Hello $toName</h2>
              <p>You requested to reset your JSTV password.</p>
              <p>Click the button below:</p>
              <a href="$resetLink" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block;">Reset Password</a>
              <p style="margin-top:20px;">Or copy this link:<br>$resetLink</p>
              <p>If you did not request this, just ignore this email.</p>
              <p style="color:#888; font-size:12px;">- JSTV Team</p>
            </div>
        """.trimIndent()
        sendEmail(toEmail, toName, "Reset Your JSTV Password", html, onResult)
    }
}
