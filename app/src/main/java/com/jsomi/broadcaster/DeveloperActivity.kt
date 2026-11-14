package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DeveloperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)

        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        val adminPrefs = getSharedPreferences("JSTV_ADMIN", MODE_PRIVATE)

        val tvStats = findViewById<TextView>(R.id.tvDevStats)
        val tvUsers = findViewById<TextView>(R.id.tvDevUsers)
        val etAnnouncement = findViewById<EditText>(R.id.etAnnouncement)
        val etGlobalRtmp = findViewById<EditText>(R.id.etGlobalRtmp)
        val etBanUser = findViewById<EditText>(R.id.etBanUser)

        fun refresh() {
            val name = prefs.getString("name", "No user")
            val email = prefs.getString("email", "No email")
            val title = prefs.getString("title", "No stream yet")
            val rtmp = prefs.getString("rtmp", "No RTMP")
            val totalStreams = adminPrefs.getInt("totalStreams", 0)
            val totalPayments = adminPrefs.getInt("totalPayments", 0)
            val banned = adminPrefs.getString("banned", "None")
            val announcement = adminPrefs.getString("announcement", "No announcement")
            val globalRtmp = adminPrefs.getString("globalRtmp", "Not set")

            tvStats.text = "📊 APP STATS\nTotal Streams: $totalStreams\nTotal Payments: $totalPayments\nBanned Users: $banned\nGlobal RTMP: $globalRtmp\nAnnouncement: $announcement"
            tvUsers.text = "👥 CURRENT USER\nName: $name\nEmail: $email\nLast Stream: $title\nLast RTMP: $rtmp"
        }
        refresh()

        // 1. Announcement - Send to all users
        findViewById<Button>(R.id.btnSendAnnouncement).setOnClickListener {
            val msg = etAnnouncement.text.toString().trim()
            if (msg.isEmpty()) { Toast.makeText(this, "Type announcement", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            adminPrefs.edit().putString("announcement", msg).apply()
            Toast.makeText(this, "Announcement Sent to All Users: $msg", Toast.LENGTH_LONG).show()
            refresh()
        }

        // 2. Global RTMP - Set for all streamers
        findViewById<Button>(R.id.btnSaveRtmp).setOnClickListener {
            val rtmp = etGlobalRtmp.text.toString().trim()
            if (rtmp.isEmpty()) { Toast.makeText(this, "Enter RTMP", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            adminPrefs.edit().putString("globalRtmp", rtmp).apply()
            Toast.makeText(this, "Global RTMP Saved: $rtmp", Toast.LENGTH_LONG).show()
            refresh()
        }

        // 3. Ban User
        findViewById<Button>(R.id.btnBanUser).setOnClickListener {
            val user = etBanUser.text.toString().trim()
            if (user.isEmpty()) { Toast.makeText(this, "Enter email to ban", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            adminPrefs.edit().putString("banned", user).apply()
            Toast.makeText(this, "BANNED: $user", Toast.LENGTH_LONG).show()
            refresh()
        }

        // 4. Unban
        findViewById<Button>(R.id.btnUnban).setOnClickListener {
            adminPrefs.edit().putString("banned", "None").apply()
            Toast.makeText(this, "All Users Unbanned", Toast.LENGTH_SHORT).show()
            refresh()
        }

        // 5. View Live Streams - Goes to Main Studio
        findViewById<Button>(R.id.btnViewLives).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 6. View Payments
        findViewById<Button>(R.id.btnViewPayments).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        // 7. View Church Screens
        findViewById<Button>(R.id.btnViewChurch).setOnClickListener {
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }

        // 8. Reset App - Clear all data
        findViewById<Button>(R.id.btnResetApp).setOnClickListener {
            prefs.edit().clear().apply()
            adminPrefs.edit().clear().apply()
            Toast.makeText(this, "APP RESET - All Data Cleared", Toast.LENGTH_LONG).show()
            refresh()
        }

        findViewById<Button>(R.id.btnBackDev).setOnClickListener { finish() }
    }
}
