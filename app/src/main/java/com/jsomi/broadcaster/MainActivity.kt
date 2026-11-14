package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.base.ConnectCheckerRtmp
import android.view.SurfaceView

class MainActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private lateinit var btnGoLive: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvBible: TextView
    private lateinit var tvTicker: TextView
    private lateinit var etTicker: EditText
    private lateinit var etYTKey: EditText
    private lateinit var etFBKey: EditText
    private lateinit var etTikTokKey: EditText
    private lateinit var etCustomUrl: EditText
    private lateinit var etCustomKey: EditText
    private lateinit var cbYT: CheckBox
    private lateinit var cbFB: CheckBox
    private lateinit var cbTikTok: CheckBox
    private lateinit var cbCustom: CheckBox
    private lateinit var etBibleInput: EditText
    private lateinit var btnSetBible: Button

    private var rtmpYT: RtmpCamera1? = null
    private var rtmpFB: RtmpCamera1? = null
    private var rtmpTikTok: RtmpCamera1? = null
    private var rtmpCustom: RtmpCamera1? = null
    private var isLive = false

    private val YT_URL = "rtmp://a.rtmp.youtube.com/live2/"
    private val FB_URL = "rtmps://live-api-s.facebook.com:443/rtmp/"
    private val TIKTOK_URL = "rtmp://rtmp-push.tiktok.com/live/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.cameraView)
        btnGoLive = findViewById(R.id.btnGoLive)
        tvStatus = findViewById(R.id.tvStatus)
        tvBible = findViewById(R.id.tvBible)
        tvTicker = findViewById(R.id.tvTicker)
        etTicker = findViewById(R.id.etTicker)
        etYTKey = findViewById(R.id.etYTKey)
        etFBKey = findViewById(R.id.etFBKey)
        etTikTokKey = findViewById(R.id.etTikTokKey)
        etCustomUrl = findViewById(R.id.etCustomUrl)
        etCustomKey = findViewById(R.id.etCustomKey)
        cbYT = findViewById(R.id.cbYT)
        cbFB = findViewById(R.id.cbFB)
        cbTikTok = findViewById(R.id.cbTikTok)
        cbCustom = findViewById(R.id.cbCustom)
        etBibleInput = findViewById(R.id.etBibleInput)
        btnSetBible = findViewById(R.id.btnSetBible)

        val prefs = getSharedPreferences("JSTV_KEYS", MODE_PRIVATE)
        etYTKey.setText(prefs.getString("YT", ""))
        etFBKey.setText(prefs.getString("FB", ""))
        etTikTokKey.setText(prefs.getString("TIKTOK", ""))
        etCustomUrl.setText(prefs.getString("CUSTOM_URL", ""))
        etCustomKey.setText(prefs.getString("CUSTOM_KEY", ""))
        tvBible.text = prefs.getString("BIBLE", "John 3:16 - For God so loved the world...")
        tvTicker.text = prefs.getString("TICKER", "Welcome to JSTV - Jesus Saves TV")

        etTicker.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { tvTicker.text = s.toString(); prefs.edit().putString("TICKER", s.toString()).apply() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
        btnSetBible.setOnClickListener {
            val v = etBibleInput.text.toString().trim()
            if (v.isNotEmpty()) { tvBible.text = v; prefs.edit().putString("BIBLE", v).apply(); Toast.makeText(this, "Bible Updated", Toast.LENGTH_SHORT).show() }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 100)
        }
        btnGoLive.setOnClickListener { if (!isLive) startMultiStream(prefs) else stopMultiStream() }
    }

    private fun makeChecker(name: String): ConnectCheckerRtmp = object : ConnectCheckerRtmp {
        override fun onConnectionSuccessRtmp() { runOnUiThread { Toast.makeText(this@MainActivity, "$name CONNECTED - LIVE!", Toast.LENGTH_SHORT).show() } }
        override fun onConnectionFailedRtmp(reason: String) { runOnUiThread { Toast.makeText(this@MainActivity, "$name Failed: $reason", Toast.LENGTH_LONG).show() } }
        override fun onNewBitrateRtmp(bitrate: Long) {}
        override fun onDisconnectRtmp() {}
        override fun onAuthErrorRtmp() { runOnUiThread { Toast.makeText(this@MainActivity, "$name Wrong Key!", Toast.LENGTH_LONG).show() } }
        override fun onAuthSuccessRtmp() {}
    }

    private fun startMultiStream(prefs: android.content.SharedPreferences) {
        prefs.edit().putString("YT", etYTKey.text.toString().trim())
            .putString("FB", etFBKey.text.toString().trim())
            .putString("TIKTOK", etTikTokKey.text.toString().trim())
            .putString("CUSTOM_URL", etCustomUrl.text.toString().trim())
            .putString("CUSTOM_KEY", etCustomKey.text.toString().trim()).apply()
        var started = 0
        val status = StringBuilder("LIVE TO: ")
        if (cbYT.isChecked) {
            val key = etYTKey.text.toString().trim()
            if (key.isNotEmpty()) {
                rtmpYT = RtmpCamera1(surfaceView, makeChecker("YouTube"))
                rtmpYT!!.startPreview()
                rtmpYT!!.prepareVideo(1280, 720, 30, 3000*1024, 0, 0)
                rtmpYT!!.prepareAudio(32000, true, 128*1024)
                rtmpYT!!.startAudio(); rtmpYT!!.startVideo()
                rtmpYT!!.startStream(YT_URL + key)
                status.append("YouTube "); started++
            }
        }
        if (cbFB.isChecked) {
            val key = etFBKey.text.toString().trim()
            if (key.isNotEmpty()) {
                rtmpFB = RtmpCamera1(surfaceView, makeChecker("Facebook"))
                if (rtmpYT == null) rtmpFB!!.startPreview()
                rtmpFB!!.prepareVideo(1280, 720, 30, 3000*1024, 0, 0)
                rtmpFB!!.prepareAudio(32000, true, 128*1024)
                rtmpFB!!.startAudio(); rtmpFB!!.startVideo()
                rtmpFB!!.startStream(FB_URL + key)
                status.append("Facebook "); started++
            }
        }
        if (cbTikTok.isChecked) {
            val key = etTikTokKey.text.toString().trim()
            if (key.isNotEmpty()) {
                val fullUrl = if (key.startsWith("rtmp")) key else TIKTOK_URL + key
                rtmpTikTok = RtmpCamera1(surfaceView, makeChecker("TikTok"))
                if (rtmpYT == null && rtmpFB == null) rtmpTikTok!!.startPreview()
                rtmpTikTok!!.prepareVideo(720, 1280, 30, 3000*1024, 0, 0)
                rtmpTikTok!!.prepareAudio(32000, true, 128*1024)
                rtmpTikTok!!.startAudio(); rtmpTikTok!!.startVideo()
                rtmpTikTok!!.startStream(fullUrl)
                status.append("TikTok "); started++
            }
        }
        if (cbCustom.isChecked) {
            val url = etCustomUrl.text.toString().trim()
            val key = etCustomKey.text.toString().trim()
            if (url.isNotEmpty()) {
                val fullUrl = if (key.isEmpty()) url else if (url.endsWith("/")) url + key else "$url/$key"
                rtmpCustom = RtmpCamera1(surfaceView, makeChecker("Custom RTMP"))
                if (rtmpYT == null && rtmpFB == null && rtmpTikTok == null) rtmpCustom!!.startPreview()
                rtmpCustom!!.prepareVideo(1280, 720, 30, 3000*1024, 0, 0)
                rtmpCustom!!.prepareAudio(32000, true, 128*1024)
                rtmpCustom!!.startAudio(); rtmpCustom!!.startVideo()
                rtmpCustom!!.startStream(fullUrl)
                status.append("Custom "); started++
            }
        }
        if (started == 0) { Toast.makeText(this, "Tick 1 platform + Paste its Key!", Toast.LENGTH_LONG).show(); return }
        tvStatus.text = status.toString()
        btnGoLive.text = "STOP ALL STREAMS"
        isLive = true
    }
private fun stopMultiStream() {
    val streams = listOf(rtmpYT, rtmpFB, rtmpTikTok, rtmpCustom)
    for (stream in streams) {
        if (stream != null && stream.isStreaming) {
            try { stream.stopStream() } catch (e: Exception) {}
            try { stream.stopAudio() } catch (e: Exception) {}
            try { stream.stopVideo() } catch (e: Exception) {}
            try { stream.stopPreview() } catch (e: Exception) {}
        }
    }
    rtmpYT = null; rtmpFB = null; rtmpTikTok = null; rtmpCustom = null
    tvStatus.text = "Offline - Ready"
    btnGoLive.text = "GO LIVE - MULTISTREAM"
    isLive = false
}
    
    override fun onDestroy() { super.onDestroy(); if (isLive) stopMultiStream() }
}
