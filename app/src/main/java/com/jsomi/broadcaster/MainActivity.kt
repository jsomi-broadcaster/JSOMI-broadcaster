package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var isFlashOn = false
    private var camera: androidx.camera.core.Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    // STUDIO - simple
    private var currentStudio = "STUDIO 1"
    private var studio1Url = ""
    private var studio2Url = ""

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else binding.tvStatus.text = "Permission DENIED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Load saved studio
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        currentStudio = prefs.getString("studio", "STUDIO 1") ?: "STUDIO 1"
        studio1Url = prefs.getString("studio1Url", "") ?: ""
        studio2Url = prefs.getString("studio2Url", "") ?: ""
        binding.etRtmpUrl.setText(if (currentStudio == "STUDIO 1") studio1Url else studio2Url)
        updateStudioUI()

        binding.tvStatus.text = "Tap OPEN CAMERA"

        binding.btnOpenCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
            else requestPermission.launch(Manifest.permission.CAMERA)
        }

        binding.btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }

        binding.btnFlash.setOnClickListener {
            try {
                if (camera?.cameraInfo?.hasFlashUnit() == true) {
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                    binding.tvStatus.text = if (isFlashOn) "FLASH ON - LIVE" else "FLASH OFF - LIVE"
                } else Toast.makeText(this, "No Flash", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(this, "Flash not supported", Toast.LENGTH_SHORT).show() }
        }

        // --- STUDIO BUTTONS ---
        binding.btnStudio1.setOnClickListener {
            saveCurrentUrl()
            currentStudio = "STUDIO 1"
            binding.etRtmpUrl.setText(studio1Url)
            saveStudio()
            updateStudioUI()
            Toast.makeText(this, "Now in STUDIO 1", Toast.LENGTH_SHORT).show()
        }

        binding.btnStudio2.setOnClickListener {
            saveCurrentUrl()
            currentStudio = "STUDIO 2"
            binding.etRtmpUrl.setText(studio2Url)
            saveStudio()
            updateStudioUI()
            Toast.makeText(this, "Now in STUDIO 2", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveUrl.setOnClickListener {
            saveCurrentUrl()
            saveStudio()
            Toast.makeText(this, "$currentStudio URL Saved", Toast.LENGTH_SHORT).show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
    }

    private fun updateStudioUI() {
        binding.tvStudio.text = "YOU ARE IN: $currentStudio"
        // Change color so you can see it
        if (currentStudio == "STUDIO 1") {
            binding.tvStudio.setBackgroundColor(android.graphics.Color.parseColor("#2196F3")) // blue
            binding.btnStudio1.alpha = 1.0f
            binding.btnStudio2.alpha = 0.5f
        } else {
            binding.tvStudio.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // orange
            binding.btnStudio1.alpha = 0.5f
            binding.btnStudio2.alpha = 1.0f
        }
    }

    private fun saveCurrentUrl() {
        val url = binding.etRtmpUrl.text.toString().trim()
        if (currentStudio == "STUDIO 1") studio1Url = url else studio2Url = url
    }

    private fun saveStudio() {
        getSharedPreferences("jstv", MODE_PRIVATE).edit()
            .putString("studio", currentStudio)
            .putString("studio1Url", studio1Url)
            .putString("studio2Url", studio2Url)
            .apply()
    }

    private fun startCamera() {
        binding.tvStatus.text = "Opening camera..."
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                binding.tvStatus.text = "LIVE - CAMERA WORKING - $currentStudio"
            } catch (e: Exception) { binding.tvStatus.text = "CANNOT OPEN: ${e.message}" }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        try { cameraExecutor.shutdown() } catch (e: Exception) {}
    }
}
