package com.jsomi.broadcaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BroadcastActivity : AppCompatActivity() {

    private lateinit var topBar: LinearLayout
    private lateinit var previewBox: LinearLayout
    private lateinit var tvPreview: TextView
    private lateinit var tvLogo: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvTicker: TextView
    private lateinit var tvLoop: TextView
    private lateinit var liveBar: LinearLayout
    private lateinit var tvLive: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnSwitchVideo: Button
    private lateinit var studioLayout: LinearLayout
    private lateinit var etScroll: EditText
    private lateinit var etTitleInput: EditText
    private lateinit var etLogoInput: EditText
    private lateinit var etFileInput: EditText
    private lateinit var tvTitles: TextView
    private lateinit var tvFiles: TextView
    private lateinit var cbIntro: CheckBox
    private lateinit var cbLoop: CheckBox
    private lateinit var rbLive: RadioButton
    private lateinit var rbVideo: RadioButton

    private var titles = mutableListOf<String>()
    private var files = mutableListOf<String>()
    private var isLive = false
    private var isVideoLoop = false
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRun: Runnable? = null
    private var channels = "Facebook"
    private var plan = "FREE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        channels = prefs.getString("selected_channels", "Facebook") ?: "Facebook"
        plan = prefs.getString("plan", "FREE") ?: "FREE"

        val scroll = ScrollView(this)
        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.BLACK)
        }
        scroll.addView(main)
        setContentView(scroll)

        // PREVIEW
        previewBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.DKGRAY)
            setPadding(16, 16, 16, 16)
            minimumHeight = 500
            gravity = Gravity.CENTER
        }
        main.addView(previewBox)

        tvPreview = TextView(this).apply {
            text = "CAMERA PREVIEW\nREADY - " + channels + " - " + plan
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        previewBox.addView(tvPreview)

        tvLogo = TextView(this).apply {
            text = "JSTV LOGO"
            textSize = 16f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
        }
        previewBox.addView(tvLogo)

        tvTitle = TextView(this).apply {
            text = ""
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        previewBox.addView(tvTitle)

        tvLoop = TextView(this).apply {
            text = ""
            textSize = 16f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
            visibility = View.GONE
        }
        previewBox.addView(tvLoop)

        // TICKER - SINGLE LINE, NEVER WRAP
        tvTicker = TextView(this).apply {
            text = prefs.getString("ticker", "Welcome to Jehovah Shalom TV") ?: "Welcome"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            maxLines = 1
            setSingleLine(true)
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            isSingleLine = true
        }
        previewBox.addView(tvTicker)

        // LIVE BAR - Hidden until LIVE
        liveBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.RED)
        }
        main.addView(liveBar)

        tvLive = TextView(this).apply {
            text = "LIVE"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        liveBar.addView(tvLive)

        tvTimer = TextView(this).apply {
            text = "00:00"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        liveBar.addView(tvTimer)

        btnSwitchVideo = Button(this).apply {
            text = "SWITCH TO VIDEO - LOOP (Not Camera)"
            setBackgroundColor(Color.YELLOW)
            setTextColor(Color.BLACK)
            setOnClickListener { switchVideo() }
        }
        liveBar.addView(btnSwitchVideo)

        val btnEnd = Button(this).apply {
            text = "END LIVE"
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.RED)
            setOnClickListener { endLive() }
        }
        liveBar.addView(btnEnd)

        // ===== ONLY 3 BUTTONS =====
        topBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        main.addView(topBar)

        val btnBack = makeButton("BACK")
        btnBack.setOnClickListener {
            if (isLive) {
                Toast.makeText(this, "End Live first!", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
        topBar.addView(btnBack)

        val btnAddFile = makeButton("ADD FILE - STUDIO")
        btnAddFile.setOnClickListener {
            if (studioLayout.visibility == View.VISIBLE) {
                studioLayout.visibility = View.GONE
            } else {
                studioLayout.visibility = View.VISIBLE
            }
        }
        topBar.addView(btnAddFile)

        val btnGoLive = makeButton("GO LIVE")
        btnGoLive.setBackgroundColor(Color.RED)
        btnGoLive.setTextColor(Color.WHITE)
        btnGoLive.setOnClickListener { confirmGoLive() }
        topBar.addView(btnGoLive)

        // ===== STUDIO =====
        studioLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.DKGRAY)
            visibility = View.GONE
        }
        main.addView(studioLayout)

        studioLayout.addView(TextView(this).apply {
            text = "STUDIO FILE"
            textSize = 22f
            setTextColor(Color.YELLOW)
        })
        studioLayout.addView(TextView(this).apply {
            text = "Scrolling Text = SINGLE LINE, never wraps, scrolls no matter how long"
            textSize = 13f
            setTextColor(Color.LTGRAY)
        })

        etScroll = EditText(this).apply {
            hint = "Scrolling Text - Single Line - Write long text here"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            maxLines = 1
            setSingleLine(true)
        }
        studioLayout.addView(etScroll)

        etTitleInput = EditText(this).apply {
            hint = "Title - e.g. Sunday Service"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(etTitleInput)

        val btnAddTitle = makeButton("ADD TITLE - Unlimited")
        btnAddTitle.setOnClickListener {
            val t = etTitleInput.text.toString().trim()
            if (t.isEmpty()) {
                Toast.makeText(this, "Write Title first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            titles.add(t)
            tvTitles.text = "Titles (" + titles.size + "):\n" + titles.joinToString("\n")
            etTitleInput.setText("")
            Toast.makeText(this, "Title Added", Toast.LENGTH_SHORT).show()
        }
        studioLayout.addView(btnAddTitle)

        tvTitles = TextView(this).apply {
            text = "Titles (0):"
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(tvTitles)

        etLogoInput = EditText(this).apply {
            hint = "Logo Text - e.g. JSTV"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(etLogoInput)

        etFileInput = EditText(this).apply {
            hint = "File Name - e.g. intro.mp4"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(etFileInput)

        val btnAddFile2 = makeButton("ADD FILE")
        btnAddFile2.setOnClickListener {
            val f = etFileInput.text.toString().trim()
            if (f.isEmpty()) {
                Toast.makeText(this, "Write File Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            files.add(f)
            tvFiles.text = "Files (" + files.size + "):\n" + files.joinToString("\n")
            etFileInput.setText("")
        }
        studioLayout.addView(btnAddFile2)

        tvFiles = TextView(this).apply {
            text = "Files (0):"
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(tvFiles)

        cbIntro = CheckBox(this).apply {
            text = "Play Intro First"
            isChecked = true
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(cbIntro)

        cbLoop = CheckBox(this).apply {
            text = "Loop Files - Repeat"
            isChecked = true
            setTextColor(Color.WHITE)
        }
        studioLayout.addView(cbLoop)

        studioLayout.addView(TextView(this).apply {
            text = "Broadcast Mode:"
            setTextColor(Color.WHITE)
        })
        val rg = RadioGroup(this)
        rbLive = RadioButton(this).apply {
            text = "LIVE - From Camera"
            isChecked = true
            setTextColor(Color.WHITE)
        }
        rbVideo = RadioButton(this).apply {
            text = "VIDEO - From Files / Loop (Not Camera)"
            setTextColor(Color.WHITE)
        }
        rg.addView(rbLive)
        rg.addView(rbVideo)
        studioLayout.addView(rg)

        val btnAddToBroadcast = makeButton("ADD TO BROADCAST SCREEN")
        btnAddToBroadcast.setBackgroundColor(Color.GREEN)
        btnAddToBroadcast.setOnClickListener { addToBroadcast() }
        studioLayout.addView(btnAddToBroadcast)

        val btnClose = makeButton("CLOSE STUDIO")
        btnClose.setOnClickListener { studioLayout.visibility = View.GONE }
        studioLayout.addView(btnClose)
    }

    private fun addToBroadcast() {
        val s = etScroll.text.toString().trim()
        if (s.isNotEmpty()) {
            tvTicker.text = s
            tvTicker.maxLines = 1
            tvTicker.setSingleLine(true)
            tvTicker.ellipsize = TextUtils.TruncateAt.MARQUEE
            tvTicker.isSelected = true
        }
        if (etLogoInput.text.toString().trim().isNotEmpty()) {
            tvLogo.text = etLogoInput.text.toString().trim()
        }
        if (titles.isNotEmpty()) {
            tvTitle.text = titles.last()
        }
        if (rbVideo.isChecked) {
            isVideoLoop = true
            tvPreview.text = "VIDEO MODE - Files / Loop\n" + files.joinToString(", ")
            tvLoop.visibility = View.VISIBLE
            tvLoop.text = "LOOP: " + files.joinToString(" - ") + " | Intro: " + cbIntro.isChecked + " | Loop: " + cbLoop.isChecked
        } else {
            isVideoLoop = false
            tvPreview.text = "CAMERA PREVIEW - LIVE MODE"
            tvLoop.visibility = View.GONE
        }
        studioLayout.visibility = View.GONE
        Toast.makeText(this, "Added to Broadcast", Toast.LENGTH_SHORT).show()
    }

    private fun confirmGoLive() {
        if (isLive) {
            Toast.makeText(this, "Already LIVE", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Confirm Destination?")
            .setMessage("Go LIVE to:\n" + channels + "\nPlan: " + plan + "\nMode: " + if (isVideoLoop) "VIDEO - Loop" else "LIVE - Camera" + "\n\nContinue?")
            .setPositiveButton("YES - GO LIVE") { _, _ -> startCount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startCount() {
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
                if (i < 3) {
                    dialog.setMessage("Starting in " + counts[i] + "...")
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
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        topBar.visibility = View.GONE
        studioLayout.visibility = View.GONE
        liveBar.visibility = View.VISIBLE
        tvLive.text = "LIVE - " + channels
        if (isVideoLoop) {
            tvPreview.text = "LIVE - VIDEO LOOP\n" + files.joinToString(", ")
        } else {
            tvPreview.text = "LIVE - CAMERA"
        }
        Toast.makeText(this, "LIVE BEGUN!", Toast.LENGTH_LONG).show()
        seconds = 0
        timerRun = object : Runnable {
            override fun run() {
                seconds++
                val m = seconds / 60
                val s = seconds % 60
                tvTimer.text = String.format("%02d:%02d - %s", m, s, if (isVideoLoop) "VIDEO LOOP" else "CAMERA")
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRun!!)
    }

    private fun switchVideo() {
        if (!isLive) return
        if (!isVideoLoop) {
            isVideoLoop = true
            tvPreview.text = "LIVE - VIDEO LOOP\n" + files.joinToString(", ")
            tvLoop.visibility = View.VISIBLE
            tvLoop.text = "LOOP PLAYING - Not Camera"
            btnSwitchVideo.text = "SWITCH BACK TO CAMERA"
        } else {
            isVideoLoop = false
            tvPreview.text = "LIVE - CAMERA"
            tvLoop.visibility = View.GONE
            btnSwitchVideo.text = "SWITCH TO VIDEO - LOOP (Not Camera)"
        }
    }

    private fun endLive() {
        AlertDialog.Builder(this)
            .setTitle("End Live?")
            .setMessage("End this Live?")
            .setPositiveButton("END LIVE") { _, _ ->
                isLive = false
                timerRun?.let { handler.removeCallbacks(it) }
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                liveBar.visibility = View.GONE
                topBar.visibility = View.VISIBLE
                tvPreview.text = "CAMERA PREVIEW\nLIVE ENDED - " + channels
                Toast.makeText(this, "Live Ended", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Keep Live", null)
            .show()
    }

    private fun makeButton(t: String): Button {
        return Button(this).apply {
            text = t
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
            textSize = 18f
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 16
            layoutParams = p
        }
    }
}
