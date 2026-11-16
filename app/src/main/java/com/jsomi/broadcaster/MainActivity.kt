package com.jsomi.broadcaster

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isStreaming = false
    private var isRecording = false
    private var isMicOn = true
    private var isFlashOn = false
    private var timer: CountDownTimer? = null
    private var previewStarted = false
    private var curW = 1280
    private var curH = 720
    private var curFps = 30
    private var curBitrate = 3500 * 1024
    private var curRotation = 90
    private val PERMISSION_CODE = 100
    private val GALLERY_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        RtmpHelper.onStatus = { status -> updateStatus(status) }
        setupSpinners()
        setupButtons()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (!previewStarted && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.previewMain.post { tryStartPreview() }
        }
    }

    private fun readSettings() {
        val res = binding.spResolution.selectedItem?.toString() ?: "720p"
        if (res.contains("1080")) { curW = 1920; curH = 1080 }
        else if (res.contains("480")) { curW = 854; curH = 480 }
        else { curW = 1280; curH = 720 }
        val bitTxt = binding.spBitrate.selectedItem?.toString() ?: "3500"
        curBitrate = when {
            bitTxt.contains("4000") -> 4000 * 1024
            bitTxt.contains("1500") -> 1500 * 1024
            bitTxt.contains("2500") -> 2500 * 1024
            else -> 3500 * 1024
        }
        val fpsTxt = binding.spFps.selectedItem?.toString() ?: "30"
        curFps = when {
            fpsTxt.contains("60") -> 60
            fpsTxt.contains("24") -> 24
            else -> 30
        }
        val ori = binding.spOrientation.selectedItem?.toString() ?: "Portrait"
        curRotation = if (ori.contains("Landscape")) 0 else 90
    }

    private fun tryStartPreview() {
        if (previewStarted) return
        RtmpHelper.init(binding.previewMain)
        readSettings()
        val ok = RtmpHelper.prepareAndPreview(curW, curH, curFps, curBitrate, curRotation)
        if (ok) {
            previewStarted = true
            binding.tvLiveStatus.visibility = View.GONE
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!previewStarted) {
                    val ok2 = RtmpHelper.prepareAndPreview(curW, curH, curFps, curBitrate, curRotation)
                    if (ok2) {
                        previewStarted = true
                        binding.tvLiveStatus.visibility = View.GONE
                    }
                }
            }, 1000)
        }
    }

    private fun updateStatus(status: String) {
        runOnUiThread {
            if (status == "LIVE") {
                binding.tvLiveStatus.visibility = View.VISIBLE
                binding.tvLiveStatus.text = "● LIVE"
                binding.tvLiveStatus.setTextColor(android.graphics.Color.RED)
            }
            if (status == "FAILED") {
                isStreaming = false
                binding.btnGoLive.text = "GO LIVE"
                Toast.makeText(this, "Live failed - check Stream Key", Toast.LENGTH_SHORT).show()
            }
            if (status == "OFFLINE") {
                isStreaming = false
                binding.tvLiveStatus.visibility = View.GONE
                binding.btnGoLive.text = "GO LIVE"
            }
            if (status == "CONNECTING") {
                binding.tvLiveStatus.visibility = View.VISIBLE
                binding.tvLiveStatus.text = "● CONNECTING..."
                binding.tvLiveStatus.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    private fun setupSpinners() {
        binding.spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("YouTube", "Facebook", "TikTok"))
        binding.spResolution.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("720p - 1280x720", "1080p - 1920x1080", "480p - 854x480"))
        binding.spBitrate.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("3500 Kbps - Bright", "4000 Kbps - HD", "2500 Kbps - Good", "1500 Kbps - Low Data"))
        binding.spFps.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("30 FPS", "60 FPS", "24 FPS"))
        binding.spIntro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("No Intro", "JSTV Intro 1", "JSTV Intro 2"))
        binding.spOrientation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Portrait", "Landscape"))
    }

    private fun setupButtons() {
        binding.btnGoLive.setOnClickListener {
            if (!isStreaming) startRealStream() else stopRealStream()
        }
        binding.btnSwitchCamera.setOnClickListener { RtmpHelper.call("switchCamera") }
        binding.btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            if (isFlashOn) {
                RtmpHelper.call("enableFlash")
                binding.btnFlash.text = "FLASH ON"
            } else {
                RtmpHelper.call("disableFlash")
                binding.btnFlash.text = "FLASH"
            }
        }
        binding.btnMic.setOnClickListener {
            if (isMicOn) {
                RtmpHelper.call("disableAudio")
                isMicOn = false
                binding.btnMic.text = "MIC OFF"
            } else {
                RtmpHelper.call("enableAudio")
                isMicOn = true
                binding.btnMic.text = "MIC ON"
            }
        }
        binding.btnAudioOnly.setOnClickListener { Toast.makeText(this, "Audio Only mode", Toast.LENGTH_SHORT).show() }
        binding.btnRecord.setOnClickListener { toggleRecord() }
        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_CODE)
        }
        binding.btnTitleStyle.setOnClickListener {
            val title = binding.etStreamTitle.text.toString().trim()
            if (title.isNotEmpty()) binding.tvOverlayTitle.text = title
        }
        binding.btnScrollText.setOnClickListener {
            val txt = binding.etScrollText.text.toString().trim()
            if (txt.isNotEmpty()) binding.tvScrolling.text = txt
        }
        binding.btnProgramTimer.setOnClickListener {
            val mins = binding.etTimerMins.text.toString().trim().toIntOrNull() ?: 0
            if (mins > 0) startProgramTimer(mins)
        }
        binding.btnLoop.setOnClickListener { Toast.makeText(this, "Loop Mode - Coming in Premium", Toast.LENGTH_SHORT).show() }
        binding.btnChurchScreen.setOnClickListener { Toast.makeText(this, "Church Screen - Coming Soon", Toast.LENGTH_SHORT).show() }
        binding.btnPayment.setOnClickListener { Toast.makeText(this, "Paybill 516600 - Account: JSTV PREMIUM", Toast.LENGTH_LONG).show() }
        binding.btnWhatsApp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text=Hello JSTV Support")))
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.previewMain.setOnClickListener { previewStarted = false; tryStartPreview() }
    }

    private fun toggleRecord() {
        if (!isRecording) {
            val folder = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "JSTV")
            if (!folder.exists()) folder.mkdirs()
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(folder, "JSTV_" + time + ".mp4")
            RtmpHelper.callStr("startRecord", file.absolutePath)
            isRecording = true
            binding.btnRecord.text = "STOP REC"
        } else {
            RtmpHelper.call("stopRecord")
            isRecording = false
            binding.btnRecord.text = "RECORD"
        }
    }

    private fun getRtmpUrl(platform: String, key: String): String {
        if (platform == "Facebook") return "rtmps://live-api-s.facebook.com:443/rtmp/" + key
        if (platform == "TikTok") return "rtmp://rtmp-push.tiktok.com/live/" + key
        return "rtmp://a.rtmp.youtube.com/live2/" + key
    }

    private fun startRealStream() {
        if (RtmpHelper.camera == null) {
            Toast.makeText(this, "Camera not ready, restart app", Toast.LENGTH_LONG).show()
            return
        }
        val key = binding.etStreamKey.text.toString().trim()
        if (key.isEmpty()) {
            Toast.makeText(this, "Paste Stream Key ONLY first", Toast.LENGTH_LONG).show()
            return
        }
        if (key.contains("rtmp")) {
            Toast.makeText(this, "Paste KEY ONLY, not full URL", Toast.LENGTH_LONG).show()
            return
        }
        val platform = binding.spPlatform.selectedItem?.toString() ?: "YouTube"
        if (RtmpHelper.callBool("isStreaming")) RtmpHelper.call("stopStream")
        RtmpHelper.callStr("startStream", getRtmpUrl(platform, key))
        isStreaming = true
        binding.btnGoLive.text = "STOP LIVE"
    }

    private fun stopRealStream() {
        if (RtmpHelper.callBool("isStreaming")) RtmpHelper.call("stopStream")
        if (isRecording) {
            RtmpHelper.call("stopRecord")
            isRecording = false
            binding.btnRecord.text = "RECORD"
        }
        isStreaming = false
        binding.btnGoLive.text = "GO LIVE"
        binding.tvLiveStatus.visibility = View.GONE
    }

    private fun startProgramTimer(mins: Int) {
        timer?.cancel()
        val total = mins * 60 * 1000L
        timer = object : CountDownTimer(total, 1000) {
            override fun onTick(millis: Long) {
                val m = millis / 60000
                val s = (millis / 1000) % 60
                binding.tvViewers.text = "Program: " + m + ":" + s + " remaining"
            }
            override fun onFinish() {
                binding.tvViewers.text = "Program: Finished"
                if (isStreaming) stopRealStream()
            }
        }.start()
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
        val need = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_CODE)
        } else {
            binding.previewMain.post { tryStartPreview() }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            val camOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val micOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (camOk && micOk) {
                binding.previewMain.post { tryStartPreview() }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK && data != null) {
            binding.imgOverlayLogo.setImageURI(data.data)
        }
    }

    override fun onPause() {
        super.onPause()
        previewStarted = false
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        RtmpHelper.release()
    }
}
