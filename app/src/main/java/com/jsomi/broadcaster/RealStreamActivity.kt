package com.jsomi.broadcaster

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.pedro.rtmp.rtmp.RtmpCamera1
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import android.view.SurfaceView

class RealStreamActivity : AppCompatActivity(), ConnectCheckerRtmp {

    private lateinit var rtmpCamera: RtmpCamera1
    private lateinit var surfaceView: SurfaceView
    private lateinit var statusText: TextView
    private var rtmpUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val dest = prefs.getString("destination", "YouTube") ?: "YouTube"
        val key = prefs.getString("streamKey", "") ?: ""
        // Real RTMP URLs
        val baseUrl = if (dest == "Facebook") "rtmp://live-api-s.facebook.com:80/rtmp/"
                      else "rtmp://a.rtmp.youtube.com/live2/"
        rtmpUrl = baseUrl + key

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        surfaceView = SurfaceView(this)
        statusText = TextView(this).apply {
            text = "Destination: $dest\nReady - Press START to go REAL LIVE"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            setPadding(20, 20, 20, 20)
        }
        val startBtn = Button(this).apply {
            text = "START REAL LIVE"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
        }
        val stopBtn = Button(this).apply { text = "STOP LIVE" }

        rtmpCamera = RtmpCamera1(surfaceView, this)

        startBtn.setOnClickListener {
            if (key.isEmpty()) {
                statusText.text = "ERROR: No Stream Key. Go back to Live Setup and paste your YouTube/Facebook Stream Key."
                return@setOnClickListener
            }
            if (!rtmpCamera.isStreaming) {
                rtmpCamera.startPreview()
                rtmpCamera.startStream(rtmpUrl)
                statusText.text = "LIVE NOW - Streaming to $dest\nCheck YouTube/Facebook to confirm"
            }
        }
        stopBtn.setOnClickListener {
            if (rtmpCamera.isStreaming) rtmpCamera.stopStream()
            rtmpCamera.stopPreview()
            statusText.text = "Stopped"
        }

        layout.addView(surfaceView, LinearLayout.LayoutParams(-1, 0, 1f))
        layout.addView(statusText)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread { statusText.text = "CONNECTED - YOU ARE LIVE FOR REAL!" }
    }
    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread { statusText.text = "FAILED: $reason\nCheck Stream Key + Internet" }
    }
    override fun onNewBitrateRtmp(bitrate: Long) {}
    override fun onDisconnectRtmp() {
        runOnUiThread { statusText.text = "Disconnected" }
    }
    override fun onAuthErrorRtmp() {
        runOnUiThread { statusText.text = "AUTH ERROR - Wrong Stream Key" }
    }
    override fun onAuthSuccessRtmp() {
        runOnUiThread { statusText.text = "Auth OK - LIVE!" }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::rtmpCamera.isInitialized && rtmpCamera.isStreaming) rtmpCamera.stopStream()
    }
}
