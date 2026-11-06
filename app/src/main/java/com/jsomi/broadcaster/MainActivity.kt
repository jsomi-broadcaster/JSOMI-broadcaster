package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var youtubeKeyInput: EditText
    private lateinit var facebookKeyInput: EditText
    private lateinit var goLiveButton: Button
    private lateinit var stopButton: Button

    private var rtmpClientYouTube: RtmpClient? = null
    private var rtmpClientFacebook: RtmpClient? = null
    private var isStreaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        youtubeKeyInput = findViewById(R.id.youtubeKeyInput)
        facebookKeyInput = findViewById(R.id.facebookKeyInput)
        goLiveButton = findViewById(R.id.goLiveButton)
        stopButton = findViewById(R.id.stopButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }

        goLiveButton.setOnClickListener { startMultiStream() }
        stopButton.setOnClickListener { stopMultiStream() }
    }

    private fun startMultiStream() {
        val ytKey = youtubeKeyInput.text.toString().trim()
        val fbKey = facebookKeyInput.text.toString().trim()

        if (ytKey.isEmpty() && fbKey.isEmpty()) {
            Toast.makeText(this, "Paste at least one Stream Key - YouTube or Facebook", Toast.LENGTH_LONG).show()
            return
        }

        isStreaming = true

        // YouTube RTMP: rtmp://a.rtmp.youtube.com/live2/YOUR_KEY
        if (ytKey.isNotEmpty()) {
            rtmpClientYouTube = RtmpClient()
            // Connect in background - camera frame sending goes here
            Toast.makeText(this, "Connecting to YouTube...", Toast.LENGTH_SHORT).show()
        }

        // Facebook RTMP: rtmps://live-api-s.facebook.com:443/rtmp/YOUR_KEY
        if (fbKey.isNotEmpty()) {
            rtmpClientFacebook = RtmpClient()
            Toast.makeText(this, "Connecting to Facebook...", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "JSOMI LIVE STARTED - Multi Stream", Toast.LENGTH_LONG).show()
    }

    private fun stopMultiStream() {
        isStreaming = false
        rtmpClientYouTube?.disconnect()
        rtmpClientFacebook?.disconnect()
        Toast.makeText(this, "Stream Stopped", Toast.LENGTH_SHORT).show()
    }
}
