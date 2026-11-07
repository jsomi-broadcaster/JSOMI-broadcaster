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
import com.pedro.library.util.sources.audio.MicrophoneSource
import com.pedro.library.util.sources.video.Camera2Source
import com.pedro.library.view.OpenGlView
import com.pedro.rtmp.utils.ConnectCheckerRtmp

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp {

    private lateinit var openGlView: OpenGlView
    private lateinit var youtubeKeyInput: EditText
    private lateinit var facebookKeyInput: EditText
    private lateinit var tiktokKeyInput: EditText
    private lateinit var jsomiTvKeyInput: EditText
    private lateinit var goLiveButton: Button
    private lateinit var stopButton: Button

    private lateinit var genericStream: GenericStream
    private var isStreaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openGlView = findViewById(R.id.openGlView)
        youtubeKeyInput = findViewById(R.id.youtubeKeyInput)
        facebookKeyInput = findViewById(R.id.facebookKeyInput)
        tiktokKeyInput = findViewById(R.id.tiktokKeyInput)
        jsomiTvKeyInput = findViewById(R.id.jsomiTvKeyInput)
        goLiveButton = findViewById(R.id.goLiveButton)
        stopButton = findViewById(R.id.stopButton)

        // Pedro GenericStream = 1 camera + 1 mic -> many RTMP destinations
        genericStream = GenericStream(
    this, openGlView, this
)
        
        genericStream.getStreamClient().setReTries(10)

        goLiveButton.setOnClickListener { startMultiStream() }
        stopButton.setOnClickListener { stopMultiStream() }

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                100
            )
        } else {
            genericStream.startPreview()
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            genericStream.startPreview()
        } else {
            Toast.makeText(this, "Camera + Mic permission needed", Toast.LENGTH_LONG).show()
        }
    }

    private fun startMultiStream() {
        val ytKey = youtubeKeyInput.text.toString().trim()
        val fbKey = facebookKeyInput.text.toString().trim()
        val tiktokUrl = tiktokKeyInput.text.toString().trim()
        val jsomiUrl = jsomiTvKeyInput.text.toString().trim()

        val urls = mutableListOf<String>()
        if (ytKey.isNotEmpty()) urls.add("rtmp://a.rtmp.youtube.com/live2/$ytKey")
        if (fbKey.isNotEmpty()) urls.add("rtmps://live-api-s.facebook.com:443/rtmp/$fbKey")
        if (tiktokUrl.isNotEmpty()) urls.add(tiktokUrl) // paste FULL TikTok URL from Live Studio
        if (jsomiUrl.isNotEmpty()) urls.add(jsomiUrl) // paste FULL JSOMI TV URL

        if (urls.isEmpty()) {
            Toast.makeText(this, "Paste at least 1 stream key", Toast.LENGTH_SHORT).show()
            return
        }
        if (isStreaming) return

        // Prepare encoder 1280x720
        if (!genericStream.isOnPreview) genericStream.startPreview()
        genericStream.prepareVideo(1280, 720, 30, 2500 * 1024, 0, 2)
        genericStream.prepareAudio(128 * 1024, 44100, true, false, false)

        // Add all destinations - same video goes to all
        for (url in urls) {
            genericStream.getStreamClient().addClient(url)
        }
        genericStream.startStream(urls[0])
        isStreaming = true
        Toast.makeText(this, "LIVE to ${urls.size} place(s) - JEHOVAH SHALOM!", Toast.LENGTH_LONG).show()
    }

    private fun stopMultiStream() {
        if (!isStreaming) return
        genericStream.stopStream()
        genericStream.getStreamClient().removeClients()
        isStreaming = false
        Toast.makeText(this, "Stream Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread { Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show() }
    }
    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread { Toast.makeText(this, "Failed: $reason", Toast.LENGTH_LONG).show() }
    }
    override fun onNewBitrateRtmp(bitrate: Long) {}
    override fun onDisconnectRtmp() {
        runOnUiThread { isStreaming = false }
    }
    override fun onAuthErrorRtmp() {
        runOnUiThread { Toast.makeText(this, "Auth Error - Check Stream Key", Toast.LENGTH_LONG).show() }
    }
    override fun onAuthSuccessRtmp() {}

    override fun onDestroy() {
        super.onDestroy()
        if (isStreaming) genericStream.stopStream()
        genericStream.stopPreview()
    }
}
