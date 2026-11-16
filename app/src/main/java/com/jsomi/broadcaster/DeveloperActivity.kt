package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeveloperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvUsers = findViewById<TextView>(R.id.tvUsers)
        val btnPay = findViewById<Button>(R.id.btnPay)
        val btnLive = findViewById<Button>(R.id.btnLive)
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        val btnDeveloper = findViewById<Button>(R.id.btnDeveloper)
        val adminPrefs = getSharedPreferences("JSTV_ADMIN", MODE_PRIVATE)
        val subPrefs = getSharedPreferences("JSTV_SUBSCRIBERS", MODE_PRIVATE)
        fun refresh() {
            val adminName = adminPrefs.getString("adminName", "No admin")
            val adminEmail = adminPrefs.getString("adminEmail", "No admin email")
            tvGreeting.text = "Welcome Admin\n$adminName"
            val total = subPrefs.getInt("totalSubscribers", 0)
            val builder = StringBuilder()
            builder.append("SUBSCRIBERS\nTotal: $total\n\n")
            for (i in 1..total) {
                val sName = subPrefs.getString("sub_${i}_name", "")
                val sEmail = subPrefs.getString("sub_${i}_email", "")
                builder.append("$i. $sName - $sEmail\n")
            }
            builder.append("\n--- ADMIN ---\nName: $adminName\nEmail: $adminEmail")
            tvUsers.text = builder.toString()
        }
        refresh()
        btnRefresh.setOnClickListener { refresh() }
        btnPay.setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }
        btnLive.setOnClickListener { startActivity(Intent(this, LiveActivity::class.java)) }
        btnDeveloper.setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
}
