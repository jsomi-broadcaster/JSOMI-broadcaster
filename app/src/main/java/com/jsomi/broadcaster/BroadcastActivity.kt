package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BroadcastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(60, 60, 60, 60)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        val title = TextView(this).apply {
            text = "JSTV BROADCAST"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)

        val btnSettings = Button(this).apply {
            text = "General Settings"
            setOnClickListener { startActivity(Intent(this@BroadcastActivity, SettingsActivity::class.java)) }
        }
        val btnLive = Button(this).apply {
            text = "GO LIVE"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener { /* TODO: Go Live next */ }
        }
        layout.addView(btnSettings)
        layout.addView(btnLive)
        setContentView(layout)
    }
}
