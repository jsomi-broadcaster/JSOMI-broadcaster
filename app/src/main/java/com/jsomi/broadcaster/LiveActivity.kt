package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
    private var isMicOn = true
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private fun isPaid(): Boolean {
        return getSharedPreferences("JSTV", MODE_PRIVATE).getBoolean("isPaid", false)
    }

    private fun showPayment() {
        val options = arrayOf("Weekly - KSh 200", "Monthly - KSh 1,000", "3 Months - KSh 2,500")
        AlertDialog.Builder(this)
            .setTitle("🔒 Premium Locked")
            .setItems(options) { _, which ->
                getSharedPreferences("JSTV", MODE_PRIVATE).edit().putBoolean("isPaid", true).apply()
                Toast.makeText(this, "${options[which]} - Unlocked!", Toast.LENGTH_LONG).show()
                updateStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus() {
        tvStatus.text = if (isPaid()) "PAID - Unlocked" else "FREE - Tap PAY to Unlock"
    }

    private fun startCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Toast.makeText(this, "CameraX: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        setContentView(root)

        // --- CAMERA PREVIEW - Visible at first ---
        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(previewView)

        // --- RTMP VIEW - HIDDEN until GO LIVE ---
        openGlView = OpenGlView(this)
        openGlView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        openGlView.visibility = View.GONE
        root.addView(openGlView)
        rtmpCamera = RtmpCamera2(openGlView, this)

        // --- OVERLAYS ---
        tvWatermark = TextView(this)
        tvWatermark.text = "JSTV • Jehovah Shalom TV"
        tvWatermark.setTextColor(Color.WHITE)
        tvWatermark.gravity = Gravity.CENTER
        root.addView(tvWatermark)

        tvTicker = TextView(this)
        tvTicker.text = "Welcome to Jehovah Shalom TV..."
        tvTicker.setTextColor(Color.YELLOW)
        tvTicker.gravity = Gravity.CENTER
        root.addView(tvTicker)

        tvVerse = TextView(this)
        tvVerse.text = "Psalm 122:1"
        tvVerse.setTextColor(Color.WHITE)
        tvVerse.gravity = Gravity.CENTER
        root.addView(tvVerse)

        tvLogo = TextView(this)
        tvLogo.text = "✝️ JSTV LOGO"
        tvLogo.setTextColor(Color.WHITE)
        tvLogo.gravity = Gravity.CENTER
        root.addView(tvLogo)

        tvBible = TextView(this)
        tvBible.text = "📖 BIBLE OPEN"
        tvBible.setTextColor(Color.WHITE)
        tvBible.gravity = Gravity.CENTER
        root.addView(tvBible)

        tvStatus = TextView(this)
        tvStatus.setTextColor(Color.GREEN)
        tvStatus.gravity = Gravity.CENTER
        root.addView(tvStatus)
        updateStatus()

        // --- MENU ---
        val menu = GridLayout(this)
        menu.columnCount = 2
        menu.rowCount = 2
        val btnStudio = Button(this); btnStudio.text = "MAIN STUDIO"
        val btnRemote = Button(this); btnRemote.text = "REMOTE"
        val btnWireless = Button(this); btnWireless.text = "WIRELESS CAM"
        val btnTv = Button(this); btnTv.text = "TV / BIG SCREEN"
        for (b in listOf(btnStudio, btnRemote, btnWireless, btnTv)) {
            val p = GridLayout.LayoutParams()
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            p.width = 0
            b.layoutParams = p
            menu.addView(b)
        }
        root.addView(menu)

        // --- STUDIO CONTROLS ---
        val studio = LinearLayout(this)
        studio.orientation = LinearLayout.VERTICAL
        root.addView(studio)

        val title = TextView(this)
        title.text = "MAIN STUDIO - REAL STREAM"
        title.setTextColor(Color.WHITE)
        studio.addView(title)

        val etChannel = EditText(this); etChannel.hint = "Channel Name"; studio.addView(etChannel)
        val etTicker = EditText(this); etTicker.hint = "Ticker Text"; studio.addView(etTicker)
        val etVerse = EditText(this); etVerse.hint = "Verse"; studio.addView(etVerse)

        val rtmpLabel = TextView(this)
        rtmpLabel.text = "🔴 REAL STREAM - Paste from Facebook / YouTube"
        rtmpLabel.setTextColor(Color.RED)
        studio.addView(rtmpLabel)

        val etRtmp = EditText(this); etRtmp.hint = "RTMP URL"; etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/"); studio.addView(etRtmp)
        val etKey = EditText(this); etKey.hint = "STREAM KEY - Paste secret key"; studio.addView(etKey)

        val cbLogo = CheckBox(this); cbLogo.text = "Show Logo"; cbLogo.isChecked = true; studio.addView(cbLogo)
        val cbVerse = CheckBox(this); cbVerse.text = "Show Verse"; cbVerse.isChecked = true; studio.addView(cbVerse)
        val cbTicker = CheckBox(this); cbTicker.text = "Show Ticker"; cbTicker.isChecked = true; studio.addView(cbTicker)
        val cbBible = CheckBox(this); cbBible.text = "Show Bible"; cbBible.isChecked = true; studio.addView(cbBible)

        val destLabel = TextView(this); destLabel.text = "Destination:"; destLabel.setTextColor(Color.WHITE); studio.addView(destLabel)
        val rgDest = RadioGroup(this)
        val rbFb = RadioButton(this); rbFb.text = "Facebook"; rbFb.isChecked = true
        val rbYt = RadioButton(this); rbYt.text = "YouTube"
        val rbTt = RadioButton(this); rbTt.text = "TikTok"
        rgDest.addView(rbFb); rgDest.addView(rbYt); rgDest.addView(rbTt)
        studio.addView(rgDest)

        val camRow = LinearLayout(this)
        val btnFrontBack = Button(this); btnFrontBack.text = "Switch Camera"
        val btnMic = Button(this); btnMic.text = "Mic ON"
        camRow.addView(btnFrontBack); camRow.addView(btnMic)
        studio.addView(camRow)

        val btnApply = Button(this); btnApply.text = "APPLY OVERLAYS"
        val btnGoLive = Button(this); btnGoLive.text = "🔴 GO LIVE"
        val btnPay2 = Button(this); btnPay2.text = "💰 PAY / UNLOCK"
        studio.addView(btnApply); studio.addView(btnGoLive); studio.addView(btnPay2)

        val scroll = ScrollView(this)
        // Note: root already has studio, we keep as is for simple layout

        startCameraX()

        btnStudio.setOnClickListener { Toast.makeText(this, "MAIN STUDIO", Toast.LENGTH_SHORT).show() }
        btnRemote.setOnClickListener { Toast.makeText(this, "REMOTE", Toast.LENGTH_SHORT).show() }
        btnWireless.setOnClickListener { Toast.makeText(this, "WIRELESS CAM", Toast.LENGTH_SHORT).show() }
        btnTv.setOnClickListener { Toast.makeText(this, "TV / BIG SCREEN", Toast.LENGTH_SHORT).show() }

        fun lockedTap(block: () -> Unit) {
            if (!isPaid()) { showPayment(); return }
            block()
        }

        cbLogo.setOnClickListener { lockedTap { tvLogo.visibility = if (cbLogo.isChecked) View.VISIBLE else View.GONE } }
        cbVerse.setOnClickListener { lockedTap { tvVerse.visibility = if (cbVerse.isChecked) View.VISIBLE else View.GONE } }
        cbTicker.setOnClickListener { lockedTap { tvTicker.visibility = if (cbTicker.isChecked) View.VISIBLE else View.GONE } }
        cbBible.setOnClickListener { lockedTap { tvBible.visibility = if (cbBible.isChecked) View.VISIBLE else View.GONE } }

        rbFb.setOnClickListener { selectedDest = "Facebook"; etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/") }
        rbYt.setOnClickListener { selectedDest = "YouTube"; etRtmp.setText("rtmp://a.rtmp.youtube.com/live2/") }
        rbTt.setOnClickListener { selectedDest = "TikTok"; etRtmp.setText("rtmp://push-rtmp-l1.tiktokcdn.com/live/") }

        btnFrontBack.setOnClickListener { lockedTap {
            isFront = !isFront
            cameraSelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            if (!isLive) startCameraX()
            Toast.makeText(this, "Camera Switched", Toast.LENGTH_SHORT).show()
        } }

        btnMic.setOnClickListener { lockedTap {
            isMicOn = !isMicOn
            btnMic.text = if (isMicOn) "Mic ON" else "Mic OFF"
            Toast.makeText(this, "Mic toggle - RTMP will handle on GO LIVE", Toast.LENGTH_SHORT).show()
        } }

        btnApply.setOnClickListener { lockedTap {
            tvTicker.text = etTicker.text.toString()
            tvVerse.text = etVerse.text.toString()
            tvLogo.visibility = if (cbLogo.isChecked) View.VISIBLE else View.GONE
            tvVerse.visibility = if (cbVerse.isChecked) View.VISIBLE else View.GONE
            tvTicker.visibility = if (cbTicker.isChecked) View.VISIBLE else View.GONE
            tvBible.visibility = if (cbBible.isChecked) View.VISIBLE else View.GONE
            Toast.makeText(this, "Overlays Applied", Toast.LENGTH_SHORT).show()
        } }

        btnGoLive.setOnClickListener { lockedTap {
            if (!isLive) {
                val url = etRtmp.text.toString().trim()
                val key = etKey.text.toString().trim()
                if (key.isEmpty()) { Toast.makeText(this, "Paste STREAM KEY first!", Toast.LENGTH_LONG).show(); return@lockedTap }
                try {
                    previewView.visibility = View.GONE
                    openGlView.visibility = View.VISIBLE
                    if (!rtmpCamera.isStreaming) {
                        rtmpCamera.startPreview()
                        rtmpCamera.startStream(url + key)
                    }
                    isLive = true; btnGoLive.text = "⏹ STOP LIVE"
                    tvStatus.text = "🔴 LIVE NOW - $selectedDest"
                    Toast.makeText(this, "LIVE NOW", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Stream failed: ${e.message}", Toast.LENGTH_LONG).show() }
            } else {
                try {
                    rtmpCamera.stopStream()
                    rtmpCamera.stopPreview()
                    openGlView.visibility = View.GONE
                    previewView.visibility = View.VISIBLE
                    startCameraX()
                    isLive = false; btnGoLive.text = "🔴 GO LIVE"
                    tvStatus.text = "LIVE STOPPED"
                } catch (e: Exception) { }
            }
        } }

        btnPay2.setOnClickListener { showPayment() }
    }
}
