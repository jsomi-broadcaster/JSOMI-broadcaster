package com.jsomi.broadcaster

import android.Manifest
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
import com.jsomi.broadcaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityMainBinding
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequest: CaptureRequest.Builder? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var cameraId = "0"
    private var isFlashOn = false
    private var surfaceReady = false

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            binding.tvStatus.text = "Permission Granted - Tap OPEN CAMERA"
        } else {
            binding.tvStatus.text = "Permission DENIED - Tap OPEN CAMERA to ask again"
        }
    }

    private val cameraCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }
        override fun onDisconnected(camera: CameraDevice) {
            try { camera.close() } catch (e: Exception) {}
            cameraDevice = null
        }
        override fun onError(camera: CameraDevice, error: Int) {
            binding.tvStatus.text = "CAMERA ERROR $error"
            try { camera.close() } catch (e: Exception) {}
            cameraDevice = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvStatus.text = "Tap OPEN CAMERA"
        binding.previewView.holder.addCallback(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        binding.btnOpenCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.CAMERA)
            } else {
                startBackground()
                openCamera()
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
            } catch (e: Exception) {
                binding.tvStatus.text = "SWITCH FAILED: ${e.message}"
            }
        }

        binding.btnFlash.setOnClickListener {
            try {
                isFlashOn = !isFlashOn
                previewRequest?.set(CaptureRequest.FLASH_MODE, if (isFlashOn) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
                previewRequest?.build()?.let { captureSession?.setRepeatingRequest(it, null, backgroundHandler) }
                binding.tvStatus.text = if (isFlashOn) "FLASH ON" else "FLASH OFF"
            } catch (e: Exception) {
                Toast.makeText(this, "Flash not supported", Toast.LENGTH_SHORT).show()
            }
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
        backgroundThread = null
        backgroundHandler = null
    }

    private fun openCamera() {
        if (!surfaceReady) {
            binding.tvStatus.text = "Wait 1 sec - Tap OPEN CAMERA again"
            return
        }
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            if (!manager.cameraIdList.contains(cameraId)) cameraId = manager.cameraIdList[0]
            binding.tvStatus.text = "Opening camera..."
            manager.openCamera(cameraId, cameraCallback, backgroundHandler)
        } catch (e: Exception) {
            binding.tvStatus.text = "CANNOT OPEN: ${e.message}"
        }
    }

    private fun startPreview() {
        try {
            val surface = binding.previewView.holder.surface
            previewRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequest?.addTarget(surface)
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        previewRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                        session.setRepeatingRequest(previewRequest!!.build(), null, backgroundHandler)
                        binding.tvStatus.text = "LIVE - CAMERA WORKING"
                    } catch (e: Exception) {
                        binding.tvStatus.text = "PREVIEW ERROR: ${e.message}"
                    }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    binding.tvStatus.text = "FAILED TO CONFIGURE"
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

    override fun surfaceCreated(holder: SurfaceHolder) { surfaceReady = true }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { surfaceReady = true }
    override fun surfaceDestroyed(holder: SurfaceHolder) { surfaceReady = false }
    override fun onPause() { closeCamera(); stopBackground(); super.onPause() }
}
