package com.jsomi.broadcaster

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jsomi.broadcaster.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityCameraBinding
    private var studioNumber = 1
    private var studioName = "STUDIO1 - Main Events"
    private var camera: Camera? = null
    private var currentCameraId = 0 // 0 = Back, 1 = Front
    private var isFlashOn = false
    private var isMicOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studioNumber = intent.getIntExtra("STUDIO", 1)
        studioName = intent.getStringExtra("STUDIO_NAME") ?: if (studioNumber == 1) "STUDIO1 - Main Events" else "STUDIO2 - Other Events"

        binding.tvStudioName.text = studioName
        if (studioNumber == 2) {
            binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        } else {
            binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
        }

        binding.tvStatus.text = "Opening Real Camera..."

        // Surface for real camera
        binding.previewStudio.holder.addCallback(this)
        // Old SurfaceView needs this
        @Suppress("DEPRECATION")
        binding.previewStudio.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        binding.btnSwitchCamera.setOnClickListener {
            // Switch Back <-> Front
            releaseCamera()
            currentCameraId = if (currentCameraId == 0) 1 else 0
            openCamera()
        }

        binding.btnFlash.setOnClickListener {
            // Flash ON / OFF - Back camera only
            try {
                val params = camera?.parameters
                if (params != null) {
                    isFlashOn = !isFlashOn
                    params.flashMode = if (isFlashOn) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
                    camera?.parameters = params
                    binding.tvStatus.text = if (isFlashOn) "FLASH ON - $studioName" else "FLASH OFF - $studioName"
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Flash not supported on this camera", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMic.setOnClickListener {
            isMicOn = !isMicOn
            binding.tvStatus.text = if (isMicOn) "MIC ON - $studioName" else "MIC OFF - Muted"
            Toast.makeText(this, if (isMicOn) "Mic ON" else "Mic OFF", Toast.LENGTH_SHORT).show()
        }

        binding.btnSendToMain.setOnClickListener {
            getSharedPreferences("jstv", MODE_PRIVATE).edit()
                .putString("active_studio", "STUDIO$studioNumber")
                .putString("active_studio_name", studioName)
                .apply()
            Toast.makeText(this, "$studioName - Sending to MAIN SCREEN", Toast.LENGTH_SHORT).show()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("FROM_STUDIO", studioNumber)
            i.putExtra("FROM_STUDIO_NAME", studioName)
            startActivity(i)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun openCamera() {
        try {
            // Find camera - if front requested but no front, use back
            val count = Camera.getNumberOfCameras()
            var idToOpen = currentCameraId
            if (idToOpen >= count) idToOpen = 0
            if (count == 0) {
                binding.tvStatus.text = "NO CAMERA FOUND ON THIS PHONE"
                return
            }
            camera = Camera.open(idToOpen)
            currentCameraId = idToOpen
            camera?.setDisplayOrientation(90)
            // If surface already ready, start now
            if (binding.previewStudio.holder.surface.isValid) {
                camera?.setPreviewDisplay(binding.previewStudio.holder)
                camera?.startPreview()
                binding.tvStatus.text = "LIVE - $studioName - Real Camera ON - ${if (currentCameraId == 0) "BACK" else "FRONT"}"
            }
        } catch (e: Exception) {
            binding.tvStatus.text = "CAMERA BUSY / IN USE - Close other camera app and reopen: ${e.message}"
            Toast.makeText(this, "Camera busy: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun releaseCamera() {
        try {
            camera?.stopPreview()
            camera?.release()
        } catch (e: Exception) {}
        camera = null
    }

    // SurfaceHolder - Real camera needs this
    override fun surfaceCreated(holder: SurfaceHolder) {
        openCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (holder.surface == null) return
        try {
            camera?.stopPreview()
        } catch (e: Exception) {}
        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
            binding.tvStatus.text = "LIVE - $studioName - Real Camera ON - ${if (currentCameraId == 0) "BACK" else "FRONT"}"
        } catch (e: Exception) {
            binding.tvStatus.text = "CAMERA ERROR: ${e.message}"
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
        if (camera == null && binding.previewStudio.holder.surface.isValid) {
            openCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }
}
