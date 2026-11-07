package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var youtubeKeyInput: EditText
    private lateinit var facebookKeyInput: EditText
    private lateinit var tiktokKeyInput: EditText
    private lateinit var jsomiTvKeyInput: EditText
    private lateinit var goLiveButton: Button
    private lateinit var stopButton: Button

    private var rtmpClientYouTube: RtmpClient? = null
    private var rtmpClientFacebook: RtmpClient? = null
    private var rtmpClientTikTok: RtmpClient?
private var rtmpClientJsomiTv: RtmpClient?
    private var isStreaming = false
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        youtubeKeyInput = findViewById(R.id.youtubeKeyInput)
        facebookKeyInput = findViewById(R.id.facebookKeyInput)
        tiktokKeyInput = findViewById(R.id.tiktokKeyInput)
jsomiTvKeyInput = findViewById(R.id.jsomiTvKeyInput)
        goLiveButton = findViewById(R.id.goLiveButton)
        stopButton = findViewById(R.id.stopButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        } else {
            startCamera()
        }

        goLiveButton.setOnClickListener { startMultiStream() }
        stopButton.setOnClickListener { stopMultiStream() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission needed", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startMultiStream() {
        val ytKey = youtubeKeyInput.text.toString().trim()
val fbKey = facebookKeyInput.text.toString().trim()
val tiktokKey = tiktokKeyInput.text.toString().trim()
val jsomiKey = jsomiTvKeyInput.text.toString().trim()

        if (ytKey.isEmpty() && fbKey.isEmpty() && tiktokKey.isEmpty() && jsomiKey.isEmpty()) {
            Toast.makeText(this, "Paste at least 1 Stream Key - YouTube or Facebook", Toast.LENGTH_LONG).show()
            return
        }

        isStreaming = true

        // YouTube RTMP: rtmp://a.rtmp.youtube.com/live2/YOUR_KEY
        if (ytKey.isNotEmpty()) {
            rtmpClientYouTube = RtmpClient()
            Toast.makeText(this, "Connecting to YouTube...", Toast.LENGTH_SHORT).show()
        }

        // Facebook RTMP: rtmps://live-api-s.facebook.com:443/rtmp/YOUR_KEY
        if (fbKey.isNotEmpty()) {
            rtmpClientFacebook = RtmpClient()
            Toast.makeText(this, "Connecting to Facebook...", Toast.LENGTH_SHORT).show()
        }
        // TikTok RTMP: Paste full RTMP from TikTok Live Studio
if (tiktokKey.isNotEmpty()) {
    rtmpClientTikTok = RtmpClient()
    Toast.makeText(this, "Connecting to TikTok...", Toast.LENGTH_SHORT).show()
}

// JSOMI TV RTMP: rtmp://your-jsomi-server/live/YOUR_KEY
if (jsomiKey.isNotEmpty()) {
    rtmpClientJsomiTv = RtmpClient()
    Toast.makeText(this, "Connecting to JSOMI TV...", Toast.LENGTH_SHORT).show()
}

        Toast.makeText(this, "JSOMI LIVE STARTED", Toast.LENGTH_LONG).show()
    }

    private fun stopMultiStream() {
        isStreaming = false
        rtmpClientYouTube?.disconnect()
        rtmpClientFacebook?.disconnect()
        rtmpClientTikTok?.disconnect()
rtmpClientJsomiTv?.disconnect()
        Toast.makeText(this, "Stream Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
