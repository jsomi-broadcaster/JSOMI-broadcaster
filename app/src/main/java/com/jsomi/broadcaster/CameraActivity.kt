package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
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
    private var surfaceReady = false

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            binding.tvStatus.text = "Permission Granted - Tap OPEN CAMERA"
            Toast.makeText(this, "Camera Allowed - Tap OPEN CAMERA", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvStatus.text = "Permission DENIED - Tap OPEN CAMERA to ask again"
        }
    }
    private val requestMicPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Toast.makeText(this, if (granted) "Mic Allowed" else "Mic Denied", Toast.LENGTH_SHORT).show()
    }
    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            try { cameraDevice = camera; startPreview() }
            catch (e: Exception) { binding.tvStatus.text = "OPENED BUT PREVIEW FAILED: ${e.message}" }
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
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        studioNumber = intent.getIntExtra("STUDIO", 1)
        studioName = intent.getStringExtra("STUDIO_NAME") ?: if (studioNumber == 1) "STUDIO1 - Main Events" else "STUDIO2 - Other Events"
        binding.tvStudioName.text = studioName
        if (studioNumber == 2) binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        else binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
        binding.tvStatus.text = "Checking Permission..."
        binding.previewStudio.holder.addCallback(this)
        askPermissions()
        binding.btnOpenCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            } else {
                try { startBackground(); openCamera() }
                catch (e: Exception) { binding.tvStatus.text = "OPEN FAILED: ${e.message}" }
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                isMicOn = !isMicOn
                binding.tvStatus.text = if (isMicOn) "MIC ON" else "MIC OFF"
            }
        }
        binding.btnSendToMain.setOnClickListener {
            getSharedPreferences("jstv", MODE_PRIVATE).edit().putString("active_studio", "STUDIO$studioNumber").putString("active_studio_name", studioName).apply()
            val i = Intent(this, LiveActivity::class.java)
            i.putExtra("FROM_STUDIO", studioNumber); i.putExtra("FROM_STUDIO_NAME", studioName)
            startActivity(i)
        }
        binding.btnBack.setOnClickListener { finish() }
    }
    private fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            binding.tvStatus.text = "Ready - Tap OPEN CAMERA"
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
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
            requestCameraPermission.launch(Manifest.permission.CAMERA)
            return
        }
        if (!surfaceReady) { binding.tvStatus.text = "Surface not ready - wait 1 sec and tap OPEN CAMERA again"; return }
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            if (!manager.cameraIdList.contains(cameraId)) cameraId = manager.cameraIdList[0]
            binding.tvStatus.text = "Opening camera $cameraId..."
            manager.openCamera(cameraId, cameraStateCallback, backgroundHandler)
        } catch (e: Exception) { binding.tvStatus.text = "CANNOT OPEN: ${e.message}" }
    }
    private fun startPreview() {
        try {
            val surface = binding.previewStudio.holder.surface
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
    override fun surfaceCreated(holder: SurfaceHolder) { surfaceReady = true }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { surfaceReady = true }
    override fun surfaceDestroyed(holder: SurfaceHolder) { surfaceReady = false }
    override fun onPause() { closeCamera(); stopBackground(); super.onPause() }
}
