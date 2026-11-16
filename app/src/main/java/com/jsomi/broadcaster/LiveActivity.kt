package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LiveActivity : AppCompatActivity() {

    private lateinit var tvCamera: TextView
    private lateinit var tvWatermark: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvVerse: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvStatus: TextView
    private var isFront = false
    private var isMicOn = true
    private var isLive = false

    private fun isPaid(): Boolean {
        return getSharedPreferences("JSTV", MODE_PRIVATE).getBoolean("isPaid", false)
    }

    private fun showPayment() {
        val options = arrayOf("Daily - KSh 200", "Monthly - KSh 1,000", "3 Months - KSh 2,500", "Yearly - KSh 8,000")
        AlertDialog.Builder(this)
            .setTitle("🔒 Premium Locked - Pay to Unlock Live Settings")
            .setItems(options) { _, which ->
                // For now: M-Pesa / Manual - mark as paid after choosing (replace with real M-Pesa later)
                getSharedPreferences("JSTV", MODE_PRIVATE).edit().putBoolean("isPaid", true).putString("plan", options[which]).apply()
                Toast.makeText(this, "Paid: ${options[which]} - Settings Unlocked! Pay with M-Pesa / Manual", Toast.LENGTH_LONG).show()
                tvStatus.text = "PAID: ${options[which]} - Settings Unlocked"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkLocked(): Boolean {
        if (!isPaid()) { showPayment(); return true }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        val savedName = prefs.getString("name", "Jehovah Shalom TV") ?: "Jehovah Shalom TV"

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)

        // --- CAMERA AREA (behind menu) ---
        tvCamera = TextView(this)
        tvCamera.text = "📷 CAMERA PREVIEW\nBACK CAM\nTap MENU below to open Main Studio"
        tvCamera.textSize = 18f
        tvCamera.setTextColor(Color.WHITE)
        tvCamera.gravity = Gravity.CENTER
        tvCamera.setPadding(20, 80, 20, 80)
        tvCamera.setBackgroundColor(Color.DKGRAY)
        root.addView(tvCamera)

        // JSTV Watermark - ALWAYS ON
        tvWatermark = TextView(this)
        tvWatermark.text = "JSTV • Jehovah Shalom TV"
        tvWatermark.setTextColor(Color.YELLOW)
        tvWatermark.textSize = 14f
        tvWatermark.gravity = Gravity.END
        root.addView(tvWatermark)

        // Ticker overlay
        tvTicker = TextView(this)
        tvTicker.text = "Welcome to Jehovah Shalom TV..."
        tvTicker.setTextColor(Color.WHITE)
        tvTicker.setBackgroundColor(Color.RED)
        tvTicker.gravity = Gravity.CENTER
        root.addView(tvTicker)

        // Verse overlay
        tvVerse = TextView(this)
        tvVerse.text = "Psalm 122:1 - I was glad when they said unto me..."
        tvVerse.setTextColor(Color.WHITE)
        tvVerse.gravity = Gravity.CENTER
        root.addView(tvVerse)

        // Logo overlay
        tvLogo = TextView(this)
        tvLogo.text = "✝️ JSTV LOGO"
        tvLogo.setTextColor(Color.YELLOW)
        tvLogo.gravity = Gravity.CENTER
        root.addView(tvLogo)

        // Bible overlay
        tvBible = TextView(this)
        tvBible.text = "📖 BIBLE OPEN"
        tvBible.setTextColor(Color.WHITE)
        tvBible.gravity = Gravity.CENTER
        root.addView(tvBible)

        tvStatus = TextView(this)
        tvStatus.text = if (isPaid()) "PAID - Settings Unlocked" else "NOT PAID - Settings Locked 🔒"
        tvStatus.setTextColor(Color.YELLOW)
        tvStatus.gravity = Gravity.CENTER
        root.addView(tvStatus)

        // --- MENU ON TOP OF CAMERA ---
        val menu = LinearLayout(this)
        menu.orientation = LinearLayout.HORIZONTAL
        val btnStudio = Button(this); btnStudio.text = "MAIN STUDIO"
        val btnRemote = Button(this); btnRemote.text = "REMOTE"
        val btnWireless = Button(this); btnWireless.text = "WIRELESS CAM"
        val btnTv = Button(this); btnTv.text = "TV / BIG SCREEN"
        menu.addView(btnStudio); menu.addView(btnRemote); menu.addView(btnWireless); menu.addView(btnTv)
        root.addView(menu)

        // --- MAIN STUDIO - ALL SETTINGS ---
        val studio = LinearLayout(this)
        studio.orientation = LinearLayout.VERTICAL
        studio.setBackgroundColor(Color.WHITE)
        studio.setPadding(40, 30, 40, 30)
        studio.visibility = LinearLayout.GONE
        root.addView(studio)

        val studioTitle = TextView(this); studioTitle.text = "MAIN STUDIO - ALL SETTINGS"; studioTitle.textSize = 20f; studioTitle.setTextColor(Color.BLACK); studioTitle.gravity = Gravity.CENTER
        studio.addView(studioTitle)

        val etChannel = EditText(this); etChannel.hint = "Channel Name"; etChannel.setText(savedName); studio.addView(etChannel)
        val etTicker = EditText(this); etTicker.hint = "Ticker Text"; etTicker.setText("Welcome to Jehovah Shalom TV..."); studio.addView(etTicker)
        val etVerse = EditText(this); etVerse.hint = "Verse"; etVerse.setText("Psalm 122:1"); studio.addView(etVerse)

        val cbLogo = CheckBox(this); cbLogo.text = "Show Logo"; cbLogo.isChecked = true; studio.addView(cbLogo)
        val cbVerse = CheckBox(this); cbVerse.text = "Show Verse"; cbVerse.isChecked = true; studio.addView(cbVerse)
        val cbTicker = CheckBox(this); cbTicker.text = "Show Ticker"; cbTicker.isChecked = true; studio.addView(cbTicker)
        val cbBible = CheckBox(this); cbBible.text = "Show Bible"; cbBible.isChecked = false; studio.addView(cbBible)

        val destLabel = TextView(this); destLabel.text = "Stream Destination - 1 at a time:"; studio.addView(destLabel)
        val rgDest = RadioGroup(this)
        val rbFb = RadioButton(this); rbFb.text = "Facebook"; rbFb.isChecked = true
        val rbYt = RadioButton(this); rbYt.text = "YouTube"
        val rbTt = RadioButton(this); rbTt.text = "TikTok"
        rgDest.addView(rbFb); rgDest.addView(rbYt); rgDest.addView(rbTt)
        studio.addView(rgDest)

        val camRow = LinearLayout(this); camRow.orientation = LinearLayout.HORIZONTAL
        val btnFrontBack = Button(this); btnFrontBack.text = "FRONT CAM / BACK CAM"
        val btnMic = Button(this); btnMic.text = "MIC: ON"
        camRow.addView(btnFrontBack); camRow.addView(btnMic); studio.addView(camRow)

        val btnApply = Button(this); btnApply.text = "APPLY SETTINGS"; studio.addView(btnApply)

        val btnGoLive = Button(this); btnGoLive.text = "🔴 GO LIVE"; btnGoLive.setBackgroundColor(Color.RED); btnGoLive.setTextColor(Color.WHITE); studio.addView(btnGoLive)

        val btnPay2 = Button(this); btnPay2.text = "PAY TO UNLOCK (M-Pesa / Manual)"; studio.addView(btnPay2)

        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)

        // --- MENU ACTIONS ---
        btnStudio.setOnClickListener {
            studio.visibility = if (studio.visibility == LinearLayout.GONE) LinearLayout.VISIBLE else LinearLayout.GONE
        }
        btnRemote.setOnClickListener {
            if (checkLocked()) return@setOnClickListener
            Toast.makeText(this, "Remote Control - Control Main Studio from another phone", Toast.LENGTH_LONG).show()
        }
        btnWireless.setOnClickListener {
            if (checkLocked()) return@setOnClickListener
            Toast.makeText(this, "Wireless Camera - Connect second phone as wireless camera", Toast.LENGTH_LONG).show()
        }
        btnTv.setOnClickListener {
            if (checkLocked()) return@setOnClickListener
            Toast.makeText(this, "TV / Church Big Screen - Cast to TV / Big Screen", Toast.LENGTH_LONG).show()
        }

        // --- SETTINGS ACTIONS - All locked if not paid ---
        fun lockedTap(block: () -> Unit) { if (!checkLocked()) block() }

        cbLogo.setOnClickListener { lockedTap { tvLogo.visibility = if (cbLogo.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbVerse.setOnClickListener { lockedTap { tvVerse.visibility = if (cbVerse.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbTicker.setOnClickListener { lockedTap { tvTicker.visibility = if (cbTicker.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbBible.setOnClickListener { lockedTap { tvBible.visibility = if (cbBible.isChecked) TextView.VISIBLE else TextView.GONE } }

        btnFrontBack.setOnClickListener { lockedTap {
            isFront = !isFront
            tvCamera.text = if (isFront) "📷 CAMERA PREVIEW\nFRONT CAM" else "📷 CAMERA PREVIEW\nBACK CAM"
        }}
        btnMic.setOnClickListener { lockedTap {
            isMicOn = !isMicOn
            btnMic.text = if (isMicOn) "MIC: ON" else "MIC: OFF"
            Toast.makeText(this, if (isMicOn) "Mic ON" else "Mic OFF", Toast.LENGTH_SHORT).show()
        }}

        btnApply.setOnClickListener { lockedTap {
            tvTicker.text = etTicker.text.toString()
            tvVerse.text = etVerse.text.toString()
            prefs.edit().putString("channelName", etChannel.text.toString()).putString("ticker", etTicker.text.toString()).putString("verse", etVerse.text.toString()).apply()
            Toast.makeText(this, "Settings Applied - Watermark JSTV Always ON", Toast.LENGTH_SHORT).show()
        }}

        btnGoLive.setOnClickListener { lockedTap {
            val dest = when (rgDest.checkedRadioButtonId) {
                rbFb.id -> "Facebook"; rbYt.id -> "YouTube"; rbTt.id -> "TikTok"; else -> "Facebook"
            }
            // RadioGroup ids are auto, use checked button text fallback
            isLive = !isLive
            if (isLive) {
                btnGoLive.text = "⏹ STOP LIVE"
                tvStatus.text = "🔴 LIVE NOW - $dest - ${etChannel.text} - JSTV Watermark ON"
                Toast.makeText(this, "GOING LIVE to $dest", Toast.LENGTH_LONG).show()
            } else {
                btnGoLive.text = "🔴 GO LIVE"
                tvStatus.text = "LIVE STOPPED"
            }
        }}

        btnPay2.setOnClickListener { showPayment() }
    }
}
