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
    private var cameraId = "0" // 0 = Back, 1 = Front
    private var isFlashOn = false
    private var isMicOn = true

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) { openCamera() }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }
        override fun onDisconnected(camera: CameraDevice) { camera.close(); cameraDevice = null }
        override fun onError(camera: CameraDevice, error: Int) {
            binding.tvStatus.text = "CAMERA ERROR $error"
            camera.close(); cameraDevice = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studioNumber = intent.getIntExtra("STUDIO", 1)
        studioName = intent.getStringExtra("STUDIO_NAME") ?: if (studioNumber == 1) "STUDIO1 - Main Events" else "STUDIO2 - Other Events"
        binding.tvStudioName.text = studioName
        if (studioNumber == 2) binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        else binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))

        binding.tvStatus.text = "Opening Real Camera2..."

        binding.btnSwitchCamera.setOnClickListener {
            closeCamera()
            // Find other camera: Back <-> Front
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            val ids = manager.cameraIdList
            val frontId = ids.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }
            val backId = ids.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            cameraId = if (cameraId == backId) (frontId ?: backId ?: "0") else (backId ?: "0")
            openCamera()
        }

        binding.btnFlash.setOnClickListener {
            try {
                isFlashOn = !isFlashOn
                previewRequest?.set(CaptureRequest.FLASH_MODE, if (isFlashOn) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
                previewRequest?.build()?.let { captureSession?.setRepeatingRequest(it, null, backgroundHandler) }
                binding.tvStatus.text = if (isFlashOn) "FLASH ON - $studioName" else "FLASH OFF - $studioName"
            } catch (e: Exception) { Toast.makeText(this, "Flash not supported", Toast.LENGTH_SHORT).show() }
        }

        binding.btnMic.setOnClickListener {
            isMicOn = !isMicOn
            binding.tvStatus.text = if (isMicOn) "MIC ON - $studioName" else "MIC OFF - Muted"
        }

        binding.btnSendToMain.setOnClickListener {
            getSharedPreferences("jstv", MODE_PRIVATE).edit()
                .putString("active_studio", "STUDIO$studioNumber")
                .putString("active_studio_name", studioName).apply()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("FROM_STUDIO", studioNumber)
            i.putExtra("FROM_STUDIO_NAME", studioName)
            startActivity(i)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun startBackground() {
        backgroundThread = HandlerThread("CameraBg").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }
    private fun stopBackground() {
        backgroundThread?.quitSafely()
        backgroundThread = null; backgroundHandler = null
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            binding.tvStatus.text = "CAMERA PERMISSION DENIED - Allow in Settings"
            return
        }
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            // If cameraId not valid, use first back camera
            if (!manager.cameraIdList.contains(cameraId)) cameraId = manager.cameraIdList[0]
            manager.openCamera(cameraId, cameraStateCallback, backgroundHandler)
            binding.tvStatus.text = "Camera opening..."
        } catch (e: Exception) {
            binding.tvStatus.text = "CANNOT OPEN CAMERA: ${e.message}"
        }
    }

    private fun startPreview() {
        try {
            val texture = binding.previewStudio.surfaceTexture ?: return
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
                        binding.tvStatus.text = "LIVE - $studioName - Camera ON"
                    } catch (e: Exception) { binding.tvStatus.text = "PREVIEW ERROR: ${e.message}" }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    binding.tvStatus.text = "FAILED TO CONFIGURE CAMERA"
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            binding.tvStatus.text = "PREVIEW ERROR: ${e.message}"
        }
    }

    private fun closeCamera() {
        try { captureSession?.close() } catch (e: Exception) {}
        captureSession = null
        try { cameraDevice?.close() } catch (e: Exception) {}
        cameraDevice = null
    }

    override fun onResume() {
        super.onResume()
        startBackground()
        if (binding.previewStudio.isAvailable) openCamera()
        else binding.previewStudio.surfaceTextureListener = textureListener
    }

    override fun onPause() {
        closeCamera()
        stopBackground()
        super.onPause()
    }
}
