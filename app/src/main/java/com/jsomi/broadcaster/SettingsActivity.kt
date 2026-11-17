package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(60, 60, 60, 60)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        val title = TextView(this).apply {
            text = "General Settings"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 22f
        }
        layout.addView(title)
        val btnPlan = Button(this).apply {
            text = "Plan"
            setOnClickListener { startActivity(Intent(this@SettingsActivity, PlanActivity::class.java)) }
        }
        val btnDest = Button(this).apply {
            text = "Destination"
            setOnClickListener { startActivity(Intent(this@SettingsActivity, DestinationActivity::class.java)) }
        }
        val btnLiveSetup = Button(this).apply { text = "Live Setup" }
        layout.addView(btnPlan)
        layout.addView(btnDest)
        layout.addView(btnLiveSetup)
        setContentView(layout)
    }
}
