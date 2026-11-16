package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityMainBinding
import java.io.*
import java.net.*
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    // MODE: NONE / STUDIO1 / STUDIO2 / MAIN
    private var mode = "NONE"
    private var currentStudio = "STUDIO 1"
    private var isFlashOn = false

    // NETWORK - MAIN is server on port 5000
    private val PORT = 5000
    private var serverRunning = false
    private var sendSocket: Socket? = null
    private var sendOut: DataOutputStream? = null
    private var isSending = false
    private var selectedOnMain = 1 // 1 or 2 - which is BIG on Main

    private var lastSendTime = 0L

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else binding.tvStatus.text = "Permission DENIED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.tvStatus.text = "Pick your role first"
        binding.tvStudio.text = "JSTV - PICK ROLE"

        // --- ROLE BUTTONS ---
        binding.btnStudio1.setOnClickListener {
            mode = "STUDIO1"; currentStudio = "STUDIO 1"
            updateUIForMode()
            Toast.makeText(this, "You are STUDIO 1", Toast.LENGTH_SHORT).show()
            askCamera()
        }
        binding.btnStudio2.setOnClickListener {
            mode = "STUDIO2"; currentStudio = "STUDIO 2"
            updateUIForMode()
            Toast.makeText(this, "You are STUDIO 2", Toast.LENGTH_SHORT).show()
            askCamera()
        }
        binding.btnMain.setOnClickListener {
            mode = "MAIN"; currentStudio = "MAIN"
            updateUIForMode()
            startMainServer()
            Toast.makeText(this, "You are MAIN - waiting for studios", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenCamera.setOnClickListener { askCamera() }

        binding.btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }

        binding.btnFlash.setOnClickListener {
            try {
                if (camera?.cameraInfo?.hasFlashUnit() == true) {
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                }
            } catch (e: Exception) {}
        }

        // SEND TO MAIN - only for studios
        binding.btnSend.setOnClickListener {
            if (mode == "STUDIO1" || mode == "STUDIO2") {
                val ip = binding.etMainIp.text.toString().trim()
                if (ip.isEmpty()) { Toast.makeText(this, "Type Main IP first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                connectToMain(ip)
            } else Toast.makeText(this, "Pick STUDIO 1 or 2 first", Toast.LENGTH_SHORT).show()
        }

        binding.btnStopSend.setOnClickListener { stopSending() }

        // MAIN - tap boxes to make BIG
        binding.imgStudio1Main.setOnClickListener { selectedOnMain = 1; updateMainSelectionUI(); Toast.makeText(this, "MAIN SHOWING: STUDIO 1", Toast.LENGTH_SHORT).show() }
        binding.imgStudio2Main.setOnClickListener { selectedOnMain = 2; updateMainSelectionUI(); Toast.makeText(this, "MAIN SHOWING: STUDIO 2", Toast.LENGTH_SHORT).show() }

        // Default IP for hotspot Main
        binding.etMainIp.setText("192.168.43.1")
    }

    private fun updateUIForMode() {
        when (mode) {
            "STUDIO1" -> {
                binding.tvStudio.text = "YOU ARE: STUDIO 1"
                binding.tvStudio.setBackgroundColor(Color.parseColor("#2196F3"))
                binding.previewView.visibility = android.view.View.VISIBLE
                binding.layoutMainViews.visibility = android.view.View.GONE
                binding.layoutStudioControls.visibility = android.view.View.VISIBLE
                binding.tvStatus.text = "STUDIO 1 - Camera ready"
            }
            "STUDIO2" -> {
                binding.tvStudio.text = "YOU ARE: STUDIO 2"
                binding.tvStudio.setBackgroundColor(Color.parseColor("#FF9800"))
                binding.previewView.visibility = android.view.View.VISIBLE
                binding.layoutMainViews.visibility = android.view.View.GONE
                binding.layoutStudioControls.visibility = android.view.View.VISIBLE
                binding.tvStatus.text = "STUDIO 2 - Camera ready"
            }
            "MAIN" -> {
                binding.tvStudio.text = "YOU ARE: MAIN SCREEN"
                binding.tvStudio.setBackgroundColor(Color.parseColor("#4CAF50"))
                binding.previewView.visibility = android.view.View.GONE
                binding.layoutMainViews.visibility = android.view.View.VISIBLE
                binding.layoutStudioControls.visibility = android.view.View.GONE
                binding.tvStatus.text = "MAIN - Waiting for Studio 1 & 2..."
                binding.tvMainInfo.text = "MAIN IP is usually 192.168.43.1\nStudios: type this IP and tap SEND TO MAIN"
            }
        }
    }

    private fun updateMainSelectionUI() {
        // Highlight selected
        binding.imgStudio1Main.alpha = if (selectedOnMain == 1) 1.0f else 0.5f
        binding.imgStudio2Main.alpha = if (selectedOnMain == 2) 1.0f else 0.5f
        binding.tvMainOnAir.text = if (selectedOnMain == 1) "ON AIR: STUDIO 1" else "ON AIR: STUDIO 2"
    }

    private fun askCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        if (mode == "MAIN") return
        binding.tvStatus.text = "Opening camera..."
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
                // Analysis for sending - small size for speed over hotspot
                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(320, 240))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try { if (isSending) sendFrame(imageProxy) } catch (e: Exception) {} finally { imageProxy.close() }
                }
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, cameraSelector, preview, analysis)
                binding.tvStatus.text = "LIVE - $currentStudio - Camera Working"
            } catch (e: Exception) { binding.tvStatus.text = "CANNOT OPEN: ${e.message}" }
        }, ContextCompat.getMainExecutor(this))
    }

    // --- STUDIO SEND ---
    private fun connectToMain(ip: String) {
        Thread {
            try {
                runOnUiThread { binding.tvStatus.text = "Connecting to Main $ip..." }
                sendSocket = Socket(ip, PORT)
                sendOut = DataOutputStream(BufferedOutputStream(sendSocket!!.getOutputStream()))
                // First byte = studio number
                val studioNum = if (mode == "STUDIO1") 1 else 2
                sendOut?.writeByte(studioNum); sendOut?.flush()
                isSending = true
                runOnUiThread {
                    binding.tvStatus.text = "SENDING TO MAIN - $currentStudio - ON AIR"
                    Toast.makeText(this, "Connected to Main!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread { binding.tvStatus.text = "Cannot connect to Main: ${e.message}\nCheck Hotspot + IP"; Toast.makeText(this, "Connect failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }

    private fun sendFrame(image: ImageProxy) {
        try {
            val now = System.currentTimeMillis()
            if (now - lastSendTime < 100) return // ~10 fps - fast enough for hotspot
            lastSendTime = now
            val out = sendOut ?: return
            val jpeg = yuvToJpeg(image)
            if (jpeg == null) return
            synchronized(out) {
                out.writeInt(jpeg.size)
                out.write(jpeg)
                out.flush()
            }
        } catch (e: Exception) { /* will reconnect on next tap */ }
    }

    private fun yuvToJpeg(image: ImageProxy): ByteArray? {
        try {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 60, out)
            return out.toByteArray()
        } catch (e: Exception) { return null }
    }

    private fun stopSending() {
        isSending = false
        try { sendOut?.close(); sendSocket?.close() } catch (e: Exception) {}
        binding.tvStatus.text = "Stopped sending - $currentStudio"
    }

    // --- MAIN RECEIVE ---
    private fun startMainServer() {
        if (serverRunning) return
        serverRunning = true
        updateMainSelectionUI()
        Thread {
            try {
                val server = ServerSocket(PORT)
                while (serverRunning) {
                    val client = server.accept()
                    Thread { handleStudioClient(client) }.start()
                }
            } catch (e: Exception) { Log.e("MAIN", "Server error ${e.message}") }
        }.start()
    }

    private fun handleStudioClient(client: Socket) {
        try {
            val input = DataInputStream(BufferedInputStream(client.getInputStream()))
            val studioNum = input.readByte().toInt() // 1 or 2
            runOnUiThread { binding.tvStatus.text = "MAIN - STUDIO $studioNum connected!" }
            while (serverRunning) {
                val len = input.readInt()
                if (len <= 0 || len > 500000) break
                val bytes = ByteArray(len)
                input.readFully(bytes)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, len)
                if (bmp != null) {
                    runOnUiThread {
                        if (studioNum == 1) binding.imgStudio1Main.setImageBitmap(bmp) else binding.imgStudio2Main.setImageBitmap(bmp)
                        // Also update big view to show selected
                        if (selectedOnMain == studioNum) binding.imgMainBig.setImageBitmap(bmp)
                        binding.tvStatus.text = "MAIN - Receiving Studio 1 & 2 - ON AIR: STUDIO $selectedOnMain"
                    }
                }
            }
        } catch (e: Exception) { Log.e("MAIN", "Client disconnected ${e.message}") }
    }

    override fun onDestroy() {
        super.onDestroy()
        serverRunning = false
        try { cameraExecutor.shutdown() } catch (e: Exception) {}
    }
}
