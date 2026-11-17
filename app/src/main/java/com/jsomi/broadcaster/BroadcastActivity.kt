package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BroadcastActivity : AppCompatActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var topBar: LinearLayout
    private lateinit var btnBack: Button
    private lateinit var btnAddFile: Button
    private lateinit var btnGoLive: Button

    // Broadcast preview area
    private lateinit var previewBox: LinearLayout
    private lateinit var tvPreviewLabel: TextView
    private lateinit var tvLogoOverlay: TextView
    private lateinit var tvTitleOverlay: TextView
    private lateinit var tvTickerOverlay: TextView
    private lateinit var tvLoopOverlay: TextView

    // Live controls - hidden until LIVE
    private lateinit var liveBar: LinearLayout
    private lateinit var tvLiveIndicator: TextView
    private lateinit var btnEnd: Button
    private lateinit var btnSwitchVideo: Button
    private lateinit var tvLiveTimer: TextView

    // Studio panel
    private lateinit var studioLayout: LinearLayout
    private lateinit var etScrollText: EditText
    private lateinit var etTitle: EditText
    private lateinit var etLogoText: EditText
    private lateinit var etFileName: EditText
    private lateinit var tvTitlesList: TextView
    private lateinit var tvFilesList: TextView
    private lateinit var cbIntro: CheckBox
    private lateinit var cbLoop: CheckBox
    private lateinit var rgMode: RadioGroup
    private lateinit var rbLiveMode: RadioButton
    private lateinit var rbVideoMode: RadioButton

    private var titles = mutableListOf<String>()
    private var files = mutableListOf<String>()
    private var isLive = false
    private var isVideoLoop = false
    private var secondsLive = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    private var channels = ""
    private var plan = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        channels = prefs.getString("selected_channels", "Facebook") ?: "Facebook"
        plan = prefs.getString("plan", "FREE") ?: "FREE"

        val scroll = ScrollView(this)
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.BLACK)
        }
        scroll.addView(mainLayout)
        setContentView(scroll)

        // ===== PREVIEW / CAMERA AREA =====
        previewBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.DKGRAY)
            setPadding(16, 16, 16, 16)
            minimumHeight = 600
            gravity = Gravity.CENTER
        }
        mainLayout.addView(previewBox)

        tvPreviewLabel = TextView(this).apply {
            text = "📷 CAMERA PREVIEW\nREADY - $channels - $plan"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        previewBox.addView(tvPreviewLabel)

        tvLogoOverlay = TextView(this).apply {
            text = "JSTV LOGO"; textSize = 16f; setTextColor(Color.YELLOW); gravity = Gravity.CENTER
        }
        previewBox.addView(tvLogoOverlay)

        tvTitleOverlay = TextView(this).apply {
            text = ""; textSize = 22f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        }
        previewBox.addView(tvTitleOverlay)

        tvLoopOverlay = TextView(this).apply {
            text = ""; textSize = 16f; setTextColor(Color.GREEN); gravity = Gravity.CENTER; visibility = View.GONE
        }
        previewBox.addView(tvLoopOverlay)

        // TICKER - SINGLE LINE, NEVER WRAP, SCROLLS
        tvTickerOverlay = TextView(this).apply {
            text = prefs.getString("ticker", "Welcome to Jehovah Shalom TV - God Bless You") ?: ""
            textSize = 18f; setTextColor(Color.WHITE); setBackgroundColor(Color.BLACK)
            maxLines = 1
            setSingleLine(true)
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true // needed for marquee to scroll
            isSingleLine = true
        }
        previewBox.addView(tvTickerOverlay)

        // ===== LIVE BAR - Hidden until LIVE =====
        liveBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.RED)
        }
        mainLayout.addView(liveBar)

        tvLiveIndicator = TextView(this).apply {
            text = "🔴 LIVE"; textSize = 24f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        }
        liveBar.addView(tvLiveIndicator)

        tvLiveTimer = TextView(this).apply {
            text = "00:00 - $channels"; textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        }
        liveBar.addView(tvLiveTimer)

        btnSwitchVideo = Button(this).apply {
            text = "SWITCH TO VIDEO - LOOP (Not Camera)"
            setBackgroundColor(Color.YELLOW); setTextColor(Color.BLACK)
            setOnClickListener { switchToVideoLoop() }
        }
        liveBar.addView(btnSwitchVideo)

        btnEnd = Button(this).apply {
            text = "⏹ END LIVE"
            setBackgroundColor(Color.WHITE); setTextColor(Color.RED)
            setOnClickListener { endLive() }
        }
        liveBar.addView(btnEnd)

        // ===== 3 BUTTONS ONLY =====
        topBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 0)
        }
        mainLayout.addView(topBar)

        btnBack = makeButton("← BACK")
        btnBack.setOnClickListener {
            if (isLive) {
                Toast.makeText(this, "End Live first!", Toast.LENGTH_SHORT).show()
            } else {
                finish() // Back to Destination
            }
        }
        topBar.addView(btnBack)

        btnAddFile = makeButton("📁 ADD FILE - STUDIO")
        btnAddFile.setOnClickListener { toggleStudio() }
        topBar.addView(btnAddFile)

        btnGoLive = makeButton("🔴 GO LIVE")
        btnGoLive.setBackgroundColor(Color.RED)
        btnGoLive.setTextColor(Color.WHITE)
        btnGoLive.setOnClickListener { confirmAndGoLive() }
        topBar.addView(btnGoLive)

        // ===== STUDIO PANEL - Hidden at first =====
        studioLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.DKGRAY)
            visibility = View.GONE
        }
        mainLayout.addView(studioLayout)
        buildStudio()
    }

    private fun buildStudio() {
        studioLayout.addView(TextView(this).apply {
            text = "🎬 STUDIO FILE - Add to Broadcast"; textSize = 22f; setTextColor(Color.YELLOW)
        })
        studioLayout.addView(TextView(this).apply {
            text = "Scrolling Text is SINGLE LINE - never wraps, scrolls no matter how long"; textSize = 13f; setTextColor(Color.LTGRAY)
        })

        etScrollText = EditText(this).apply {
            hint = "Scrolling Text - Single Line - e.g. Welcome to JSTV Live - Psalm 122:1 I was glad... (can be very long)"
            setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
            maxLines = 1; setSingleLine(true)
        }
        studioLayout.addView(etScrollText)

        etTitle = EditText(this).apply {
            hint = "Title - e.g. Sunday Service Live"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
        }
        studioLayout.addView(etTitle)

        val btnAddTitle = makeButton("➕ ADD TITLE (Unlimited)")
        btnAddTitle.setOnClickListener {
            val t = etTitle.text.toString().trim()
            if (t.isEmpty()) { Toast.makeText(this, "Write Title first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            titles.add(t)
            tvTitlesList.text = "Titles (${titles.size}):\n" + titles.joinToString("\n• ", prefix = "• ")
            etTitle.setText("")
            Toast.makeText(this, "Title Added: $t", Toast.LENGTH_SHORT).show()
        }
        studioLayout.addView(btnAddTitle)

        tvTitlesList = TextView(this).apply { text = "Titles (0):"; setTextColor(Color.WHITE); textSize = 14f }
        studioLayout.addView(tvTitlesList)

        etLogoText = EditText(this).apply {
            hint = "Logo Text - e.g. JSTV"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
        }
        studioLayout.addView(etLogoText)

        etFileName = EditText(this).apply {
            hint = "File Name - e.g. intro.mp4 / worship.mp3 / verse.png"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
        }
        studioLayout.addView(etFileName)

        val btnAddFileItem = makeButton("➕ ADD FILE")
        btnAddFileItem.setOnClickListener {
            val f = etFileName.text.toString().trim()
            if (f.isEmpty()) { Toast.makeText(this, "Write File Name first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            files.add(f)
            tvFilesList.text = "Files (${files.size}):\n" + files.joinToString("\n• ", prefix = "• ")
            etFileName.setText("")
            Toast.makeText(this, "File Added: $f", Toast.LENGTH_SHORT).show()
        }
        studioLayout.addView(btnAddFileItem)

        tvFilesList = TextView(this).apply { text = "Files (0):"; setTextColor(Color.WHITE); textSize = 14f }
        studioLayout.addView(tvFilesList)

        cbIntro = CheckBox(this).apply { text = "Play Intro First"; isChecked = true; setTextColor(Color.WHITE) }
        studioLayout.addView(cbIntro)
        cbLoop = CheckBox(this).apply { text = "Loop Files - Repeat Again & Again"; isChecked = true; setTextColor(Color.WHITE) }
        studioLayout.addView(cbLoop)

        studioLayout.addView(TextView(this).apply { text = "Broadcast Mode:"; setTextColor(Color.WHITE); textSize = 16f })
        rgMode = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        rbLiveMode = RadioButton(this).apply { text = "LIVE - From Camera"; isChecked = true; setTextColor(Color.WHITE) }
        rbVideoMode = RadioButton(this).apply { text = "VIDEO - From Files / Loop (Not Camera)"; setTextColor(Color.WHITE) }
        rgMode.addView(rbLiveMode); rgMode.addView(rbVideoMode)
        studioLayout.addView(rgMode)

        val btnAddToBroadcast = makeButton("✅ ADD TO BROADCAST SCREEN")
        btnAddToBroadcast.setBackgroundColor(Color.GREEN)
        btnAddToBroadcast.setOnClickListener { addToBroadcast() }
        studioLayout.addView(btnAddToBroadcast)

        val btnCloseStudio = makeButton("CLOSE STUDIO ▲")
        btnCloseStudio.setOnClickListener { studioLayout.visibility = View.GONE }
        studioLayout.addView(btnCloseStudio)
    }

    private fun toggleStudio() {
        studioLayout.visibility = if (studioLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun addToBroadcast() {
        // Single line scrolling text - never twin/wrap
        val scrollTxt = etScrollText.text.toString().trim()
        if (scrollTxt.isNotEmpty()) {
            tvTickerOverlay.text = scrollTxt
            tvTickerOverlay.maxLines = 1
            tvTickerOverlay.setSingleLine(true)
            tvTickerOverlay.ellipsize = TextUtils.TruncateAt.MARQUEE
            tvTickerOverlay.isSelected = true
        }
        if (etLogoText.text.toString().trim().isNotEmpty()) {
            tvLogoOverlay.text = etLogoText.text.toString().trim()
        }
        if (titles.isNotEmpty()) {
            tvTitleOverlay.text = titles.last()
        }
        if (rbVideoMode.isChecked) {
            isVideoLoop = true
            tvPreviewLabel.text = "🎬 VIDEO MODE - Playing Files / Loop\nFiles: ${files.joinToString(", ")}"
            tvLoopOverlay.visibility = View.VISIBLE
            tvLoopOverlay.text = "🔁 LOOP: ${if (files.isEmpty()) "No file - Add File" else files.joinToString(" → ")} | Intro: ${if (cbIntro.isChecked) "ON" else "OFF"} | Loop: ${if (cbLoop.isChecked) "ON" else "OFF"}"
        } else {
            isVideoLoop = false
            tvPreviewLabel.text = "📷 CAMERA PREVIEW - LIVE MODE"
            tvLoopOverlay.visibility = View.GONE
        }
        studioLayout.visibility = View.GONE
        Toast.makeText(this, "Added to Broadcast ✅", Toast.LENGTH_SHORT).show()
    }

    private fun confirmAndGoLive() {
        if (isLive) { Toast.makeText(this, "Already LIVE", Toast.LENGTH_SHORT).show(); return }
        AlertDialog.Builder(this)
            .setTitle("Confirm Destination?")
            .setMessage("Go LIVE to:\n$channels\nPlan: $plan\n\nMode: ${if (isVideoLoop) "VIDEO - Loop Files" else "LIVE - Camera"}\n\nTitles: ${titles.size} | Files: ${files.size}\n\nContinue?")
            .setPositiveButton("YES - GO LIVE") { _, _ -> startCountdown() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startCountdown() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Get Ready!")
            .setMessage("Starting in 3...")
            .setCancelable(false)
            .create()
        dialog.show()
        val counts = listOf("3", "2", "1")
        var i = 0
        val h = Handler(Looper.getMainLooper())
        val r = object : Runnable {
            override fun run() {
                if (i < counts.size) {
                    dialog.setMessage("Starting in ${counts[i]}...")
                    Toast.makeText(this@BroadcastActivity, counts[i], Toast.LENGTH_SHORT).show()
                    i++
                    h.postDelayed(this, 1000)
                } else {
                    dialog.dismiss()
                    beginLive()
                }
            }
        }
        h.postDelayed(r, 1000)
    }

    private fun beginLive() {
        isLive = true
        // FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Hide top 3 buttons, show Live Bar
        topBar.visibility = View.GONE
        studioLayout.visibility = View.GONE
        liveBar.visibility = View.VISIBLE
        tvLiveIndicator.text = "🔴 LIVE - $channels"
        tvPreviewLabel.text = if (isVideoLoop) "🎬 LIVE - VIDEO LOOP PLAYING\n${files.joinToString(", ")}" else "🔴 LIVE - CAMERA"
        Toast.makeText(this, "🔴 LIVE BEGUN!", Toast.LENGTH_LONG).show()
        secondsLive = 0
        timerRunnable = object : Runnable {
            override fun run() {
                secondsLive++
                val m = secondsLive / 60
                val s = secondsLive % 60
                tvLiveTimer.text = String.format("%02d:%02d - %s", m, s, if (isVideoLoop) "VIDEO LOOP" else "CAMERA")
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun switchToVideoLoop() {
        if (!isLive) return
        if (!isVideoLoop) {
            isVideoLoop = true
            tvPreviewLabel.text = "🎬 LIVE - SWITCHED TO VIDEO - LOOP\nFiles: ${if (files.isEmpty()) "No file - Add in Studio before Live" else files.joinToString(", ")}"
            tvLoopOverlay.visibility = View.VISIBLE
            tvLoopOverlay.text = "🔁 LOOP PLAYING - Not Camera"
            btnSwitchVideo.text = "SWITCH BACK TO CAMERA"
            Toast.makeText(this, "Switched to Video Loop - Camera OFF", Toast.LENGTH_SHORT).show()
        } else {
            isVideoLoop = false
            tvPreviewLabel.text = "🔴 LIVE - CAMERA"
            tvLoopOverlay.visibility = View.GONE
            btnSwitchVideo.text = "SWITCH TO VIDEO - LOOP (Not Camera)"
            Toast.makeText(this, "Back to Camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endLive() {
        AlertDialog.Builder(this)
            .setTitle("End Live?")
            .setMessage("Are you sure you want to END this Live?")
            .setPositiveButton("END LIVE") { _, _ ->
                isLive = false
                timerRunnable?.let { handler.removeCallbacks(it) }
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                liveBar.visibility = View.GONE
                topBar.visibility = View.VISIBLE
                tvPreviewLabel.text = "📷 CAMERA PREVIEW\nLIVE ENDED - $channels"
                Toast.makeText(this, "Live Ended", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Keep Live", null)
            .show()
    }

    private fun makeButton(t: String): Button {
        return Button(this).apply {
            text = t; setBackgroundColor(Color.LTGRAY); setTextColor(Color.BLACK); textSize = 18f
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 16; layoutParams = p
        }
    }
}
