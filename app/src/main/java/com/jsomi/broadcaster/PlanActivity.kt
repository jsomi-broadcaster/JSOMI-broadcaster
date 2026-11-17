package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(40, 40, 40, 40)
        setContentView(root)

        val title = TextView(this); title.text = "Choose Plan"; title.textSize = 24f; title.setTextColor(Color.WHITE); root.addView(title)
        val note = TextView(this); note.text = "For now EVERYTHING IS FREE - Testing Mode"; note.setTextColor(Color.GREEN); root.addView(note)

        val btnFree = Button(this); btnFree.text = "FREE MODE - CONTINUE (FREE)"
        btnFree.setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("plan", "FREE").apply()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        root.addView(btnFree)

        val btnPremium = Button(this); btnPremium.text = "PREMIUM - Weekly 200 / Monthly 1000 (FREE FOR NOW)"
        btnPremium.setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("plan", "PREMIUM").apply()
            Toast.makeText(this, "Premium selected - FREE during testing", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        root.addView(btnPremium)
    }
}
