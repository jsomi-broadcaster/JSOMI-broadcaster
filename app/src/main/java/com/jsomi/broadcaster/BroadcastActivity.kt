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

class BroadcastActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var isLive = false
    private var isFront = false
    private var isMicOn = true
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA

    private fun startCamera() {
        val f = ProcessCameraProvider.getInstance(this)
        f.addListener({
            try {
                val p = f.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                p.unbindAll()
                p.bindToLifecycle(this, selector, preview)
            } catch (e: Exception) { Toast.makeText(this, "Camera: ${e.message}", Toast.LENGTH_LONG).show() }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        val root = LinearLayout(this); root.orientation = LinearLayout.VERTICAL; root.setBackgroundColor(Color.BLACK); setContentView(root)

        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
        root.addView(previewView)

        val tvLogo = TextView(this); tvLogo.text = "JSTV LOGO"; tvLogo.setTextColor(Color.WHITE); tvLogo.gravity = Gravity.CENTER
        if (prefs.getBoolean("showLogo", true)) root.addView(tvLogo)
        val tvTicker = TextView(this); tvTicker.text = prefs.getString("ticker", "Welcome to JSTV..."); tvTicker.setTextColor(Color.YELLOW); tvTicker.gravity = Gravity.CENTER
        if (prefs.getBoolean("showTicker", true)) root.addView(tvTicker)
        val tvVerse = TextView(this); tvVerse.text = prefs.getString("verse", "Psalm 122:1"); tvVerse.setTextColor(Color.WHITE); tvVerse.gravity = Gravity.CENTER
        if (prefs.getBoolean("showVerse", true)) root.addView(tvVerse)
        val tvBible = TextView(this); tvBible.text = "BIBLE OPEN"; tvBible.setTextColor(Color.WHITE); tvBible.gravity = Gravity.CENTER
        if (prefs.getBoolean("showBible", true)) root.addView(tvBible)

        val tvStatus = TextView(this); tvStatus.text = "READY - ${prefs.getString("dest", "Facebook")} - FREE MODE"; tvStatus.setTextColor(Color.GREEN); tvStatus.gravity = Gravity.CENTER; root.addView(tvStatus)

        val scroll = ScrollView(this); val controls = LinearLayout(this); controls.orientation = LinearLayout.VERTICAL; controls.setPadding(24, 24, 24, 24); scroll.addView(controls); root.addView(scroll)

        val btnCam = Button(this); btnCam.text = "Switch Camera"; controls.addView(btnCam)
        val btnMic = Button(this); btnMic.text = "Mic ON"; controls.addView(btnMic)
        val btnLive = Button(this); btnLive.text = "🔴 GO LIVE"; controls.addView(btnLive)

        startCamera()

        btnCam.setOnClickListener { isFront = !isFront; selector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA; startCamera() }
        btnMic.setOnClickListener { isMicOn = !isMicOn; btnMic.text = if (isMicOn) "Mic ON" else "Mic OFF" }
        btnLive.setOnClickListener {
            if (prefs.getString("streamKey", "").isNullOrEmpty()) { Toast.makeText(this, "Go back, paste Stream Key first", Toast.LENGTH_LONG).show(); return@setOnClickListener }
            if (!isLive) { isLive = true; btnLive.text = "⏹ STOP LIVE"; tvStatus.text = "🔴 LIVE NOW - ${prefs.getString("dest", "")}" }
            else { isLive = false; btnLive.text = "🔴 GO LIVE"; tvStatus.text = "LIVE STOPPED" }
        }
    }
}
