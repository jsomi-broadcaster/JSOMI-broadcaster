package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)
        val name = getSharedPreferences("JSTV", MODE_PRIVATE).getString("name", "Streamer")
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $name"
        findViewById<Button>(R.id.btnMainStudio).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        findViewById<Button>(R.id.btnRemote).setOnClickListener { startActivity(Intent(this, RemoteControlActivity::class.java)) }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { startActivity(Intent(this, CameraActivity::class.java)) }
        findViewById<Button>(R.id.btnChurch).setOnClickListener { startActivity(Intent(this, ChurchScreenActivity::class.java)) }
        findViewById<Button>(R.id.btnDeveloper).setOnClickListener { startActivity(Intent(this, DeveloperActivity::class.java)) }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, AuthActivity::class.java)); finish()
        }
    }
}
