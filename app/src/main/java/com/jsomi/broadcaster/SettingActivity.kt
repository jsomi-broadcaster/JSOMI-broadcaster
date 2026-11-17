package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(40, 40, 40, 40)
        setContentView(root)

        val title = TextView(this); title.text = "Settings - Studio Setup"; title.textSize = 24f; title.setTextColor(Color.WHITE); root.addView(title)

        val etChannel = EditText(this); etChannel.hint = "Channel Name"; etChannel.setHintTextColor(Color.GRAY); etChannel.setTextColor(Color.WHITE); root.addView(etChannel)
        val etTicker = EditText(this); etTicker.hint = "Ticker Text"; etTicker.setHintTextColor(Color.GRAY); etTicker.setTextColor(Color.WHITE); root.addView(etTicker)
        val etVerse = EditText(this); etVerse.hint = "Verse e.g. Psalm 122:1"; etVerse.setHintTextColor(Color.GRAY); etVerse.setTextColor(Color.WHITE); root.addView(etVerse)

        val cbLogo = CheckBox(this); cbLogo.text = "Show Logo"; cbLogo.setTextColor(Color.WHITE); cbLogo.isChecked = true; root.addView(cbLogo)
        val cbVerse = CheckBox(this); cbVerse.text = "Show Verse"; cbVerse.setTextColor(Color.WHITE); cbVerse.isChecked = true; root.addView(cbVerse)
        val cbTicker = CheckBox(this); cbTicker.text = "Show Ticker"; cbTicker.setTextColor(Color.WHITE); cbTicker.isChecked = true; root.addView(cbTicker)
        val cbBible = CheckBox(this); cbBible.text = "Show Bible"; cbBible.setTextColor(Color.WHITE); cbBible.isChecked = true; root.addView(cbBible)

        val btnApply = Button(this); btnApply.text = "APPLY → GO TO DESTINATION"
        btnApply.setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit()
                .putString("channel", etChannel.text.toString())
                .putString("ticker", etTicker.text.toString())
                .putString("verse", etVerse.text.toString())
                .putBoolean("showLogo", cbLogo.isChecked)
                .putBoolean("showVerse", cbVerse.isChecked)
                .putBoolean("showTicker", cbTicker.isChecked)
                .putBoolean("showBible", cbBible.isChecked)
                .apply()
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DestinationActivity::class.java))
        }
        root.addView(btnApply)
    }
}
