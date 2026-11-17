package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LiveActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvWatermark: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvVerse: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvStatus: TextView

    private var isLive = false
    private var isFront = false
    private var isMicOn = true
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera: " + e.message, Toast.LENGTH_LONG).show()
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

        // CAMERA - Big top area
        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
        root.addView(previewView)

        tvWatermark = TextView(this)
        tvWatermark.text = "JSTV - Jehovah Shalom TV"
        tvWatermark.setTextColor(Color.WHITE)
        tvWatermark.textSize = 18f
        tvWatermark.gravity = Gravity.CENTER
        root.addView(tvWatermark)

        tvTicker = TextView(this)
        tvTicker.text = "Welcome to Jehovah Shalom TV..."
        tvTicker.setTextColor(Color.YELLOW)
        tvTicker.textSize = 16f
        tvTicker.gravity = Gravity.CENTER
        root.addView(tvTicker)

        tvVerse = TextView(this)
        tvVerse.text = "Psalm 122:1"
        tvVerse.setTextColor(Color.WHITE)
        tvVerse.textSize = 16f
        tvVerse.gravity = Gravity.CENTER
        root.addView(tvVerse)

        tvLogo = TextView(this)
        tvLogo.text = "JSTV LOGO"
        tvLogo.setTextColor(Color.WHITE)
        tvLogo.gravity = Gravity.CENTER
        root.addView(tvLogo)

        tvBible = TextView(this)
        tvBible.text = "BIBLE OPEN"
        tvBible.setTextColor(Color.WHITE)
        tvBible.gravity = Gravity.CENTER
        root.addView(tvBible)

        tvStatus = TextView(this)
        tvStatus.text = "READY - FREE MODE - ALL UNLOCKED"
        tvStatus.setTextColor(Color.GREEN)
        tvStatus.gravity = Gravity.CENTER
        root.addView(tvStatus)

        // SCROLL FOR CONTROLS
        val scroll = ScrollView(this)
        val studio = LinearLayout(this)
        studio.orientation = LinearLayout.VERTICAL
        studio.setPadding(24, 24, 24, 24)
        scroll.addView(studio)
        root.addView(scroll)

        val title = TextView(this)
        title.text = "MAIN STUDIO - CAMERA PREVIEW"
        title.setTextColor(Color.WHITE)
        title.textSize = 18f
        studio.addView(title)

        val etTicker = EditText(this)
        etTicker.hint = "Ticker Text"
        etTicker.setHintTextColor(Color.GRAY)
        etTicker.setTextColor(Color.WHITE)
        studio.addView(etTicker)

        val etVerse = EditText(this)
        etVerse.hint = "Verse"
        etVerse.setHintTextColor(Color.GRAY)
        etVerse.setTextColor(Color.WHITE)
        studio.addView(etVerse)

        val etRtmp = EditText(this)
        etRtmp.hint = "RTMP URL"
        etRtmp.setHintTextColor(Color.GRAY)
        etRtmp.setTextColor(Color.WHITE)
        etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/")
        studio.addView(etRtmp)

        val etKey = EditText(this)
        etKey.hint = "STREAM KEY"
        etKey.setHintTextColor(Color.GRAY)
        etKey.setTextColor(Color.WHITE)
        studio.addView(etKey)

        // FIXED: Checkboxes now have WHITE text so you can see them
        val cbLogo = CheckBox(this)
        cbLogo.text = "Show Logo"
        cbLogo.setTextColor(Color.WHITE)
        cbLogo.isChecked = true
        studio.addView(cbLogo)

        val cbVerse = CheckBox(this)
        cbVerse.text = "Show Verse"
        cbVerse.setTextColor(Color.WHITE)
        cbVerse.isChecked = true
        studio.addView(cbVerse)

        val cbTicker = CheckBox(this)
        cbTicker.text = "Show Ticker"
        cbTicker.setTextColor(Color.WHITE)
        cbTicker.isChecked = true
        studio.addView(cbTicker)

        val cbBible = CheckBox(this)
        cbBible.text = "Show Bible"
        cbBible.setTextColor(Color.WHITE)
        cbBible.isChecked = true
        studio.addView(cbBible)

        val btnFrontBack = Button(this)
        btnFrontBack.text = "Switch Camera"
        studio.addView(btnFrontBack)

        val btnMic = Button(this)
        btnMic.text = "Mic ON"
        studio.addView(btnMic)

        val btnApply = Button(this)
        btnApply.text = "APPLY OVERLAYS"
        studio.addView(btnApply)

        val btnGoLive = Button(this)
        btnGoLive.text = "GO LIVE (PREVIEW)"
        studio.addView(btnGoLive)

        startCamera()

        btnFrontBack.setOnClickListener {
            isFront = !isFront
            cameraSelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
            Toast.makeText(this, if (isFront) "Front Camera" else "Back Camera", Toast.LENGTH_SHORT).show()
        }

        btnMic.setOnClickListener {
            isMicOn = !isMicOn
            btnMic.text = if (isMicOn) "Mic ON" else "Mic OFF"
            Toast.makeText(this, btnMic.text, Toast.LENGTH_SHORT).show()
        }

        btnApply.setOnClickListener {
            if (etTicker.text.toString().isNotEmpty()) tvTicker.text = etTicker.text.toString()
            if (etVerse.text.toString().isNotEmpty()) tvVerse.text = etVerse.text.toString()
            tvLogo.visibility = if (cbLogo.isChecked) View.VISIBLE else View.GONE
            tvVerse.visibility = if (cbVerse.isChecked) View.VISIBLE else View.GONE
            tvTicker.visibility = if (cbTicker.isChecked) View.VISIBLE else View.GONE
            tvBible.visibility = if (cbBible.isChecked) View.VISIBLE else View.GONE
            Toast.makeText(this, "Overlays Applied", Toast.LENGTH_SHORT).show()
        }

        btnGoLive.setOnClickListener {
            val key = etKey.text.toString().trim()
            if (key.isEmpty()) { Toast.makeText(this, "Paste STREAM KEY first!", Toast.LENGTH_LONG).show(); return@setOnClickListener }
            if (!isLive) {
                isLive = true
                btnGoLive.text = "STOP LIVE"
                tvStatus.text = "LIVE NOW - PREVIEW MODE"
                Toast.makeText(this, "LIVE NOW", Toast.LENGTH_SHORT).show()
            } else {
                isLive = false
                btnGoLive.text = "GO LIVE (PREVIEW)"
                tvStatus.text = "READY - FREE MODE - ALL UNLOCKED"
                Toast.makeText(this, "LIVE STOPPED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
