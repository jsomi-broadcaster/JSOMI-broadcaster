package com.jsomi.broadcaster

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ============================================
// JSTV EMAIL SYSTEM - Powered by Brevo
// SAFE VERSION - No real key in GitHub
// ============================================

object EmailSender {

    // --- 1. SETTINGS ---
    // IMPORTANT: Do NOT paste your real key here in GitHub.
    // Put your real key ONLY in Android Studio on your computer
    // before you build the APK for release.
    private const val BREVO_API_KEY = "PASTE_KEY_IN_ANDROID_STUDIO_ONLY"
    private const val SENDER_NAME = "JSTV"
    private const val SENDER_EMAIL = "info.jsomitv@gmail.com"
    private const val JSTV_WEBSITE = "https://jstv.com"
    private const val BREVO_URL = "https://api.brevo.com/v3/smtp/email"

    // --- 2. MAIN SENDER ---
    private fun sendEmail(toEmail: String, toName: String, subject: String, htmlContent: String, onResult: (Boolean) -> Unit) {
        Thread {
            try {
                val url = URL(BREVO_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("accept", "application/json")
                conn.setRequestProperty("api-key", BREVO_API_KEY)
                conn.setRequestProperty("content-type", "application/json")
                conn.doOutput = true

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
                }.toString()

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(body)
                writer.flush()
                writer.close()

                val success = conn.responseCode in 200..299
                onResult(success)
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }.start()
    }

    // --- 3. WELCOME EMAIL ---
    fun sendWelcomeEmail(toEmail: String, toName: String, onResult: (Boolean) -> Unit = {}) {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
              <h1>Welcome to JSTV 🎉</h1>
              <p>Hello <b>$toName</b>,</p>
              <p>Thanks for joining <b>JSTV</b>. Your account is now active.</p>
              <a href="$JSTV_WEBSITE" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block; margin-top:10px;">Open JSTV</a>
              <br><br>
              <p style="color:#888; font-size:12px;">- JSTV Team<br>$SENDER_EMAIL</p>
            </div>
        """.trimIndent()
        sendEmail(toEmail, toName, "Welcome to JSTV 🎉", html, onResult)
    }

    // --- 4. OTP / VERIFICATION CODE ---
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

    // --- 5. PASSWORD RESET ---
    fun sendPasswordResetEmail(toEmail: String, toName: String, resetLink: String, onResult: (Boolean) -> Unit = {}) {
        val html = """
            <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
              <h2>Hello $toName</h2>
              <p>You requested to reset your JSTV password.</p>
              <p>Click below:</p>
              <a href="$resetLink" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block;">Reset Password</a>
              <p style="margin-top:20px;">Or copy this link:<br>$resetLink</p>
              <p>If you did not request this, just ignore this email.</p>
              <p style="color:#888; font-size:12px;">- JSTV Team</p>
            </div>
        """.trimIndent()
        sendEmail(toEmail, toName, "Reset Your JSTV Password", html, onResult)
    }
}
