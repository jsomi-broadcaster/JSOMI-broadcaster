package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.library.generic.GenericStream
import com.pedro.library.view.OpenGlView

class MainActivity : AppCompatActivity(), ConnectChecker {

    private lateinit var genericStream: GenericStream
    private lateinit var openGlView: OpenGlView
    private var isStreaming = false
    private var previewStarted = false

    private lateinit var youtubeKeyInput: EditText
    private lateinit var facebookKeyInput: EditText
    private lateinit var tiktokKeyInput: EditText
    private lateinit var jsomiTvKeyInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openGlView = findViewById(R.id.openGlView)
        youtubeKeyInput = findViewById(R.id.youtubeKeyInput)
        facebookKeyInput = findViewById(R.id.facebookKeyInput)
        tiktokKeyInput = findViewById(R.id.tiktokKeyInput)
        jsomiTvKeyInput = findViewById(R.id.jsomiTvKeyInput)

        findViewById<Button>(R.id.goLiveButton).setOnClickListener { startMultiStream() }
        findViewById<Button>(R.id.stopButton).setOnClickListener { stopMultiStream() }

        genericStream = GenericStream(this, this)

        if (hasPermissions()) {
            startPreviewSafe()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                1001
            )
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startPreviewSafe()
        } else {
            Toast.makeText(this, "Camera + Mic permission needed to go live", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPreviewSafe() {
        if (previewStarted) return
        try {
            genericStream.startPreview(openGlView)
            genericStream.prepareVideo(1280, 720, 30, 2500 * 1024, 0, 2)
            genericStream.prepareAudio(128 * 1024, true, 44100, false, false)
            previewStarted = true
        } catch (e: Exception) {
            Toast.makeText(this, "Preview failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getUrls(): List<String> {
        val list = mutableListOf<String>()
        val yt = youtubeKeyInput.text.toString().trim()
        val fb = facebookKeyInput.text.toString().trim()
        val tk = tiktokKeyInput.text.toString().trim()
        val tv = jsomiTvKeyInput.text.toString().trim()
        if (yt.isNotEmpty()) list.add(yt)
        if (fb.isNotEmpty()) list.add(fb)
        if (tk.isNotEmpty()) list.add(tk)
        if (tv.isNotEmpty()) list.add(tv)
        return list
    }

    private fun startMultiStream() {
        if (isStreaming) return
        val urls = getUrls()
        if (urls.isEmpty()) {
            Toast.makeText(this, "Paste at least 1 stream key / RTMP URL first", Toast.LENGTH_LONG).show()
            return
        }
        genericStream.startStream(urls[0])
        isStreaming = true
        Toast.makeText(this, "LIVE - JEHOVAH SHALOM!", Toast.LENGTH_LONG).show()
    }

    private fun stopMultiStream() {
        if (!isStreaming) return
        genericStream.stopStream()
        isStreaming = false
        Toast.makeText(this, "Stream Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isStreaming) genericStream.stopStream()
        if (previewStarted) genericStream.stopPreview()}
            override fun onConnectionStarted(url: String) {
        runOnUiThread { Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show() }
    }
    override fun onConnectionSuccess() {
        runOnUiThread { Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show() }
    }
    override fun onConnectionFailed(reason: String) {
        runOnUiThread { Toast.makeText(this, "Failed: $reason", Toast.LENGTH_LONG).show() }
    }
    override fun onNewBitrate(bitrate: Long) {}
    override fun onDisconnect() {
        runOnUiThread { Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show() }
    }
    override fun onAuthError() {}
    override fun onAuthSuccess() {}
    }

