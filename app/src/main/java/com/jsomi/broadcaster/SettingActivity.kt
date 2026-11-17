package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    // Studio
    private lateinit var channelInput: EditText
    private lateinit var tickerInput: EditText
    private lateinit var verseInput: EditText
    private lateinit var cbLogo: CheckBox
    private lateinit var cbVerse: CheckBox
    private lateinit var cbTicker: CheckBox
    private lateinit var cbBible: CheckBox

    // Camera
    private lateinit var spCamera: Spinner
    private lateinit var spResolution: Spinner
    private lateinit var spFps: Spinner
    private lateinit var spFocus: Spinner
    private lateinit var spFlash: Spinner
    private lateinit var cbStabilization: CheckBox
    private lateinit var cbMirror: CheckBox

    // Video Quality / Orientation
    private lateinit var spQuality: Spinner
    private lateinit var spBitrate: Spinner
    private lateinit var spKeyframe: Spinner
    private lateinit var rgOrientation: RadioGroup
    private lateinit var rbPortrait: RadioButton
    private lateinit var rbLandscape: RadioButton
    private lateinit var cbAutoRotate: CheckBox

    // Audio
    private lateinit var spMic: Spinner
    private lateinit var spAudioQuality: Spinner
    private lateinit var sbVolume: SeekBar
    private lateinit var tvVolume: TextView
    private lateinit var cbNoise: CheckBox
    private lateinit var cbEcho: CheckBox
    private lateinit var cbMusic: CheckBox

    // Stream
    private lateinit var cbReconnect: CheckBox
    private lateinit var cbSaveReplay: CheckBox
    private lateinit var cbShowChat: CheckBox
    private lateinit var cbShowViewers: CheckBox
    private lateinit var spLatency: Spinner
    private lateinit var spEncoder: Spinner

    // Defaults to restore on DISCARD
    private val defaults = mapOf(
        "camera" to 0, "resolution" to 1, "fps" to 1, "focus" to 0, "flash" to 0,
        "quality" to 1, "bitrate" to 1, "keyframe" to 1, "mic" to 0, "audioQuality" to 1,
        "latency" to 1, "encoder" to 0, "volume" to 80
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 48)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        scroll.addView(layout)
        setContentView(scroll)

        layout.addView(sectionTitle("Settings - Studio Setup"))
        layout.addView(subTitle("EVERYTHING IS FREE - All settings saved for your Live"))

        // ===== 1. STUDIO / OVERLAY =====
        layout.addView(sectionTitle("🎬 1. STUDIO / OVERLAY"))
        channelInput = makeInput("Channel Name - e.g. Jehovah Shalom TV")
        tickerInput = makeInput("Ticker Text - e.g. Welcome to JSTV Live")
        verseInput = makeInput("Verse e.g. Psalm 122:1")
        layout.addView(channelInput); layout.addView(tickerInput); layout.addView(verseInput)

        cbLogo = makeCheck("Show Logo", true); layout.addView(cbLogo)
        cbVerse = makeCheck("Show Verse", true); layout.addView(cbVerse)
        cbTicker = makeCheck("Show Ticker", true); layout.addView(cbTicker)
        cbBible = makeCheck("Show Bible", true); layout.addView(cbBible)

        // ===== 2. CAMERA =====
        layout.addView(sectionTitle("📷 2. CAMERA"))
        spCamera = makeSpinner(layout, "Camera:", listOf("Back Camera", "Front Camera / Selfie", "External / USB Camera"))
        spResolution = makeSpinner(layout, "Camera Resolution:", listOf("720p HD - 1280x720", "1080p Full HD - 1920x1080", "480p - 854x480", "4K - 3840x2160 (Premium)", "Auto"))
        spFps = makeSpinner(layout, "Frame Rate (FPS):", listOf("30 FPS - Smooth", "60 FPS - Very Smooth", "24 FPS - Cinema", "15 FPS - Low Data"))
        spFocus = makeSpinner(layout, "Focus:", listOf("Auto Focus", "Fixed Focus", "Tap to Focus"))
        spFlash = makeSpinner(layout, "Flash / Torch:", listOf("Off", "On", "Auto"))
        cbStabilization = makeCheck("Video Stabilization - No Shaking", true); layout.addView(cbStabilization)
        cbMirror = makeCheck("Mirror Front Camera", false); layout.addView(cbMirror)

        // ===== 3. VIDEO QUALITY / ORIENTATION =====
        layout.addView(sectionTitle("🎞️ 3. VIDEO QUALITY & ORIENTATION"))
        spQuality = makeSpinner(layout, "Video Quality:", listOf("High - Best Quality", "Medium - Balanced", "Low - Save Data", "Auto - Adapts to Network"))
        spBitrate = makeSpinner(layout, "Bitrate:", listOf("2500 Kbps - HD", "4000 Kbps - Full HD", "1500 Kbps - Medium", "800 Kbps - Low Data", "Auto"))
        spKeyframe = makeSpinner(layout, "Keyframe Interval:", listOf("2 seconds (Recommended)", "1 second", "4 seconds", "5 seconds"))

        layout.addView(label("Orientation - Portrait / Landscape:"))
        rgOrientation = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        rbPortrait = RadioButton(this).apply { text = "Portrait - Vertical (Phone, TikTok, Reels)"; setTextColor(android.graphics.Color.WHITE); isChecked = true }
        rbLandscape = RadioButton(this).apply { text = "Landscape - Horizontal (YouTube, Facebook, TV)"; setTextColor(android.graphics.Color.WHITE) }
        rgOrientation.addView(rbPortrait); rgOrientation.addView(rbLandscape)
        layout.addView(rgOrientation)
        cbAutoRotate = makeCheck("Auto-Rotate with Phone", false); layout.addView(cbAutoRotate)

        // ===== 4. AUDIO =====
        layout.addView(sectionTitle("🎙️ 4. AUDIO"))
        spMic = makeSpinner(layout, "Microphone:", listOf("Phone Mic", "Bluetooth Mic / AirPods", "Wired Earphone Mic", "External Mic"))
        spAudioQuality = makeSpinner(layout, "Audio Quality:", listOf("128 Kbps - Clear", "96 Kbps - Good", "64 Kbps - Low Data", "192 Kbps - Studio"))
        tvVolume = label("Mic Volume: 80%")
        layout.addView(tvVolume)
        sbVolume = SeekBar(this).apply { max = 100; progress = 80 }
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvVolume.text = "Mic Volume: $p%" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        layout.addView(sbVolume)
        cbNoise = makeCheck("Noise Suppression - Remove Background Noise", true); layout.addView(cbNoise)
        cbEcho = makeCheck("Echo Cancellation", true); layout.addView(cbEcho)
        cbMusic = makeCheck("Background Music - Play Music in Live", false); layout.addView(cbMusic)

        // ===== 5. STREAM - PRISM STYLE =====
        layout.addView(sectionTitle("📡 5. STREAM SETTINGS - PRISM"))
        spEncoder = makeSpinner(layout, "Encoder:", listOf("Hardware Encoder - Fast", "Software Encoder - Compatible", "Auto"))
        spLatency = makeSpinner(layout, "Latency / Delay:", listOf("Low Latency - 2-3 sec", "Normal - 5-8 sec", "Ultra Low - <2 sec"))
        cbReconnect = makeCheck("Auto-Reconnect if Network Drops", true); layout.addView(cbReconnect)
        cbSaveReplay = makeCheck("Save Replay After Live", true); layout.addView(cbSaveReplay)
        cbShowChat = makeCheck("Show Live Chat on Screen", true); layout.addView(cbShowChat)
        cbShowViewers = makeCheck("Show Viewer Count", true); layout.addView(cbShowViewers)

        // ===== BUTTONS: SAVE / DISCARD / BACK =====
        val btnSave = makeButton("✅ SAVE → GO TO DESTINATION")
        btnSave.setBackgroundColor(android.graphics.Color.GREEN)
        btnSave.setOnClickListener { saveAndNext() }
        layout.addView(btnSave)

        val btnDiscard = makeButton("🗑️ DISCARD - Reset to Defaults")
        btnDiscard.setOnClickListener {
            discardAll()
            Toast.makeText(this, "Discarded - Back to Defaults", Toast.LENGTH_SHORT).show()
        }
        layout.addView(btnDiscard)

        val btnBack = makeButton("← BACK TO PLAN")
        btnBack.setOnClickListener {
            finish() // Back to PlanActivity
        }
        layout.addView(btnBack)
    }

    private fun sectionTitle(t: String): TextView {
        return TextView(this).apply {
            text = t; textSize = 22f; setTextColor(android.graphics.Color.YELLOW); setPadding(0, 32, 0, 12)
        }
    }
    private fun subTitle(t: String): TextView {
        return TextView(this).apply { text = t; textSize = 14f; setTextColor(android.graphics.Color.GREEN) }
    }
    private fun label(t: String): TextView {
        return TextView(this).apply { text = t; textSize = 16f; setTextColor(android.graphics.Color.WHITE); setPadding(0, 16, 0, 4) }
    }
    private fun makeInput(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint; setHintTextColor(android.graphics.Color.GRAY); setTextColor(android.graphics.Color.WHITE); textSize = 18f; setPadding(0, 20, 0, 20)
        }
    }
    private fun makeCheck(t: String, checked: Boolean): CheckBox {
        return CheckBox(this).apply { text = t; isChecked = checked; setTextColor(android.graphics.Color.WHITE) }
    }
    private fun makeSpinner(parent: LinearLayout, title: String, items: List<String>): Spinner {
        parent.addView(label(title))
        val sp = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, items)
        }
        parent.addView(sp)
        return sp
    }
    private fun makeButton(t: String): Button {
        return Button(this).apply {
            text = t; setBackgroundColor(android.graphics.Color.LTGRAY); setTextColor(android.graphics.Color.BLACK)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 20; layoutParams = p
        }
    }

    private fun discardAll() {
        channelInput.setText(""); tickerInput.setText(""); verseInput.setText("")
        cbLogo.isChecked = true; cbVerse.isChecked = true; cbTicker.isChecked = true; cbBible.isChecked = true
        spCamera.setSelection(0); spResolution.setSelection(1); spFps.setSelection(1); spFocus.setSelection(0); spFlash.setSelection(0)
        cbStabilization.isChecked = true; cbMirror.isChecked = false
        spQuality.setSelection(1); spBitrate.setSelection(1); spKeyframe.setSelection(1)
        rbPortrait.isChecked = true; cbAutoRotate.isChecked = false
        spMic.setSelection(0); spAudioQuality.setSelection(1); sbVolume.progress = 80; tvVolume.text = "Mic Volume: 80%"
        cbNoise.isChecked = true; cbEcho.isChecked = true; cbMusic.isChecked = false
        spEncoder.setSelection(0); spLatency.setSelection(1)
        cbReconnect.isChecked = true; cbSaveReplay.isChecked = true; cbShowChat.isChecked = true; cbShowViewers.isChecked = true
    }

    private fun saveAndNext() {
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE).edit()
        prefs.putString("channel", channelInput.text.toString())
        prefs.putString("ticker", tickerInput.text.toString())
        prefs.putString("verse", verseInput.text.toString())
        prefs.putBoolean("showLogo", cbLogo.isChecked)
        prefs.putBoolean("showVerse", cbVerse.isChecked)
        prefs.putBoolean("showTicker", cbTicker.isChecked)
        prefs.putBoolean("showBible", cbBible.isChecked)
        prefs.putString("camera", spCamera.selectedItem.toString())
        prefs.putString("resolution", spResolution.selectedItem.toString())
        prefs.putString("fps", spFps.selectedItem.toString())
        prefs.putString("focus", spFocus.selectedItem.toString())
        prefs.putString("flash", spFlash.selectedItem.toString())
        prefs.putBoolean("stabilization", cbStabilization.isChecked)
        prefs.putBoolean("mirror", cbMirror.isChecked)
        prefs.putString("quality", spQuality.selectedItem.toString())
        prefs.putString("bitrate", spBitrate.selectedItem.toString())
        prefs.putString("keyframe", spKeyframe.selectedItem.toString())
        prefs.putString("orientation", if (rbPortrait.isChecked) "PORTRAIT" else "LANDSCAPE")
        prefs.putBoolean("autoRotate", cbAutoRotate.isChecked)
        prefs.putString("mic", spMic.selectedItem.toString())
        prefs.putString("audioQuality", spAudioQuality.selectedItem.toString())
        prefs.putInt("volume", sbVolume.progress)
        prefs.putBoolean("noise", cbNoise.isChecked)
        prefs.putBoolean("echo", cbEcho.isChecked)
        prefs.putBoolean("music", cbMusic.isChecked)
        prefs.putString("encoder", spEncoder.selectedItem.toString())
        prefs.putString("latency", spLatency.selectedItem.toString())
        prefs.putBoolean("reconnect", cbReconnect.isChecked)
        prefs.putBoolean("saveReplay", cbSaveReplay.isChecked)
        prefs.putBoolean("showChat", cbShowChat.isChecked)
        prefs.putBoolean("showViewers", cbShowViewers.isChecked)
        prefs.apply()

        Toast.makeText(this, "Saved ✅ Going to Destination", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DestinationActivity::class.java))
        // Do NOT finish() - so BACK from Destination returns here
    }
}
