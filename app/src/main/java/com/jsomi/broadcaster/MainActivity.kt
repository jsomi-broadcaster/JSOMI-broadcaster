package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var camera: Camera? = null
    private var selector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var isLive = false
    private var isRecording = false
    private var isMicMuted = false
    private var isFlash = false
    private var viewers = 0
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private val perms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) startCamera()
    }
    private val timer = object : Runnable {
        override fun run() {
            if (isLive) {
                seconds++; viewers += (0..3).random()
                val m = String.format("%02d:%02d", seconds/60, seconds%60)
                findViewById<TextView>(R.id.tvViewers).text = "👁 $viewers viewers | ⏱ $m"
                handler.postDelayed(this, 1000)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        setupSpinner(R.id.spResolution, listOf("1080p - Full HD", "720p - HD", "480p - SD", "360p - Low"))
        setupSpinner(R.id.spBitrate, listOf("4000 kbps - High", "2500 kbps - Medium", "1000 kbps - Low", "500 kbps - Data Saver"))
        setupSpinner(R.id.spFps, listOf("30 FPS", "60 FPS", "24 FPS"))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else perms.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        findViewById<Button>(R.id.btnSwitchCamera).setOnClickListener {
            selector = if (selector == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera()
        }
        findViewById<Button>(R.id.btnFlash).setOnClickListener {
            isFlash = !isFlash
            if (camera?.cameraInfo?.hasFlashUnit() == true) camera?.cameraControl?.enableTorch(isFlash)
            Toast.makeText(this, if (isFlash) "Flash ON" else "Flash OFF", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnMic).setOnClickListener {
            isMicMuted = !isMicMuted
            (it as Button).text = if (isMicMuted) "🔇 MUTED" else "🎤 MIC ON"
        }
        findViewById<Button>(R.id.btnAudioOnly).setOnClickListener { Toast.makeText(this, "Audio Only Mode", Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            isRecording = !isRecording
            (it as Button).text = if (isRecording) "⏹ STOP REC" else "⏺ RECORD"
        }
        findViewById<Button>(R.id.btnGoLive).setOnClickListener {
            val title = findViewById<EditText>(R.id.etStreamTitle).text.toString().ifEmpty { "JSTV Live" }
            val rtmp = findViewById<EditText>(R.id.etRtmpUrl).text.toString()
            val key = findViewById<EditText>(R.id.etStreamKey).text.toString()
            val res = findViewById<Spinner>(R.id.spResolution).selectedItem.toString()
            val bit = findViewById<Spinner>(R.id.spBitrate).selectedItem.toString()
            if (!isLive) {
                isLive = true; seconds = 0; viewers = 1
                findViewById<TextView>(R.id.tvLiveStatus).text = "● LIVE - $title | $res | $bit"
                (it as Button).text = "⏹ END LIVE"
                getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("rtmp", rtmp).putString("key", key).putString("title", title).apply()
                Toast.makeText(this, "LIVE STARTED: $title", Toast.LENGTH_LONG).show()
                handler.post(timer)
            } else {
                isLive = false
                findViewById<TextView>(R.id.tvLiveStatus).text = "● OFFLINE"
                (it as Button).text = "🔴 GO LIVE"
                Toast.makeText(this, "Live Ended", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnChurchScreen).setOnClickListener { startActivity(Intent(this, ChurchScreenActivity::class.java)) }
        findViewById<Button>(R.id.btnPayment).setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
    private fun setupSpinner(id: Int, items: List<String>) {
        findViewById<Spinner>(id).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
    }
    private fun startCamera() {
        val f = ProcessCameraProvider.getInstance(this)
        f.addListener({
            val p = f.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try { p.unbindAll(); camera = p.bindToLifecycle(this, selector, preview) } catch (e: Exception) { Toast.makeText(this, "Camera: ${e.message}", Toast.LENGTH_LONG).show() }
        }, ContextCompat.getMainExecutor(this))
    }
}
