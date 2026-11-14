package com.jsomi.broadcaster

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome to JSTV - Choose What To Do"

        fun openStudio() {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // MAIN STUDIO - Kept
        findViewById<Button>(R.id.btnGoToStudio).setOnClickListener { openStudio() }
        findViewById<Button>(R.id.btnMainStudio).setOnClickListener { openStudio() }

        // REMOTE CONTROL - Kept - Opens Studio in Remote Mode
        findViewById<Button>(R.id.btnRemote).setOnClickListener {
            Toast.makeText(this, "📱 Remote Control - Control Studio from Phone", Toast.LENGTH_LONG).show()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "remote")
            startActivity(i)
        }

        // WIRELESS CAMERA - Kept
        findViewById<Button>(R.id.btnWireless).setOnClickListener {
            Toast.makeText(this, "📶 Wireless Camera - Connect Second Phone as Camera", Toast.LENGTH_LONG).show()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "wireless")
            startActivity(i)
        }

        // BIG SCREEN MODE - Kept - Forces Landscape Studio
        findViewById<Button>(R.id.btnBigScreen).setOnClickListener {
            Toast.makeText(this, "🖥 Big Screen Mode - Landscape", Toast.LENGTH_SHORT).show()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "bigscreen")
            startActivity(i)
        }

        // PAYMENT / PREMIUM - Kept
        findViewById<Button>(R.id.btnPayment).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }
        findViewById<Button>(R.id.btnGoPremium).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        // LOGOUT - Kept
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}
