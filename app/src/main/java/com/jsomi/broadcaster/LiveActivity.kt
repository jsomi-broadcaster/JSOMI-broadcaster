package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityLiveBinding
import java.util.concurrent.Executors

class LiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: androidx.camera.core.Camera? = null
    private var isLive = false
    private var isFlashOn = false
    private var rtmpUrl = "rtmp://a.rtmp.youtube.com/live2/xxxx-xxxx"

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else binding.tvStatus.text = "Permission DENIED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val name = prefs.getString("name", "JSTV")
        binding.tvWelcome.text = "Hello, $name - JSTV LIVE"
        rtmpUrl = prefs.getString("rtmp", rtmpUrl) ?: rtmpUrl

        askCamera()

        // GO LIVE
        binding.btnGoLive.setOnClickListener {
            if (!isLive) {
                isLive = true
                binding.btnGoLive.text = "STOP LIVE"
                binding.btnGoLive.setBackgroundColor(android.graphics.Color.parseColor("#FF0000"))
                binding.tvStatus.text = "● LIVE - Broadcasting"
                binding.tvLiveBadge.text = "● LIVE"
                Toast.makeText(this, "You are LIVE!", Toast.LENGTH_SHORT).show()
                // Later we connect this to RTMP - for now camera is LIVE preview
            } else {
                isLive = false
                binding.btnGoLive.text = "GO LIVE"
                binding.btnGoLive.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.tvStatus.text = "Ready - Not Live"
                binding.tvLiveBadge.text = ""
            }
        }

        // FEATURES BUTTON
        binding.btnFeatures.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("JSTV Features")
                .setItems(arrayOf("Switch Camera", "Flash On/Off", "Stop Live", "Logout")) { _, which ->
                    when (which) {
                        0 -> { cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA; startCamera() }
                        1 -> { try { if (camera?.cameraInfo?.hasFlashUnit() == true) { isFlashOn = !isFlashOn; camera?.cameraControl?.enableTorch(isFlashOn) } } catch (e: Exception) {} }
                        2 -> { if (isLive) binding.btnGoLive.performClick() }
                        3 -> { prefs.edit().clear().apply(); finish() }
                    }
                }.show()
        }

        // SETTINGS BUTTON
        binding.btnSettings.setOnClickListener {
            val input = EditText(this)
            input.setText(rtmpUrl)
            input.hint = "RTMP URL"
            AlertDialog.Builder(this)
                .setTitle("Settings - Stream Key")
                .setMessage("Paste your YouTube / Facebook RTMP URL here")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    rtmpUrl = input.text.toString()
                    prefs.edit().putString("rtmp", rtmpUrl).apply()
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnSwitch.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
    }

    private fun askCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, cameraSelector, preview)
                binding.tvStatus.text = "Camera Ready - Tap GO LIVE"
            } catch (e: Exception) { binding.tvStatus.text = "Camera error: ${e.message}" }
        }, ContextCompat.getMainExecutor(this))
    }
}
