package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Camera source: OWN -> STUDIO1 -> STUDIO2 -> OWN
    private var cameraSource = "OWN"
    private var isPreviewOn = false
    private var isStreaming = false
    private var isPremium = false
    private val PERM_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isPremium = getSharedPreferences("jstv", MODE_PRIVATE).getBoolean("is_premium", false)
        applyFreePremium()

        // Fill the 2 Spinners in new layout - otherwise Save crashes
        binding.spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("YouTube", "Facebook", "TikTok"))
        binding.spResolution.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("720p - 1280x720", "1080p - 1920x1080", "480p - 854x480"))

        // Start with OWN camera
        checkPermissionsAndStartOwnCamera()

        // SWITCH BUTTON - OWN / STUDIO1 / STUDIO2
        binding.btnSwitchSource.setOnClickListener { switchSource() }

        // GO LIVE - MAIN SCREEN is where livestreaming happens
        binding.btnGoLive.setOnClickListener {
            if (!isStreaming) startStream() else stopStream()
        }

        // SETTINGS - Panel inside MAIN SCREEN
        binding.btnSettings.setOnClickListener {
            binding.settingsPanel.visibility = if (binding.settingsPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // PREMIUM - Inside MAIN SCREEN
        binding.btnPremium.setOnClickListener {
            if (isPremium) {
                Toast.makeText(this, "Already PREMIUM - Multistream Unlocked", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, PaymentActivity::class.java))
            }
        }

        binding.btnSwitchCamera.setOnClickListener { RtmpHelper.call("switchCamera") }
        binding.btnFlash.setOnClickListener { RtmpHelper.call("toggleFlash") }
        binding.btnMic.setOnClickListener { RtmpHelper.call("toggleAudio") }
        binding.btnRecord.setOnClickListener {
            if (!isPremium) {
                Toast.makeText(this, "Recording full - Premium Only. Tap PREMIUM", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Recording - Premium", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnRemote.setOnClickListener { startActivity(Intent(this, RemoteControlActivity::class.java)) }
        binding.btnChurchScreen.setOnClickListener { startActivity(Intent(this, ChurchScreenActivity::class.java)) }
        binding.btnAdmin.setOnClickListener { startActivity(Intent(this, DeveloperActivity::class.java)) }
        binding.btnBack.setOnClickListener { finish() }

        // Save Settings button inside panel
        binding.btnSaveSettings.setOnClickListener { saveSettings() }
    }

    private fun applyFreePremium() {
        if (isPremium) {
            binding.tvWatermark1.visibility = View.GONE
            binding.tvWatermark2.visibility = View.GONE
            binding.tvWatermark3.visibility = View.GONE
            binding.tvPlan.text = "⭐ PREMIUM - MULTISTREAM (YouTube + Facebook + TikTok) - ALL FEATURES"
            binding.tvMultistream.visibility = View.VISIBLE
        } else {
            binding.tvWatermark1.visibility = View.VISIBLE
            binding.tvWatermark2.visibility = View.VISIBLE
            binding.tvWatermark3.visibility = View.VISIBLE
            binding.tvPlan.text = "FREE - 1 STREAM - WATERMARKS - LIMITED FEATURES"
            binding.tvMultistream.visibility = View.GONE
        }
    }

    // --- SWITCH LOGIC - OWN / STUDIO1 / STUDIO2 ---
    private fun switchSource() {
        when (cameraSource) {
            "OWN" -> {
                stopOwnCamera()
                cameraSource = "STUDIO1"
                binding.tvSource.text = "SOURCE: STUDIO 1 - Main Events"
                binding.tvSource.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                binding.tvStudioNotice.visibility = View.VISIBLE
                binding.tvStudioNotice.text = "Receiving from STUDIO 1 - Waiting for STUDIO 1 to send..."
                Toast.makeText(this, "Switched to STUDIO 1 - Own camera STOPPED", Toast.LENGTH_SHORT).show()
            }
            "STUDIO1" -> {
                cameraSource = "STUDIO2"
                binding.tvSource.text = "SOURCE: STUDIO 2 - Other Events"
                binding.tvSource.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                binding.tvStudioNotice.text = "Receiving from STUDIO 2 - Waiting for STUDIO 2 to send..."
                Toast.makeText(this, "Switched to STUDIO 2", Toast.LENGTH_SHORT).show()
            }
            "STUDIO2" -> {
                cameraSource = "OWN"
                binding.tvSource.text = "SOURCE: OWN CAMERA - MAIN SCREEN Direct"
                binding.tvSource.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.tvStudioNotice.visibility = View.GONE
                startOwnCamera()
                Toast.makeText(this, "Switched back to OWN Camera", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSwitchSource.text = "SWITCH: $cameraSource -> NEXT"
    }

        private fun startOwnCamera() {
        if (isPreviewOn) return
        if (RtmpHelper.camera == null) RtmpHelper.init(binding.previewMain)
        val started: Boolean = RtmpHelper.prepareAndPreview(1280, 720, 30, 2500 * 1024, 90)
        binding.tvLiveStatus.text = if (started) "STARTED - OWN CAMERA" else "PREVIEW FAILED"
        isPreviewOn = started
    }

    private fun stopOwnCamera() {
        if (isPreviewOn) {
            try { RtmpHelper.call("stopPreview") } catch (e: Exception) {}
            isPreviewOn = false
        }
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        prefs.edit()
            .putString("stream_key", binding.etStreamKey.text.toString())
            .putString("stream_key2", binding.etStreamKey2.text.toString())
            .putString("stream_key3", binding.etStreamKey3.text.toString())
            .putString("platform", binding.spPlatform.selectedItem.toString())
            .putString("resolution", binding.spResolution.selectedItem.toString())
            .putString("stream_title", binding.etStreamTitle.text.toString())
            .apply()
        Toast.makeText(this, "Settings Saved on MAIN SCREEN", Toast.LENGTH_SHORT).show()
    }

    private fun startStream() {
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val key = prefs.getString("stream_key", "") ?: ""
        if (key.isEmpty()) {
            Toast.makeText(this, "Tap SETTINGS - Paste Stream Key first", Toast.LENGTH_LONG).show()
            binding.settingsPanel.visibility = View.VISIBLE
            return
        }
        if (key.contains("rtmp")) {
            Toast.makeText(this, "Paste KEY ONLY, not URL", Toast.LENGTH_LONG).show()
            return
        }
        if (isPremium) {
            binding.tvLiveStatus.text = "LIVE - MULTISTREAM PREMIUM"
            Toast.makeText(this, "Premium Multistream: YouTube + Facebook + TikTok", Toast.LENGTH_LONG).show()
        } else {
            binding.tvLiveStatus.text = "LIVE - FREE 1 STREAM"
            Toast.makeText(this, "Free: 1 Stream Only", Toast.LENGTH_SHORT).show()
        }
        RtmpHelper.callStr("startStream", "rtmp://a.rtmp.youtube.com/live2/" + key)
        isStreaming = true
        binding.btnGoLive.text = "STOP LIVE"
    }

    private fun stopStream() {
        try { RtmpHelper.call("stopStream") } catch (e: Exception) {}
        isStreaming = false
        binding.btnGoLive.text = "GO LIVE"
        binding.tvLiveStatus.text = "OFFLINE"
    }

    private fun checkPermissionsAndStartOwnCamera() {
        val perms = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        val need = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) ActivityCompat.requestPermissions(this, need.toTypedArray(), PERM_CODE)
        else startOwnCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_CODE) {
            val camOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val micOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (camOk && micOk) startOwnCamera()
            else Toast.makeText(this, "Allow Camera + Mic + Files + Contacts + Notification in Settings > Apps > JSTV", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isStreaming) try { RtmpHelper.call("stopStream") } catch (e: Exception) {}
        if (isPreviewOn) try { RtmpHelper.call("stopPreview") } catch (e: Exception) {}
    }
}
