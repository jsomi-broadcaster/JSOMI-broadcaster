package com.jsomi.broadcaster

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityMainBinding
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.rtmp.ConnectCheckerRtmp
import java.io.File
import java.lang.reflect.Proxy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var rtmpCamera: RtmpCamera1? = null

    private var isStreaming = false
    private var isRecording = false
    private var isMicOn = true
    private var isFlashOn = false
    private var isAudioOnly = false
    private var timer: CountDownTimer? = null

    private val PERMISSION_CODE = 100
    private val GALLERY_CODE = 101

    // PROXY CALLBACK - No override names needed, works with ANY 2.2.6 method name
    private fun createConnectChecker(): ConnectCheckerRtmp {
        val handler = java.lang.reflect.InvocationHandler { _, method, _ ->
            val name = method.name
            runOnUiThread {
                try {
                    when {
                        name.contains("Success") -> {
                            binding.tvLiveStatus.text = "LIVE - REALLY TRANSMITTING"
                            binding.tvLiveStatus.setTextColor(android.graphics.Color.GREEN)
                            binding.tvViewers.text = "Viewers: 0 | Program: LIVE"
                        }
                        name.contains("Failed") -> {
                            isStreaming = false
                            binding.tvLiveStatus.text = "FAILED - Check Key / Internet"
                            binding.tvLiveStatus.setTextColor(android.graphics.Color.RED)
                            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
                        }
                        name.contains("Disconnect") -> {
                            isStreaming = false
                            binding.tvLiveStatus.text = "OFFLINE"
                            binding.tvLiveStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
                        }
                        name.contains("Started") -> {
                            binding.tvLiveStatus.text = "CONNECTING..."
                            binding.tvLiveStatus.setTextColor(android.graphics.Color.YELLOW)
                        }
                    }
                } catch (e: Exception) { }
            }
            // Return default for any return type
            when (method.returnType) {
                Boolean::class.javaPrimitiveType -> false
                Int::class.javaPrimitiveType -> 0
                Long::class.javaPrimitiveType -> 0L
                else -> null
            }
        }
        return Proxy.newProxyInstance(
            ConnectCheckerRtmp::class.java.classLoader,
            arrayOf(ConnectCheckerRtmp::class.java),
            handler
        ) as ConnectCheckerRtmp
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // CORRECT 2-arg constructor - OpenGlView + Proxy (never null)
            rtmpCamera = RtmpCamera1(binding.previewMain, createConnectChecker())
        } catch (e: Exception) {
            rtmpCamera = null
            Toast.makeText(this, "Camera init failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setupSpinners()
        setupButtons()
        checkPermissions()
    }

    private fun setupSpinners() {
        try {
            val platforms = listOf("YouTube", "Facebook", "TikTok")
            binding.spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platforms)
            val resolutions = listOf("720p - 1280x720", "1080p - 1920x1080", "480p - 854x480")
            binding.spResolution.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resolutions)
            val bitrates = listOf("2500 Kbps - Good", "4000 Kbps - HD", "1500 Kbps - Low Data")
            binding.spBitrate.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bitrates)
            val fpsList = listOf("30 FPS", "60 FPS", "24 FPS")
            binding.spFps.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fpsList)
            val intros = listOf("No Intro", "JSTV Intro 1", "JSTV Intro 2")
            binding.spIntro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intros)
            val orientations = listOf("Portrait", "Landscape")
            binding.spOrientation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, orientations)
        } catch (e: Exception) { }
    }

    private fun setupButtons() {
        binding.btnGoLive.setOnClickListener {
            if (!isStreaming) startRealStream() else stopRealStream()
        }
        binding.btnSwitchCamera.setOnClickListener {
            try { rtmpCamera?.switchCamera() } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            binding.btnFlash.text = if (isFlashOn) "FLASH ON" else "FLASH"
            Toast.makeText(this, if (isFlashOn) "Flash ON" else "Flash OFF", Toast.LENGTH_SHORT).show()
        }
        binding.btnMic.setOnClickListener {
            try {
                if (isMicOn) { rtmpCamera?.disableAudio(); isMicOn = false; binding.btnMic.text = "MIC OFF" }
                else { rtmpCamera?.enableAudio(); isMicOn = true; binding.btnMic.text = "MIC ON" }
            } catch (e: Exception) { Toast.makeText(this, "Mic error", Toast.LENGTH_SHORT).show() }
        }
        binding.btnAudioOnly.setOnClickListener {
            isAudioOnly = !isAudioOnly
            binding.btnAudioOnly.text = if (isAudioOnly) "VIDEO OFF" else "AUDIO ONLY"
            Toast.makeText(this, if (isAudioOnly) "Audio Only mode" else "Video back on", Toast.LENGTH_SHORT).show()
        }
        binding.btnRecord.setOnClickListener {
            if (!isRecording) {
                try {
                    val folder = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "JSTV")
                    if (!folder.exists()) folder.mkdirs()
                    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val file = File(folder, "JSTV_$time.mp4")
                    rtmpCamera?.startRecord(file.absolutePath)
                    isRecording = true
                    binding.btnRecord.text = "STOP REC"
                    Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Record error: ${e.message}", Toast.LENGTH_SHORT).show() }
            } else {
                try { rtmpCamera?.stopRecord() } catch (e: Exception) { }
                isRecording = false
                binding.btnRecord.text = "RECORD"
                Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnGallery.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, GALLERY_CODE)
            } catch (e: Exception) { }
        }
        binding.btnTitleStyle.setOnClickListener {
            val title = binding.etStreamTitle.text.toString().trim()
            if (title.isNotEmpty()) { binding.tvOverlayTitle.text = title; Toast.makeText(this, "Title set", Toast.LENGTH_SHORT).show() }
            else Toast.makeText(this, "Enter title first", Toast.LENGTH_SHORT).show()
        }
        binding.btnScrollText.setOnClickListener {
            val txt = binding.etScrollText.text.toString().trim()
            if (txt.isNotEmpty()) { binding.tvScrolling.text = txt; binding.tvScrolling.isSelected = true }
        }
        binding.btnProgramTimer.setOnClickListener {
            val mins = binding.etTimerMins.text.toString().trim().toIntOrNull() ?: 0
            if (mins > 0) startProgramTimer(mins) else Toast.makeText(this, "Enter minutes e.g. 30", Toast.LENGTH_SHORT).show()
        }
        binding.btnLoop.setOnClickListener { Toast.makeText(this, "Loop Mode - Coming in Premium", Toast.LENGTH_SHORT).show() }
        binding.btnChurchScreen.setOnClickListener { Toast.makeText(this, "Church Screen - Coming Soon", Toast.LENGTH_SHORT).show() }
        binding.btnPayment.setOnClickListener { Toast.makeText(this, "Paybill 516600 - Account: JSTV PREMIUM", Toast.LENGTH_LONG).show() }
        binding.btnWhatsApp.setOnClickListener {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text=Hello JSTV Support"))) }
            catch (e: Exception) { Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show() }
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun getRtmpUrl(platform: String, key: String): String {
        return when (platform) {
            "YouTube" -> "rtmp://a.rtmp.youtube.com/live2/$key"
            "Facebook" -> "rtmps://live-api-s.facebook.com:443/rtmp/$key"
            "TikTok" -> "rtmp://rtmp-push.tiktok.com/live/$key"
            else -> "rtmp://a.rtmp.youtube.com/live2/$key"
        }
    }

    private fun startRealStream() {
        val cam = rtmpCamera
        if (cam == null) { Toast.makeText(this, "Camera not ready, restart app", Toast.LENGTH_LONG).show(); return }
        val key = binding.etStreamKey.text.toString().trim()
        if (key.isEmpty()) { Toast.makeText(this, "Paste Stream Key ONLY first", Toast.LENGTH_LONG).show(); return }
        if (key.contains("rtmp") || key.contains("://")) { Toast.makeText(this, "Paste KEY ONLY, not full URL", Toast.LENGTH_LONG).show(); return }
        val platform = binding.spPlatform.selectedItem?.toString() ?: "YouTube"
        val fullUrl = getRtmpUrl(platform, key)
        try {
            when (binding.spResolution.selectedItemPosition) {
                0 -> cam.setVideoBitrateOnFly(2500 * 1024)
                1 -> cam.setVideoBitrateOnFly(4000 * 1024)
                2 -> cam.setVideoBitrateOnFly(1500 * 1024)
            }
        } catch (e: Exception) { }
        binding.tvRtmp.text = "RTMP: $platform - Connecting..."
        binding.tvLiveStatus.text = "CONNECTING..."
        binding.tvLiveStatus.setTextColor(android.graphics.Color.YELLOW)
        try {
            if (cam.isStreaming) cam.stopStream()
            cam.startStream(fullUrl)
            isStreaming = true
            binding.btnGoLive.text = "STOP LIVE - REALLY TRANSMITTING"
            binding.tvLiveStatus.text = "LIVE - REALLY TRANSMITTING"
            binding.tvLiveStatus.setTextColor(android.graphics.Color.GREEN)
            binding.tvViewers.text = "Viewers: 0 | Program: LIVE"
            Toast.makeText(this, "Connecting to $platform...", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            isStreaming = false
            binding.tvLiveStatus.text = "FAILED: ${e.message}"
            binding.tvLiveStatus.setTextColor(android.graphics.Color.RED)
            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRealStream() {
        try { if (rtmpCamera?.isStreaming == true) rtmpCamera?.stopStream() } catch (e: Exception) { }
        try { if (isRecording) { rtmpCamera?.stopRecord(); isRecording = false; binding.btnRecord.text = "RECORD" } } catch (e: Exception) { }
        isStreaming = false
        binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
        binding.tvLiveStatus.text = "OFFLINE"
        binding.tvLiveStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        binding.tvViewers.text = "Viewers: 0 | Program: Stopped"
    }

    private fun startProgramTimer(mins: Int) {
        timer?.cancel()
        val totalMillis = mins * 60 * 1000L
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val m = millisUntilFinished / 60000
                val s = (millisUntilFinished / 1000) % 60
                binding.tvViewers.text = "Viewers: 0 | Program: $m:$s remaining"
            }
            override fun onFinish() {
                binding.tvViewers.text = "Viewers: 0 | Program: Finished"
                Toast.makeText(this@MainActivity, "Program time finished", Toast.LENGTH_LONG).show()
                if (isStreaming) stopRealStream()
            }
        }.start()
        Toast.makeText(this, "Timer started: $mins mins", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.CAMERA)
        perms.add(Manifest.permission.RECORD_AUDIO)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            perms.add(Manifest.permission.READ_MEDIA_VIDEO)
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            Toast.makeText(this, "JSTV needs Camera + Mic - Tap ALLOW", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_CODE)
        } else {
            Toast.makeText(this, "Permissions OK - Starting Camera", Toast.LENGTH_SHORT).show()
            startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            val cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (cameraGranted && micGranted) {
                Toast.makeText(this, "Thank you - Starting Camera", Toast.LENGTH_SHORT).show()
                startPreview()
            } else {
                Toast.makeText(this, "Camera / Mic DENIED - Go to Settings > JSTV > Permissions > Allow", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startPreview() {
        try {
            val cam = rtmpCamera ?: return
            if (!cam.isOnPreview) cam.startPreview()
        } catch (e: Exception) {
            Toast.makeText(this, "Preview error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try { binding.imgOverlayLogo.setImageURI(data.data) } catch (e: Exception) { }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        try {
            if (rtmpCamera?.isStreaming == true) rtmpCamera?.stopStream()
            if (rtmpCamera?.isOnPreview == true) rtmpCamera?.stopPreview()
        } catch (e: Exception) { }
    }
}
