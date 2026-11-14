package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class HomeMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)

        val prefs = getSharedPreferences("JSTV_PREFS", MODE_PRIVATE)
        val userName = prefs.getString("userName", "Streamer") ?: "Streamer"
        val isPaid = prefs.getBoolean("isPaid", false)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome, $userName"

        val btnMainStudio = findViewById<Button>(R.id.btnMainStudio)
        val btnRemote = findViewById<Button>(R.id.btnRemote)
        val btnWireless = findViewById<Button>(R.id.btnWireless)
        val btnBigScreen = findViewById<Button>(R.id.btnBigScreen)

        // Ask camera permission - Screen 2 Camera Opens
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }

        // MAIN STUDIO - FREE, always opens. All settings inside.
        btnMainStudio.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // REMOTE CONTROL - PREMIUM LOCKED
        btnRemote.setOnClickListener {
            if (isPaid) {
                Toast.makeText(this, "Remote Control - Coming Soon", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🔒 Premium - Pay to Unlock", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PaymentActivity::class.java))
            }
        }

        // WIRELESS CAMERA - PREMIUM LOCKED
        btnWireless.setOnClickListener {
            if (isPaid) {
                Toast.makeText(this, "Wireless Camera - Coming Soon", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🔒 Premium - Pay to Unlock", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PaymentActivity::class.java))
            }
        }

        // TV / CHURCH BIG SCREEN - PREMIUM LOCKED
        btnBigScreen.setOnClickListener {
            if (isPaid) {
                Toast.makeText(this, "TV / Big Screen - Coming Soon", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🔒 Premium - Pay to Unlock", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PaymentActivity::class.java))
            }
        }
    }
}
