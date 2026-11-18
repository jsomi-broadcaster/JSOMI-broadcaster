package com.jsomi.broadcaster

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.pedro.library.rtmp.RtmpCamera1
import com.pedro.library.view.OpenGlView
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class RealStreamActivity : AppCompatActivity() {

    private var rtmpCamera: Any? = null
    private lateinit var openGlView: OpenGlView
    private lateinit var statusText: TextView
    private var rtmpUrl = ""

    // Call a method on RtmpCamera1 by reflection, so we never need to import ConnectChecker
    private fun call(name: String, vararg args: Any?) {
        val c = rtmpCamera ?: return
        val method = c.javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.size == args.size }
        method?.invoke(c, *args)
    }
    private fun callBool(name: String): Boolean {
        val c = rtmpCamera ?: return false
        val method = c.javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }
        return (method?.invoke(c) as? Boolean) ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val channels = prefs.getString("selected_channels", "") ?: ""
        val fbUrl = prefs.getString("fb_full_url", "") ?: ""
        val ytUrl = prefs.getString("yt_full_url", "") ?: ""
        val ttUrl = prefs.getString("tt_full_url", "") ?: ""
        val dest = when {
            channels.contains("FACEBOOK") -> "Facebook"
            channels.contains("TIKTOK") -> "TikTok"
            else -> "YouTube"
        }
        val key = when (dest) {
            "Facebook" -> fbUrl
            "TikTok" -> ttUrl
            else -> ytUrl
        }
        rtmpUrl = key

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        openGlView = OpenGlView(this)
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

        // Build the ConnectChecker proxy without importing its package.
        // We get its Class from the RtmpCamera1 constructor itself.
        try {
            val ctor = RtmpCamera1::class.java.constructors.first { it.parameterTypes.size == 2 && it.parameterTypes[0] == OpenGlView::class.java }
            val checkerClass = ctor.parameterTypes[1]
            val checker = Proxy.newProxyInstance(
                checkerClass.classLoader,
                arrayOf(checkerClass)
            ) { _: Any, method: Method, args: Array<Any?>? ->
                when (method.name) {
                    "onConnectionSuccess", "onConnectionSuccessRtmp" -> runOnUiThread { statusText.text = "CONNECTED - YOU ARE LIVE FOR REAL!" }
                    "onConnectionFailed", "onConnectionFailedRtmp" -> runOnUiThread { statusText.text = "FAILED: ${args?.getOrNull(0)}\nCheck Stream Key + Internet" }
                    "onDisconnect", "onDisconnectRtmp" -> runOnUiThread { statusText.text = "Disconnected" }
                    "onAuthError", "onAuthErrorRtmp" -> runOnUiThread { statusText.text = "AUTH ERROR - Wrong Stream Key" }
                    "onAuthSuccess", "onAuthSuccessRtmp" -> runOnUiThread { statusText.text = "Auth OK - LIVE!" }
                    "onNewBitrate", "onNewBitrateRtmp" -> { /* ignore */ }
                }
                null
            }
            rtmpCamera = ctor.newInstance(openGlView, checker)
        } catch (e: Exception) {
            statusText.text = "ERROR creating streamer: ${e.message}"
        }

        startBtn.setOnClickListener {
            if (key.isEmpty()) {
                statusText.text = "ERROR: No Stream Key. Go back to Live Setup and paste your YouTube/Facebook Stream Key."
                return@setOnClickListener
            }
            if (!callBool("isStreaming")) {
                call("startPreview")
                call("startStream", rtmpUrl)
                statusText.text = "LIVE NOW - Streaming to $dest\nCheck YouTube/Facebook to confirm"
            }
        }
        stopBtn.setOnClickListener {
            if (callBool("isStreaming")) call("stopStream")
            call("stopPreview")
            statusText.text = "Stopped"
        }

        layout.addView(openGlView, LinearLayout.LayoutParams(-1, 0, 1f))
        layout.addView(statusText)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (callBool("isStreaming")) call("stopStream")
    }
}
