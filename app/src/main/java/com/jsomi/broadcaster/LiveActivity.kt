package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.rtmp.rtmp.RtmpCamera2

class LiveActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvWatermark: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvVerse: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvStatus: TextView
    private lateinit var rtmpCamera: RtmpCamera2
    private var isLive = false
    private var isFront = false
    private var selectedDest = "Facebook"
    private var rtmpUrl = "rtmp://live-api-s.facebook.com:80/rtmp/"
    private var streamKey = ""

    private fun isPaid(): Boolean = getSharedPreferences("JSTV", MODE_PRIVATE).getBoolean("isPaid", false)

    private fun showPayment() {
        val options = arrayOf("Daily - KSh 200", "Monthly - KSh 1,000", "3 Months - KSh 2,500", "Yearly - KSh 8,000")
        AlertDialog.Builder(this).setTitle("🔒 Premium Locked").setItems(options) { _, which ->
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putBoolean("isPaid", true).putString("plan", options[which]).apply()
            tvStatus.text = "PAID: ${options[which]} - Unlocked"
            Toast.makeText(this, "Paid - Unlocked!", Toast.LENGTH_LONG).show()
        }.setNegativeButton("Cancel", null).show()
    }

    private fun checkLocked(): Boolean { if (!isPaid()) { showPayment(); return true }; return false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)

        // Ask Camera + Mic permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }

        val root = LinearLayout(this); root.orientation = LinearLayout.VERTICAL; root.setBackgroundColor(Color.BLACK)

        // REAL CAMERA PREVIEW - replaces grey box
        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
        root.addView(previewView)

        // Init RTMP Camera - connects preview to RTMP
        rtmpCamera = RtmpCamera2(previewView, this)

        tvWatermark = TextView(this); tvWatermark.text = "JSTV • Jehovah Shalom TV"; tvWatermark.setTextColor(Color.YELLOW); tvWatermark.gravity = Gravity.END; root.addView(tvWatermark)
        tvTicker = TextView(this); tvTicker.text = "Welcome to Jehovah Shalom TV..."; tvTicker.setTextColor(Color.WHITE); tvTicker.setBackgroundColor(Color.RED); tvTicker.gravity = Gravity.CENTER; root.addView(tvTicker)
        tvVerse = TextView(this); tvVerse.text = "Psalm 122:1"; tvVerse.setTextColor(Color.WHITE); tvVerse.gravity = Gravity.CENTER; root.addView(tvVerse)
        tvLogo = TextView(this); tvLogo.text = "✝️ JSTV LOGO"; tvLogo.setTextColor(Color.YELLOW); tvLogo.gravity = Gravity.CENTER; root.addView(tvLogo)
        tvBible = TextView(this); tvBible.text = "📖 BIBLE OPEN"; tvBible.setTextColor(Color.WHITE); tvBible.gravity = Gravity.CENTER; root.addView(tvBible)
        tvStatus = TextView(this); tvStatus.text = if (isPaid()) "PAID - Unlocked" else "NOT PAID - Locked 🔒"; tvStatus.setTextColor(Color.YELLOW); tvStatus.gravity = Gravity.CENTER; root.addView(tvStatus)

        // MENU 2x2
        val menu = GridLayout(this); menu.columnCount = 2; menu.rowCount = 2
        val btnStudio = Button(this); btnStudio.text = "MAIN STUDIO"
        val btnRemote = Button(this); btnRemote.text = "REMOTE"
        val btnWireless = Button(this); btnWireless.text = "WIRELESS CAM"
        val btnTv = Button(this); btnTv.text = "TV / BIG SCREEN"
        for (b in listOf(btnStudio, btnRemote, btnWireless, btnTv)) {
            val p = GridLayout.LayoutParams(); p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); p.width = 0; b.layoutParams = p; menu.addView(b)
        }
        root.addView(menu)

        // MAIN STUDIO
        val studio = LinearLayout(this); studio.orientation = LinearLayout.VERTICAL; studio.setBackgroundColor(Color.WHITE); studio.setPadding(40, 30, 40, 30); studio.visibility = LinearLayout.GONE; root.addView(studio)
        val title = TextView(this); title.text = "MAIN STUDIO - REAL STREAM"; title.textSize = 20f; title.setTextColor(Color.BLACK); title.gravity = Gravity.CENTER; studio.addView(title)

        val etChannel = EditText(this); etChannel.hint = "Channel Name"; etChannel.setText(prefs.getString("name", "Jehovah Shalom TV")); studio.addView(etChannel)
        val etTicker = EditText(this); etTicker.hint = "Ticker Text"; studio.addView(etTicker)
        val etVerse = EditText(this); etVerse.hint = "Verse"; studio.addView(etVerse)

        // NEW: RTMP URL + STREAM KEY - THIS IS REAL STREAMING
        val rtmpLabel = TextView(this); rtmpLabel.text = "🔴 REAL STREAM SETUP - Paste from Facebook / YouTube:"; studio.addView(rtmpLabel)
        val etRtmp = EditText(this); etRtmp.hint = "RTMP URL - e.g. rtmp://live-api-s.facebook.com:80/rtmp/"; etRtmp.setText(rtmpUrl); studio.addView(etRtmp)
        val etKey = EditText(this); etKey.hint = "STREAM KEY - Paste secret key here"; studio.addView(etKey)

        val cbLogo = CheckBox(this); cbLogo.text = "Show Logo"; cbLogo.isChecked = true; studio.addView(cbLogo)
        val cbVerse = CheckBox(this); cbVerse.text = "Show Verse"; cbVerse.isChecked = true; studio.addView(cbVerse)
        val cbTicker = CheckBox(this); cbTicker.text = "Show Ticker"; cbTicker.isChecked = true; studio.addView(cbTicker)
        val cbBible = CheckBox(this); cbBible.text = "Show Bible"; studio.addView(cbBible)

        val destLabel = TextView(this); destLabel.text = "Destination - 1 at a time:"; studio.addView(destLabel)
        val rgDest = RadioGroup(this)
        val rbFb = RadioButton(this); rbFb.text = "Facebook"; rbFb.isChecked = true
        val rbYt = RadioButton(this); rbYt.text = "YouTube"
        val rbTt = RadioButton(this); rbTt.text = "TikTok"
        rgDest.addView(rbFb); rgDest.addView(rbYt); rgDest.addView(rbTt); studio.addView(rgDest)

        val camRow = LinearLayout(this); camRow.orientation = LinearLayout.HORIZONTAL
        val btnFrontBack = Button(this); btnFrontBack.text = "FRONT / BACK CAM"
        val btnMic = Button(this); btnMic.text = "MIC: ON"
        camRow.addView(btnFrontBack); camRow.addView(btnMic); studio.addView(camRow)

        val btnApply = Button(this); btnApply.text = "APPLY SETTINGS"; studio.addView(btnApply)
        val btnGoLive = Button(this); btnGoLive.text = "🔴 GO LIVE FOR REAL"; btnGoLive.setBackgroundColor(Color.RED); btnGoLive.setTextColor(Color.WHITE); studio.addView(btnGoLive)
        val btnPay2 = Button(this); btnPay2.text = "PAY TO UNLOCK (M-Pesa / Manual)"; studio.addView(btnPay2)

        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)

        // START REAL CAMERA PREVIEW
        try { if (!rtmpCamera.isStreaming) rtmpCamera.startPreview() } catch (e: Exception) { Toast.makeText(this, "Camera: ${e.message}", Toast.LENGTH_SHORT).show() }

        btnStudio.setOnClickListener { studio.visibility = if (studio.visibility == LinearLayout.GONE) LinearLayout.VISIBLE else LinearLayout.GONE }
        btnRemote.setOnClickListener { if (checkLocked()) return@setOnClickListener; Toast.makeText(this, "Remote Control", Toast.LENGTH_SHORT).show() }
        btnWireless.setOnClickListener { if (checkLocked()) return@setOnClickListener; Toast.makeText(this, "Wireless Camera", Toast.LENGTH_SHORT).show() }
        btnTv.setOnClickListener { if (checkLocked()) return@setOnClickListener; Toast.makeText(this, "TV / Big Screen", Toast.LENGTH_SHORT).show() }

        fun lockedTap(block: () -> Unit) { if (!checkLocked()) block() }
        cbLogo.setOnClickListener { lockedTap { tvLogo.visibility = if (cbLogo.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbVerse.setOnClickListener { lockedTap { tvVerse.visibility = if (cbVerse.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbTicker.setOnClickListener { lockedTap { tvTicker.visibility = if (cbTicker.isChecked) TextView.VISIBLE else TextView.GONE } }
        cbBible.setOnClickListener { lockedTap { tvBible.visibility = if (cbBible.isChecked) TextView.VISIBLE else TextView.GONE } }
        rbFb.setOnClickListener { selectedDest = "Facebook"; etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/") }
        rbYt.setOnClickListener { selectedDest = "YouTube"; etRtmp.setText("rtmp://a.rtmp.youtube.com/live2/") }
        rbTt.setOnClickListener { selectedDest = "TikTok"; etRtmp.setText("rtmp://push-rtmp-l1.tiktokcdn.com/live/") }

        btnFrontBack.setOnClickListener { lockedTap { isFront = !isFront; try { rtmpCamera.switchCamera() } catch (e: Exception) {} } }
        btnMic.setOnClickListener { lockedTap { try { if (rtmpCamera.isAudioMuted) { rtmpCamera.enableAudio(); btnMic.text = "MIC: ON" } else { rtmpCamera.disableAudio(); btnMic.text = "MIC: OFF" } } catch (e: Exception) {} } }
        btnApply.setOnClickListener { lockedTap { tvTicker.text = etTicker.text.toString(); tvVerse.text = etVerse.text.toString(); Toast.makeText(this, "Applied - Watermark Always ON", Toast.LENGTH_SHORT).show() } }

        btnGoLive.setOnClickListener { lockedTap {
            if (!isLive) {
                rtmpUrl = etRtmp.text.toString().trim()
                streamKey = etKey.text.toString().trim()
                if (streamKey.isEmpty()) { Toast.makeText(this, "Paste STREAM KEY first! Get it from Facebook Live / YouTube Studio", Toast.LENGTH_LONG).show(); return@lockedTap }
                val fullUrl = rtmpUrl + streamKey
                try {
                    if (!rtmpCamera.isStreaming) rtmpCamera.startStream(fullUrl)
                    isLive = true; btnGoLive.text = "⏹ STOP LIVE"
                    tvStatus.text = "🔴 LIVE NOW - $selectedDest - JSTV Watermark ON"
                    Toast.makeText(this, "LIVE to $selectedDest!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(this, "Stream failed: ${e.message}", Toast.LENGTH_LONG).show() }
            } else {
                try { rtmpCamera.stopStream() } catch (e: Exception) {}
                isLive = false; btnGoLive.text = "🔴 GO LIVE FOR REAL"; tvStatus.text = "LIVE STOPPED"
            }
        }}
        btnPay2.setOnClickListener { showPayment() }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { if (::rtmpCamera.isInitialized) { rtmpCamera.stopStream(); rtmpCamera.stopPreview() } } catch (e: Exception) {}
    }
}
