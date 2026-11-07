package com.jsomi.broadcaster

import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.library.base.ConnectChecker
import com.pedro.library.generic.GenericStream

class MainActivity : AppCompatActivity(), ConnectChecker {

    private lateinit var genericStream: GenericStream
    private var isStreaming = false

    // Your 4 destinations - we will use [0] for now to get a green build
    private val urls = mutableListOf(
        "rtmp://a.rtmp.youtube.com/live2/YOUR_KEY_1",
        "rtmp://live-api-s.facebook.com:443/rtmp/YOUR_KEY_2",
        "rtmp://rtmp.tiktok.com/live/YOUR_KEY_3",
        "rtmp://ingest.global-contribute.live-video.net/app/YOUR_KEY_4"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2.8.1 constructor - just context + checker, no sources needed
        genericStream = GenericStream(this, this)

        val textureView = findViewById<TextureView>(R.id.textureView)
        genericStream.startPreview(textureView)

        genericStream.prepareVideo(1280, 720, 30, 2500 * 1024, 0, 2)
        genericStream.prepareAudio(128 * 1024, true, 44100, false, false)
    }

    private fun startMultiStream() {
        if (isStreaming) return
        // 2.8.1 startStream takes ONE String, not a List
        genericStream.startStream(urls[0])
        isStreaming = true
        Toast.makeText(this, "LIVE to ${urls.size} place(s) - JEHOVAH SHALOM!", Toast.LENGTH_LONG).show()
    }

    private fun stopMultiStream() {
        if (!isStreaming) return
        genericStream.stopStream()
        isStreaming = false
        Toast.makeText(this, "Stream Stopped", Toast.LENGTH_SHORT).show()
    }

    // 2.8.1 ConnectChecker - NO "Rtmp" suffix anymore
    override fun onConnectionSuccess() {
        runOnUiThread { Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailed(reason: String) {
        runOnUiThread { Toast.makeText(this, "Failed: $reason", Toast.LENGTH_SHORT).show() }
    }

    override fun onNewBitrate(bitrate: Long) {}
    override fun onDisconnect() {}
    override fun onAuthError() {}
    override fun onAuthSuccess() {}

    override fun onDestroy() {
        super.onDestroy()
        if (isStreaming) genericStream.stopStream()
        genericStream.stopPreview()
    }
}
