package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.rtmp.rtmp.RtmpCamera2
import com.pedro.rtmp.utils.OpenGlView

class LiveActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var openGlView: OpenGlView
    private lateinit var tvWatermark: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvVerse: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvStatus: TextView
    private lateinit var rtmpCamera: RtmpCamera2
    private var isLive = false
    private var selectedDest = "Facebook"
    private var isFront = false
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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

    private fun startCameraX() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) { Toast.makeText(this, "CameraX: ${e.message}", Toast.LENGTH_SHORT).show() }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }

        val root = LinearLayout(this); root.orientation = LinearLayout.VERTICAL; root.setBackgroundColor(Color.BLACK)

        // CAMERA X - PREVIEW THAT WORKS - Visible at first
        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
        root.addView(previewView)

        // RTMP - HIDDEN until GO LIVE - Only for streaming
        openGlView = OpenGlView(this)
        openGlView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
        openGlView.visibility = LinearLayout.GONE
        root.addView(openGlView)
        rtmpCamera = RtmpCamera2(openGlView, this)

        tvWatermark = TextView(this); tvWatermark.text = "JSTV • Jehovah Shalom TV"; tvWatermark.setTextColor(Color.YELLOW); tvWatermark.gravity = Gravity.END; root.addView(tvWatermark)
        tvTicker = TextView(this); tvTicker.text = "Welcome to Jehovah Shalom TV..."; tvTicker.setTextColor(Color.WHITE); tvTicker.setBackgroundColor(Color.RED); tvTicker.gravity = Gravity.CENTER; root.addView(tvTicker)
        tvVerse = TextView(this); tvVerse.text = "Psalm 122:1"; tvVerse.setTextColor(Color.WHITE); tvVerse.gravity = Gravity.CENTER; root.addView(tvVerse)
        tvLogo = TextView(this); tvLogo.text = "✝️ JSTV LOGO"; tvLogo.setTextColor(Color.YELLOW); tvLogo.gravity = Gravity.CENTER; root.addView(tvLogo)
        tvBible = TextView(this); tvBible.text = "📖 BIBLE OPEN"; tvBible.setTextColor(Color.WHITE); tvBible.gravity = Gravity.CENTER; root.addView(tvBible)
        tvStatus = TextView(this); tvStatus.text = if (isPaid()) "PAID - Unlocked" else "NOT PAID - Locked 🔒"; tvStatus.setTextColor(Color.YELLOW); tvStatus.gravity = Gravity.CENTER; root.addView(tvStatus)

        val menu = GridLayout(this); menu.columnCount = 2; menu.rowCount = 2
        val btnStudio = Button(this); btnStudio.text = "MAIN STUDIO"
        val btnRemote = Button(this); btnRemote.text = "REMOTE"
        val btnWireless = Button(this); btnWireless.text = "WIRELESS CAM"
        val btnTv = Button(this); btnTv.text = "TV / BIG SCREEN"
        for (b in listOf(btnStudio, btnRemote, btnWireless, btnTv)) {
            val p = GridLayout.LayoutParams(); p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); p.width = 0; b.layoutParams = p; menu.addView(b)
        }
        root.addView(menu)

        val studio = LinearLayout(this); studio.orientation = LinearLayout.VERTICAL; studio.setBackgroundColor(Color.WHITE); studio.setPadding(40, 30, 40, 30); studio.visibility = LinearLayout.GONE; root.addView(studio)
        val title = TextView(this); title.text = "MAIN STUDIO - REAL STREAM"; title.textSize = 20f; title.setTextColor(Color.BLACK); title.gravity = Gravity.CENTER; studio.addView(title)

        val etChannel = EditText(this); etChannel.hint = "Channel Name"; etChannel.setText(prefs.getString("name", "Jehovah Shalom TV")); studio.addView(etChannel)
        val etTicker = EditText(this); etTicker.hint = "Ticker Text"; studio.addView(etTicker)
        val etVerse = EditText(this); etVerse.hint = "Verse"; studio.addView(etVerse)

        val rtmpLabel = TextView(this); rtmpLabel.text = "🔴 REAL STREAM - Paste from Facebook / YouTube:"; studio.addView(rtmpLabel)
        val etRtmp = EditText(this); etRtmp.hint = "RTMP URL"; etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/"); studio.addView(etRtmp)
        val etKey = EditText(this); etKey.hint = "STREAM KEY - Paste secret key"; studio.addView(etKey)

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

        // START CameraX preview - the one that works
        startCameraX()

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
        btnFrontBack.setOnClickListener { lockedTap { isFront = !isFront; cameraSelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA; startCameraX() } }
        btnMic.setOnClickListener { lockedTap { Toast.makeText(this, "Mic toggle - RTMP will handle on GO LIVE", Toast.LENGTH_SHORT).show() } }
        btnApply.setOnClickListener { lockedTap { tvTicker.text = etTicker.text.toString(); tvVerse.text = etVerse.text.toString(); Toast.makeText(this, "Applied - Watermark Always ON", Toast.LENGTH_SHORT).show() } }

        btnGoLive.setOnClickListener { lockedTap {
            if (!isLive) {
                val url = etRtmp.text.toString().trim()
                val key = etKey.text.toString().trim()
                if (key.isEmpty()) { Toast.makeText(this, "Paste STREAM KEY first!", Toast.LENGTH_LONG).show(); return@lockedTap }
                try {
                    // Switch from CameraX preview to RTMP streaming view
                    previewView.visibility = LinearLayout.GONE
                    openGlView.visibility = LinearLayout.VISIBLE
                    if (!rtmpCamera.isStreaming) { rtmpCamera.startPreview(); rtmpCamera.startStream(url + key) }
                    isLive = true; btnGoLive.text = "⏹ STOP LIVE"; tvStatus.text = "🔴 LIVE NOW - $selectedDest"; Toast.makeText(this, "LIVE to $selectedDest!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(this, "Stream failed: ${e.message}", Toast.LENGTH_LONG).show(); previewView.visibility = LinearLayout.VISIBLE; openGlView.visibility = LinearLayout.GONE }
            } else {
                try { rtmpCamera.stopStream(); rtmpCamera.stopPreview() } catch (e: Exception) {}
                openGlView.visibility = LinearLayout.GONE
                previewView.visibility = LinearLayout.VISIBLE
                startCameraX()
                isLive = false; btnGoLive.text = "🔴 GO LIVE FOR REAL"; tvStatus.text = "LIVE STOPPED"
            }
        }}
        btnPay2.setOnClickListener { showPayment() }
    }
}
