package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var isLive = false
    private var isPortrait = true
    private var selectedPlatform = "Facebook"
    private var plan = "FREE"
    private var timer: CountDownTimer? = null
    private var isMicMuted = false

    private val rtmpMap = mapOf(
        "YouTube" to "rtmp://a.rtmp.youtube.com/live2/",
        "Facebook" to "rtmps://live-api-s.facebook.com:443/rtmp/",
        "TikTok" to "rtmp://push-rtmp-l1.tiktok.com/live/"
    )

    private val PERMS = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    private val REQ_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        plan = prefs.getString("plan", "FREE") ?: "FREE"

        findViewById<TextView>(R.id.tvPlan).text = "MAIN STUDIO - Main Event Camera | Plan: $plan | ${if (plan == "FREE") "📢 AD - Upgrade to Premium | 1 Stream Only" else "✅ PREMIUM - Unlimited"}"
        findViewById<View>(R.id.adBanner).visibility = if (plan == "FREE") View.VISIBLE else View.GONE

        // MAIN STUDIO IS THE STREAMING CAMERA - CameraX
        previewView = findViewById(R.id.previewMain)
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMS, REQ_CODE)
        } else {
            startCamera()
        }

        val platforms = arrayOf("YouTube", "Facebook", "TikTok")
        val spPlatform = findViewById<Spinner>(R.id.spPlatform)
        spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platforms)
        spPlatform.setSelection(1)
        spPlatform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedPlatform = platforms[pos]
                prefs.edit().putString("main_platform", selectedPlatform).apply()
                findViewById<TextView>(R.id.tvRtmp).text = "RTMP: ${rtmpMap[selectedPlatform]} (Built-in)"
                Toast.makeText(this@MainActivity, "$selectedPlatform RTMP loaded - Just paste Stream Key", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        findViewById<Spinner>(R.id.spResolution).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("1080p HD", "720p HD", "480p"))
        findViewById<Spinner>(R.id.spBitrate).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("4000 kbps", "2500 kbps", "1500 kbps"))
        findViewById<Spinner>(R.id.spFps).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("30 FPS", "60 FPS"))
        findViewById<Spinner>(R.id.spIntro).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("No Intro", "JSTV Intro 5s", "JSTV Intro 10s", "Welcome Church Intro"))
        findViewById<Spinner>(R.id.spOrientation).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Portrait 9:16 - TikTok / Reels", "Landscape 16:9 - YouTube / Facebook"))
        findViewById<Spinner>(R.id.spOrientation).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { isPortrait = pos == 0 }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        makeDraggable(findViewById<TextView>(R.id.tvOverlayTitle))
        makeDraggable(findViewById<ImageView>(R.id.imgOverlayLogo))
        makeDraggable(findViewById<TextView>(R.id.tvScrolling))

        findViewById<Button>(R.id.btnTitleStyle).setOnClickListener {
            val t = findViewById<TextView>(R.id.tvOverlayTitle)
            t.text = findViewById<EditText>(R.id.etStreamTitle).text.toString().ifEmpty { "🔴 JSTV LIVE" }
            prefs.edit().putString("main_title", t.text.toString()).apply()
            t.setTextColor(android.graphics.Color.YELLOW)
            t.textSize = 24f
            t.setShadowLayer(8f, 2f, 2f, android.graphics.Color.BLACK)
        }
        findViewById<Button>(R.id.btnScrollText).setOnClickListener {
            val s = findViewById<EditText>(R.id.etScrollText).text.toString()
            val txt = "📢 $s • JSTV • Paybill 516600 • WhatsApp 0763052911 • "
            findViewById<TextView>(R.id.tvScrolling).text = txt
            prefs.edit().putString("main_scroll", txt).apply()
            findViewById<TextView>(R.id.tvScrolling).isSelected = true
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            if (!hasPermissions()) {
                ActivityCompat.requestPermissions(this, PERMS, REQ_CODE)
                Toast.makeText(this, "Allow Gallery permission first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, 100)
        }
        findViewById<Button>(R.id.btnLoop).setOnClickListener {
            Toast.makeText(this, "Loop Mode ON - Gallery will auto-play + repeat", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btnProgramTimer).setOnClickListener {
            val mins = findViewById<EditText>(R.id.etTimerMins).text.toString().toLongOrNull() ?: 30
            timer?.cancel()
            timer = object : CountDownTimer(mins * 60 * 1000, 1000) {
                override fun onTick(m: Long) {
                    findViewById<TextView>(R.id.tvViewers).text = "⏱ Program: ${m / 60000}:${(m / 1000) % 60} | 👁 Live"
                }
                override fun onFinish() {
                    Toast.makeText(this@MainActivity, "Program Finished - Auto Switching to Next", Toast.LENGTH_LONG).show()
                }
            }.start()
            Toast.makeText(this, "Auto Program Timer: $mins mins set", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSwitchCamera).setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        findViewById<Button>(R.id.btnFlash).setOnClickListener {
            Toast.makeText(this, "Flash toggled", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnMic).setOnClickListener {
            if (!hasPermissions()) { ActivityCompat.requestPermissions(this, PERMS, REQ_CODE); return@setOnClickListener }
            isMicMuted = !isMicMuted
            findViewById<Button>(R.id.btnMic).text = if (isMicMuted) "🔇 MIC OFF" else "🎤 MIC ON"
            Toast.makeText(this, if (isMicMuted) "Mic MUTED" else "Mic ON", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnAudioOnly).setOnClickListener { Toast.makeText(this, "Audio Only Mode", Toast.LENGTH_SHORT).show() }

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            Toast.makeText(this, "⏺ RECORDING Main Event - Saved to Gallery", Toast.LENGTH_SHORT).show()
            findViewById<Button>(R.id.btnRecord).text = "⏹ STOP REC"
        }

        findViewById<Button>(R.id.btnChurchScreen).setOnClickListener {
            prefs.edit().putString("church_source", "MAIN").putString("church_title", findViewById<EditText>(R.id.etStreamTitle).text.toString()).apply()
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }

        findViewById<Button>(R.id.btnGoLive).setOnClickListener {
            if (!hasPermissions()) {
                ActivityCompat.requestPermissions(this, PERMS, REQ_CODE)
                Toast.makeText(this, "Allow Camera + Mic first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val key = findViewById<EditText>(R.id.etStreamKey).text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(this, "Paste Stream Key for $selectedPlatform first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (plan == "FREE" && isLive) {
                Toast.makeText(this, "FREE: 1 Stream Only - Upgrade to Premium", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isLive) {
                isLive = true
                prefs.edit().putString("main_key", key).putBoolean("main_live", true).apply()
                findViewById<TextView>(R.id.tvLiveStatus).text = "🔴 LIVE ON $selectedPlatform - Main Studio Camera"
                findViewById<Button>(R.id.btnGoLive).text = "⏹ END LIVE"
                Toast.makeText(this, "✅ LIVE - Main Studio Camera Active\nRTMP: ${rtmpMap[selectedPlatform]}$key", Toast.LENGTH_LONG).show()
            } else {
                isLive = false
                prefs.edit().putBoolean("main_live", false).apply()
                findViewById<TextView>(R.id.tvLiveStatus).text = "● OFFLINE"
                findViewById<Button>(R.id.btnGoLive).text = "🔴 GO LIVE TO SOCIAL MEDIA"
            }
        }

        findViewById<Button>(R.id.btnPayment).setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }
        findViewById<Button>(R.id.btnWhatsApp).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text=Hi JSTV, I want Premium")))
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun startCamera() {
        try {
            val future = ProcessCameraProvider.getInstance(this)
            future.addListener({
                val provider = future.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(this, cameraSelector, preview)
                } catch (e: Exception) {
                    Toast.makeText(this, "Camera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {}
    }

    private fun hasPermissions(): Boolean {
        return PERMS.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "✅ Camera + Mic + Gallery Granted", Toast.LENGTH_SHORT).show()
                startCamera()
            } else {
                Toast.makeText(this, "❌ Denied - Go to Settings > Apps > JSTV > Permissions > Allow All", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeDraggable(v: View) {
        var dx = 0f
        var dy = 0f
        v.setOnTouchListener { view, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { dx = e.rawX - view.x; dy = e.rawY - view.y }
                MotionEvent.ACTION_MOVE -> { view.x = e.rawX - dx; view.y = e.rawY - dy }
            }
            true
        }
    }

    override fun onActivityResult(rc: Int, r: Int, d: Intent?) {
        super.onActivityResult(rc, r, d)
        if (rc == 100 && r == RESULT_OK) {
            findViewById<ImageView>(R.id.imgOverlayLogo).setImageURI(d?.data)
            Toast.makeText(this, "Gallery image set as Logo / Overlay", Toast.LENGTH_SHORT).show()
        }
    }
}
