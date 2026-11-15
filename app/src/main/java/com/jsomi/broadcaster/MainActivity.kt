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
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.rtmp.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.OpenGlView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rtmpCamera: RtmpCamera1
    private lateinit var openGlView: OpenGlView

    private var isStreaming = false
    private var isRecording = false
    private var isMicOn = true
    private var isFlashOn = false
    private var isAudioOnly = false
    private var timer: CountDownTimer? = null

    private val PERMISSION_CODE = 100
    private val GALLERY_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openGlView = binding.previewMain
        rtmpCamera = RtmpCamera1(openGlView, this)

        setupSpinners()
        setupButtons()
        checkPermissions()
    }

    private fun setupSpinners() {
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
    }

    private fun setupButtons() {
        binding.btnGoLive.setOnClickListener {
            if (!isStreaming) {
                startRealStream()
            } else {
                stopRealStream()
            }
        }

        binding.btnSwitchCamera.setOnClickListener {
            try {
                rtmpCamera.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFlash.setOnClickListener {
            try {
                if (isFlashOn) {
                    rtmpCamera.turnOffLantern()
                    isFlashOn = false
                    binding.btnFlash.text = "FLASH"
                } else {
                    rtmpCamera.turnOnLantern()
                    isFlashOn = true
                    binding.btnFlash.text = "FLASH ON"
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMic.setOnClickListener {
            if (isMicOn) {
                rtmpCamera.disableAudio()
                isMicOn = false
                binding.btnMic.text = "MIC OFF"
            } else {
                rtmpCamera.enableAudio()
                isMicOn = true
                binding.btnMic.text = "MIC ON"
            }
        }

        binding.btnAudioOnly.setOnClickListener {
            if (!isAudioOnly) {
                rtmpCamera.disableVideo()
                isAudioOnly = true
                binding.btnAudioOnly.text = "VIDEO OFF"
                Toast.makeText(this, "Audio Only - Video paused", Toast.LENGTH_SHORT).show()
            } else {
                rtmpCamera.enableVideo()
                isAudioOnly = false
                binding.btnAudioOnly.text = "AUDIO ONLY"
            }
        }

        binding.btnRecord.setOnClickListener {
            if (!isRecording) {
                try {
                    val folder = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "JSTV")
                    if (!folder.exists()) folder.mkdirs()
                    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val file = File(folder, "JSTV_$time.mp4")
                    rtmpCamera.startRecord(file.absolutePath)
                    isRecording = true
                    binding.btnRecord.text = "STOP REC"
                    Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Record error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                try {
                    rtmpCamera.stopRecord()
                } catch (e: Exception) { }
                isRecording = false
                binding.btnRecord.text = "RECORD"
                Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_CODE)
        }

        binding.btnTitleStyle.setOnClickListener {
            val title = binding.etStreamTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                binding.tvOverlayTitle.text = title
                binding.tvOverlayTitle.isSelected = true
                Toast.makeText(this, "Title set on screen", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter title first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnScrollText.setOnClickListener {
            val txt = binding.etScrollText.text.toString().trim()
            if (txt.isNotEmpty()) {
                binding.tvScrolling.text = txt
                binding.tvScrolling.isSelected = true
                Toast.makeText(this, "Scroll text set", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnProgramTimer.setOnClickListener {
            val minsStr = binding.etTimerMins.text.toString().trim()
            val mins = minsStr.toIntOrNull() ?: 0
            if (mins > 0) {
                startProgramTimer(mins)
            } else {
                Toast.makeText(this, "Enter minutes e.g. 30", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoop.setOnClickListener {
            Toast.makeText(this, "Loop Mode - Coming in Premium", Toast.LENGTH_SHORT).show()
        }

        binding.btnChurchScreen.setOnClickListener {
            Toast.makeText(this, "Church Screen - Opening...", Toast.LENGTH_SHORT).show()
        }

        binding.btnPayment.setOnClickListener {
            Toast.makeText(this, "Paybill 516600 - Account: JSTV PREMIUM", Toast.LENGTH_LONG).show()
        }

        binding.btnWhatsApp.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text=Hello JSTV Support"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
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
        val key = binding.etStreamKey.text.toString().trim()
        if (key.isEmpty()) {
            Toast.makeText(this, "Paste Stream Key ONLY first", Toast.LENGTH_LONG).show()
            return
        }
        if (key.contains("rtmp") || key.contains("://")) {
            Toast.makeText(this, "Paste KEY ONLY, not full URL", Toast.LENGTH_LONG).show()
            return
        }
        val platform = binding.spPlatform.selectedItem?.toString() ?: "YouTube"
        val fullUrl = getRtmpUrl(platform, key)

        // Apply quality
        try {
            when (binding.spResolution.selectedItemPosition) {
                0 -> rtmpCamera.setVideoBitrateOnFly(2500 * 1024)
                1 -> rtmpCamera.setVideoBitrateOnFly(4000 * 1024)
                2 -> rtmpCamera.setVideoBitrateOnFly(1500 * 1024)
            }
        } catch (e: Exception) { }

        binding.tvRtmp.text = "RTMP: $platform - Connecting..."
        binding.tvLiveStatus.text = "CONNECTING..."
        binding.tvLiveStatus.setTextColor(android.graphics.Color.YELLOW)

        // REAL RTMP TRANSMISSION - This is what sends video to YouTube / Facebook / TikTok
        if (rtmpCamera.isStreaming) {
            rtmpCamera.stopStream()
        }
        rtmpCamera.startStream(fullUrl)
    }

    private fun stopRealStream() {
        try {
            if (rtmpCamera.isStreaming) rtmpCamera.stopStream()
        } catch (e: Exception) { }
        try {
            if (isRecording) {
                rtmpCamera.stopRecord()
                isRecording = false
                binding.btnRecord.text = "RECORD"
            }
        } catch (e: Exception) { }
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
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_CODE)
        } else {
            startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            startPreview()
        }
    }

    private fun startPreview() {
        try {
            if (!rtmpCamera.isOnPreview) {
                rtmpCamera.startPreview()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Preview error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                binding.imgOverlayLogo.setImageURI(data.data)
            } catch (e: Exception) { }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        try {
            if (rtmpCamera.isStreaming) rtmpCamera.stopStream()
            if (rtmpCamera.isOnPreview) rtmpCamera.stopPreview()
        } catch (e: Exception) { }
    }

    // --- ConnectCheckerRtmp - REAL RTMP Callbacks ---
    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        runOnUiThread {
            binding.tvLiveStatus.text = "CONNECTING..."
        }
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            isStreaming = true
            binding.btnGoLive.text = "STOP LIVE - REALLY TRANSMITTING"
            binding.tvLiveStatus.text = "LIVE - REALLY TRANSMITTING"
            binding.tvLiveStatus.setTextColor(android.graphics.Color.GREEN)
            binding.tvViewers.text = "Viewers: 0 | Program: LIVE"
            Toast.makeText(this, "LIVE - Connected to platform", Toast.LENGTH_LONG).show()
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            isStreaming = false
            binding.tvLiveStatus.text = "FAILED: $reason"
            binding.tvLiveStatus.setTextColor(android.graphics.Color.RED)
            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
            Toast.makeText(this, "Connection failed: $reason - Check key", Toast.LENGTH_LONG).show()
            try {
                if (rtmpCamera.isStreaming) rtmpCamera.stopStream()
            } catch (e: Exception) { }
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        // Real bitrate updates from encoder - no fake viewers
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            isStreaming = false
            binding.tvLiveStatus.text = "DISCONNECTED"
            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            isStreaming = false
            binding.tvLiveStatus.text = "AUTH ERROR - WRONG KEY"
            binding.btnGoLive.text = "GO LIVE TO SOCIAL MEDIA"
            Toast.makeText(this, "Auth Error - Wrong Stream Key", Toast.LENGTH_LONG).show()
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            binding.tvLiveStatus.text = "AUTH OK - LIVE"
        }
    }
}
