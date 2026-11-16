package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeveloperActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvUsers: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adminPrefs = getSharedPreferences("JSTV_ADMIN", MODE_PRIVATE)
        val subPrefs = getSharedPreferences("JSTV_SUBSCRIBERS", MODE_PRIVATE)

        // Build screen in code - no XML ids needed
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)
        layout.setBackgroundColor(Color.WHITE)

        tvGreeting = TextView(this)
        tvGreeting.textSize = 22f
        tvGreeting.setTextColor(Color.BLACK)
        tvGreeting.gravity = Gravity.CENTER
        layout.addView(tvGreeting)

        tvUsers = TextView(this)
        tvUsers.textSize = 16f
        tvUsers.setTextColor(Color.BLACK)
        tvUsers.setPadding(0, 30, 0, 30)
        layout.addView(tvUsers)

        val btnRefresh = Button(this)
        btnRefresh.text = "REFRESH"
        layout.addView(btnRefresh)

        val btnPay = Button(this)
        btnPay.text = "GO TO PAYMENT"
        layout.addView(btnPay)

        val btnLive = Button(this)
        btnLive.text = "GO TO LIVE"
        layout.addView(btnLive)

        val btnDeveloper = Button(this)
        btnDeveloper.text = "LOGOUT / BACK"
        layout.addView(btnDeveloper)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)

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
