package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.library.view.OpenGlView
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class RealStreamActivity : AppCompatActivity() {

    private var rtmpCamera: Any? = null
    private var rtmpCameraClass: Class<*>? = null
    private lateinit var openGlView: OpenGlView
    private lateinit var statusText: TextView
    private var rtmpUrl = ""
    private var dest = "YouTube"
    private var lastError = ""

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

    private fun makeChecker(checkerClass: Class<*>): Any {
        return Proxy.newProxyInstance(checkerClass.classLoader, arrayOf(checkerClass)) { _: Any, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "onConnectionSuccess", "onConnectionSuccessRtmp" -> runOnUiThread { statusText.text = "CONNECTED - YOU ARE LIVE FOR REAL!" }
                "onConnectionFailed", "onConnectionFailedRtmp" -> runOnUiThread { statusText.text = "FAILED: ${args?.getOrNull(0)}" }
                "onDisconnect", "onDisconnectRtmp" -> runOnUiThread { statusText.text = "Disconnected" }
                "onAuthError", "onAuthErrorRtmp" -> runOnUiThread { statusText.text = "AUTH ERROR - Wrong Stream Key" }
                "onAuthSuccess", "onAuthSuccessRtmp" -> runOnUiThread { statusText.text = "Auth OK - LIVE!" }
                "onNewBitrate", "onNewBitrateRtmp" -> { }
            }
            if (method.returnType == Boolean::class.javaPrimitiveType) false else null
        }
    }

    private fun tryCreateWith(clazz: Class<*>): Boolean {
        try {
            val ctors = clazz.constructors.filter { it.parameterTypes.size == 2 }
            for (ctor in ctors) {
                try {
                    // Only try OpenGlView / SurfaceView constructors, skip TextureView/Context - they need different view
                    if (!ctor.parameterTypes[0].isAssignableFrom(openGlView.javaClass)) continue
                    val checker = makeChecker(ctor.parameterTypes[1])
                    rtmpCamera = ctor.newInstance(openGlView, checker)
                    if (rtmpCamera != null) {
                        rtmpCameraClass = clazz
                        return true
                    }
                } catch (e: Exception) {
                    lastError += "${clazz.simpleName} ${ctor.parameterTypes[0].simpleName}: ${e.cause ?: e.message}\n"
                }
            }
        } catch (e: Exception) {
            lastError += "${clazz.simpleName}: ${e.cause ?: e.message}\n"
        }
        return false
    }

    private fun tryCreate(): Boolean {
        lastError = ""
        rtmpCamera = null
        // Try Camera1 first, then Camera2 - Camera2 works on most modern phones when Camera1 fails
        if (tryCreateWith(Class.forName("com.pedro.library.rtmp.RtmpCamera1"))) return true
        if (tryCreateWith(Class.forName("com.pedro.library.rtmp.RtmpCamera2"))) return true
        return false
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (hasPermissions()) {
            if (tryCreate()) statusText.text = "Permissions OK - Streamer ready - Press START"
            else statusText.text = "Permissions OK but still failed:\n$lastError"
        } else {
            statusText.text = "PERMISSION DENIED - Go to Settings > Apps > JSOMI > Permissions > Allow Camera + Mic, then reopen"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val channels = prefs.getString("selected_channels", "") ?: ""
        val fbUrl = prefs.getString("fb_full_url", "") ?: ""
        val ytUrl = prefs.getString("yt_full_url", "") ?: ""
        val ttUrl = prefs.getString("tt_full_url", "") ?: ""
        dest = when {
            channels.contains("FACEBOOK") -> "Facebook"
            channels.contains("TIKTOK") -> "TikTok"
            else -> "YouTube"
        }
        rtmpUrl = when (dest) {
            "Facebook" -> fbUrl
            "TikTok" -> ttUrl
            else -> ytUrl
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        openGlView = OpenGlView(this)
        statusText = TextView(this).apply {
            text = "Destination: $dest\nChecking permissions..."
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

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
            statusText.text = "Please Allow Camera + Microphone when asked"
        } else {
            if (tryCreate()) statusText.text = "Streamer ready - Destination: $dest\nPress START REAL LIVE"
            else statusText.text = "ERROR creating streamer:\n$lastError\n\nClose all other camera apps and reopen"
        }

        startBtn.setOnClickListener {
            if (!hasPermissions()) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
                statusText.text = "Need Camera + Mic permission - allow and press START again"
                return@setOnClickListener
            }
            if (rtmpCamera == null) {
                if (tryCreate()) {
                    statusText.text = "Retry OK - press START again"
                    return@setOnClickListener
                } else {
                    statusText.text = "Still failed:\n$lastError\n\nClose Camera app, allow permissions in Settings"
                    return@setOnClickListener
                }
            }
            if (rtmpUrl.isEmpty()) {
                statusText.text = "ERROR: No Stream Key. Go back to Live Setup."
                return@setOnClickListener
            }
            if (!callBool("isStreaming")) {
                call("startPreview")
                call("startStream", rtmpUrl)
                statusText.text = "STREAMING to $dest - Check YouTube/Facebook to confirm"
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
