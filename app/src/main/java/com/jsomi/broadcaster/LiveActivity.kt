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

class LiveActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvWatermark: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvVerse: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvStatus: TextView

    private var isLive = false
    private var selectedDest = "Facebook"
    private var isFront = false
    private var isMicOn = true
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private fun isPaid(): Boolean {
        return getSharedPreferences("JSTV", MODE_PRIVATE).getBoolean("isPaid", false)
    }

    private fun showPayment() {
        val options = arrayOf("Weekly - KSh 200", "Monthly - KSh 1000", "3 Months - KSh 2500")
        AlertDialog.Builder(this)
            .setTitle("Premium Locked")
            .setItems(options) { _, which ->
                getSharedPreferences("JSTV", MODE_PRIVATE).edit().putBoolean("isPaid", true).apply()
                Toast.makeText(this, "Unlocked: " + options[which], Toast.LENGTH_LONG).show()
                updateStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus() {
        if (::tvStatus.isInitialized) {
            tvStatus.text = if (isPaid()) "PAID - Unlocked" else "FREE - Tap PAY to Unlock"
        }
    }

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

        previewView = PreviewView(this)
        previewView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(previewView)

        tvWatermark = TextView(this)
        tvWatermark.text = "JSTV - Jehovah Shalom TV"
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
        tvStatus.setTextColor(Color.GREEN)
        tvStatus.gravity = Gravity.CENTER
        root.addView(tvStatus)
        updateStatus()

        val scroll = ScrollView(this)
        val studio = LinearLayout(this)
        studio.orientation = LinearLayout.VERTICAL
        scroll.addView(studio)
        root.addView(scroll)

        val title = TextView(this)
        title.text = "MAIN STUDIO - CAMERA PREVIEW"
        title.setTextColor(Color.WHITE)
        studio.addView(title)

        val etTicker = EditText(this)
        etTicker.hint = "Ticker Text"
        studio.addView(etTicker)

        val etVerse = EditText(this)
        etVerse.hint = "Verse"
        studio.addView(etVerse)

        val etRtmp = EditText(this)
        etRtmp.hint = "RTMP URL"
        etRtmp.setText("rtmp://live-api-s.facebook.com:80/rtmp/")
        studio.addView(etRtmp)

        val etKey = EditText(this)
        etKey.hint = "STREAM KEY"
        studio.addView(etKey)

        val cbLogo = CheckBox(this)
        cbLogo.text = "Show Logo"
        cbLogo.isChecked = true
        studio.addView(cbLogo)

        val cbVerse = CheckBox(this)
        cbVerse.text = "Show Verse"
        cbVerse.isChecked = true
        studio.addView(cbVerse)

        val cbTicker = CheckBox(this)
        cbTicker.text = "Show Ticker"
        cbTicker.isChecked = true
        studio.addView(cbTicker)

        val cbBible = CheckBox(this)
        cbBible.text = "Show Bible"
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

        val btnPay = Button(this)
        btnPay.text = "PAY / UNLOCK"
        studio.addView(btnPay)

        startCamera()

        fun lockedTap(block: () -> Unit) {
            if (!isPaid()) { showPayment(); return }
            block()
        }

        btnFrontBack.setOnClickListener { lockedTap {
            isFront = !isFront
            cameraSelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        } }

        btnMic.setOnClickListener { lockedTap {
            isMicOn = !isMicOn
            btnMic.text = if (isMicOn) "Mic ON" else "Mic OFF"
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
            val key = etKey.text.toString().trim()
            if (key.isEmpty()) { Toast.makeText(this, "Paste STREAM KEY first!", Toast.LENGTH_LONG).show(); return@lockedTap }
            if (!isLive) {
                isLive = true
                btnGoLive.text = "STOP LIVE"
                tvStatus.text = "LIVE NOW - " + selectedDest + " (PREVIEW MODE)"
                Toast.makeText(this, "PREVIEW LIVE - RTMP comes next", Toast.LENGTH_SHORT).show()
            } else {
                isLive = false
                btnGoLive.text = "GO LIVE (PREVIEW)"
                tvStatus.text = "LIVE STOPPED"
            }
        } }

        btnPay.setOnClickListener { showPayment() }
    }
}
