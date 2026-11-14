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
import com.pedro.rtmp.utils.ConnectCheckerRtmp
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

    private var rtmpCamera: RtmpCamera1? = null
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
        tvBible.text = prefs.getString("BIBLE", "John 3:16 - For God so loved the world")
        tvTicker.text = prefs.getString("TICKER", "Welcome to JSTV - Jesus Saves TV")

        etTicker.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { tvTicker.text = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        btnSetBible.setOnClickListener {
            val v = etBibleInput.text.toString()
            if (v.isNotEmpty()) {
                tvBible.text = v
                prefs.edit().putString("BIBLE", v).apply()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 123)
        }

        btnGoLive.setOnClickListener {
            if (!isLive) startMultiStream(prefs) else stopStream()
        }
    }

    private fun makeChecker(name: String): ConnectCheckerRtmp {
        return object : ConnectCheckerRtmp {
            override fun onConnectionSuccessRtmp() {
                runOnUiThread { tvStatus.text = "LIVE TO: $name - Connected" }
            }
            override fun onConnectionFailedRtmp(reason: String) {
                runOnUiThread { tvStatus.text = "Failed $name: $reason" }
            }
            override fun onNewBitrateRtmp(bitrate: Long) {}
        }
    }

    private fun startMultiStream(prefs: android.content.SharedPreferences) {
        prefs.edit()
            .putString("YT", etYTKey.text.toString())
            .putString("FB", etFBKey.text.toString())
            .putString("TIKTOK", etTikTokKey.text.toString())
            .putString("CUSTOM_URL", etCustomUrl.text.toString())
            .putString("CUSTOM_KEY", etCustomKey.text.toString())
            .putString("BIBLE", tvBible.text.toString())
            .putString("TICKER", tvTicker.text.toString())
            .apply()

        val dest = when {
            cbYT.isChecked && etYTKey.text.toString().trim().isNotEmpty() ->
                Pair(YT_URL + etYTKey.text.toString().trim(), "YouTube")
            cbFB.isChecked && etFBKey.text.toString().trim().isNotEmpty() ->
                Pair(FB_URL + etFBKey.text.toString().trim(), "Facebook")
            cbTikTok.isChecked && etTikTokKey.text.toString().trim().isNotEmpty() ->
                Pair(TIKTOK_URL + etTikTokKey.text.toString().trim(), "TikTok")
            cbCustom.isChecked && etCustomKey.text.toString().trim().isNotEmpty() ->
                Pair(etCustomUrl.text.toString().trim() + "/" + etCustomKey.text.toString().trim(), "Custom")
            else -> null
        }

        if (dest == null) {
            Toast.makeText(this, "Tick one platform and enter its Stream Key", Toast.LENGTH_LONG).show()
            return
        }

        val checkedCount = listOf(cbYT, cbFB, cbTikTok, cbCustom).count { it.isChecked }
        if (checkedCount > 1) {
            Toast.makeText(this, "Streaming to ${dest.second} only. For true multi-stream use Restream.io as Custom URL.", Toast.LENGTH_LONG).show()
        }

        rtmpCamera = RtmpCamera1(surfaceView, makeChecker(dest.second))
        rtmpCamera?.startPreview()
        if (rtmpCamera?.prepareVideo(1280, 720, 30, 2500 * 1024, 0, 2) == true &&
            rtmpCamera?.prepareAudio(128 * 1024, 32000, true, false, false) == true) {
            rtmpCamera?.startStream(dest.first)
            isLive = true
            btnGoLive.text = "STOP LIVE"
            tvStatus.text = "LIVE TO: ${dest.second}"
        } else {
            tvStatus.text = "Failed to prepare camera"
        }
    }

    private fun stopStream() {
        try {
            rtmpCamera?.stopStream()
            rtmpCamera?.stopPreview()
        } catch (e: Exception) { }
        rtmpCamera = null
        isLive = false
        btnGoLive.text = "GO LIVE"
        tvStatus.text = "Offline"
    }

    override fun onDestroy() {
        if (isLive) stopStream()
        super.onDestroy()
    }
}
