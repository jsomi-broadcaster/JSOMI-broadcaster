package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var studioNumber = 1
    private var studioName = "STUDIO1 - Main Events"
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequest: CaptureRequest.Builder? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var cameraId = "0"
    private var isFlashOn = false
    private var isMicOn = true

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            try {
                cameraDevice = camera
                startPreview()
            } catch (e: Exception) {
                binding.tvStatus.text = "OPENED BUT PREVIEW FAILED: ${e.message}"
            }
        }
        override fun onDisconnected(camera: CameraDevice) { try { camera.close() } catch (e: Exception) {} ; cameraDevice = null }
        override fun onError(camera: CameraDevice, error: Int) {
            binding.tvStatus.text = "CAMERA ERROR $error - Tap OPEN CAMERA again"
            try { camera.close() } catch (e: Exception) {}
            cameraDevice = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            Toast.makeText(this, "LAYOUT ERROR: ${e.message} - You did NOT replace activity_camera.xml", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        studioNumber = intent.getIntExtra("STUDIO", 1)
        studioName = intent.getStringExtra("STUDIO_NAME") ?: if (studioNumber == 1) "STUDIO1 - Main Events" else "STUDIO2 - Other Events"
        binding.tvStudioName.text = studioName
        if (studioNumber == 2) binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        else binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))

        binding.tvStatus.text = "Ready - Tap OPEN CAMERA"
        binding.previewStudio.surfaceTextureListener = textureListener

        binding.btnOpenCamera.setOnClickListener {
            try {
                startBackground()
                openCamera()
            } catch (e: Exception) {
                binding.tvStatus.text = "OPEN FAILED: ${e.message}"
            }
        }

        binding.btnSwitchCamera.setOnClickListener {
            try {
                closeCamera()
                val manager = getSystemService(CAMERA_SERVICE) as CameraManager
                val ids = manager.cameraIdList
                val frontId = ids.firstOrNull { manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT }
                val backId = ids.firstOrNull { manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK }
                cameraId = if (cameraId == backId) (frontId ?: backId ?: "0") else (backId ?: "0")
                openCamera()
            } catch (e: Exception) { binding.tvStatus.text = "SWITCH FAILED: ${e.message}" }
        }

        binding.btnFlash.setOnClickListener {
            try {
                isFlashOn = !isFlashOn
                previewRequest?.set(CaptureRequest.FLASH_MODE, if (isFlashOn) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
                previewRequest?.build()?.let { captureSession?.setRepeatingRequest(it, null, backgroundHandler) }
                binding.tvStatus.text = if (isFlashOn) "FLASH ON" else "FLASH OFF"
            } catch (e: Exception) { Toast.makeText(this, "Flash not supported", Toast.LENGTH_SHORT).show() }
        }

        binding.btnMic.setOnClickListener {
            isMicOn = !isMicOn
            binding.tvStatus.text = if (isMicOn) "MIC ON" else "MIC OFF"
        }

        binding.btnSendToMain.setOnClickListener {
            getSharedPreferences("jstv", MODE_PRIVATE).edit().putString("active_studio", "STUDIO$studioNumber").putString("active_studio_name", studioName).apply()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("FROM_STUDIO", studioNumber); i.putExtra("FROM_STUDIO_NAME", studioName)
            startActivity(i)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun startBackground() {
        if (backgroundThread == null) {
            backgroundThread = HandlerThread("CameraBg").also { it.start() }
            backgroundHandler = Handler(backgroundThread!!.looper)
        }
    }
    private fun stopBackground() {
        try { backgroundThread?.quitSafely() } catch (e: Exception) {}
        backgroundThread = null; backgroundHandler = null
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            binding.tvStatus.text = "CAMERA PERMISSION DENIED - Allow in Settings > Apps > JSTV > Permissions"
            return
        }
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            if (!manager.cameraIdList.contains(cameraId)) cameraId = manager.cameraIdList[0]
            binding.tvStatus.text = "Opening camera $cameraId..."
            manager.openCamera(cameraId, cameraStateCallback, backgroundHandler)
        } catch (e: Exception) {
            binding.tvStatus.text = "CANNOT OPEN: ${e.message}"
        }
    }

    private fun startPreview() {
        try {
            val texture = binding.previewStudio.surfaceTexture
            if (texture == null) { binding.tvStatus.text = "Texture not ready - tap OPEN CAMERA again"; return }
            texture.setDefaultBufferSize(1280, 720)
            val surface = Surface(texture)
            previewRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequest?.addTarget(surface)
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        previewRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                        session.setRepeatingRequest(previewRequest!!.build(), null, backgroundHandler)
                        binding.tvStatus.text = "LIVE - $studioName"
                    } catch (e: Exception) { binding.tvStatus.text = "PREVIEW ERROR: ${e.message}" }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) { binding.tvStatus.text = "FAILED TO CONFIGURE - Tap OPEN CAMERA again" }
            }, backgroundHandler)
        } catch (e: Exception) { binding.tvStatus.text = "PREVIEW ERROR: ${e.message}" }
    }

    private fun closeCamera() {
        try { captureSession?.close() } catch (e: Exception) {}
        captureSession = null
        try { cameraDevice?.close() } catch (e: Exception) {}
        cameraDevice = null
    }

    override fun onPause() {
        closeCamera(); stopBackground(); super.onPause()
    }
}
