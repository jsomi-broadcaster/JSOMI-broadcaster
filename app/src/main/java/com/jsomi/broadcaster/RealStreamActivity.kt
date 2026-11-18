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

    private fun call(name: String, vararg args: Any?) {
        val c = rtmpCamera ?: return
        val method = c.javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.size == args.size }
        try { method?.invoke(c, *args) } catch (e: Exception) { statusText.text = "Call $name failed: ${e.cause ?: e}" }
    }
    private fun callBool(name: String): Boolean {
        val c = rtmpCamera ?: return false
        val method = c.javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }
        return try { (method?.invoke(c) as? Boolean) ?: false } catch (e: Exception) { false }
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

        // Try EVERY 2-arg constructor, not just the OpenGlView one.
        // OpenGlView extends SurfaceView, so SurfaceView constructor also works.
        var lastError: String = "none"
        try {
            val ctors = RtmpCamera1::class.java.constructors.filter { it.parameterTypes.size == 2 }
            for (ctor in ctors) {
                try {
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
                            "onNewBitrate", "onNewBitrateRtmp" -> { }
                        }
                        // Return correct default for non-void methods
                        if (method.returnType == Boolean::class.javaPrimitiveType) false else null
                    }
                    // First arg can be OpenGlView, SurfaceView, TextureView, or Context
                    // OpenGlView works for all view ones because it IS a SurfaceView
                    val firstArg: Any = if (ctor.parameterTypes[0].isAssignableFrom(openGlView.javaClass)) openGlView else this
                    rtmpCamera = ctor.newInstance(firstArg, checker)
                    if (rtmpCamera != null) break
                } catch (e: Exception) {
                    lastError = "${e.javaClass.simpleName}: ${e.cause ?: e.message}"
                }
            }
            if (rtmpCamera == null) statusText.text = "ERROR creating streamer:\n$lastError"
        } catch (e: Exception) {
            statusText.text = "ERROR creating streamer:\n${e.javaClass.simpleName}: ${e.cause ?: e.message}"
        }

        startBtn.setOnClickListener {
            if (rtmpCamera == null) {
                statusText.text = "Streamer not created, cannot start. Reopen screen."
                return@setOnClickListener
            }
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
