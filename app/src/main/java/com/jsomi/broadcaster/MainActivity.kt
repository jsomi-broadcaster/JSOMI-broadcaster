package com.jsomi.broadcaster

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isLive = false
    private var isMuted = false
    private var isIntroPlaying = false
    private var isLandscape = false
    private var seconds = 0
    private var autoTimerOn = false
    private var programIndex = -1
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var liveRunnable: Runnable

    // PROGRAM LIST - Time in seconds, Title, Ticker
    private val programs = listOf(
        Triple(0, "🎬 INTRO", "Welcome to JSTV - Intro Playing"),
        Triple(30, "🎵 WORSHIP", "Worship Time - Sing Along - God Bless You"),
        Triple(120, "📖 SERMON - Pastor John", "Sermon: Faith That Moves Mountains"),
        Triple(300, "💰 OFFERING", "Offering Time - Give With Joy - M-PESA 123456"),
        Triple(420, "🙏 CLOSING & PRAYER", "Thank You For Watching - See You Next Time")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val controlPanel = findViewById<LinearLayout>(R.id.controlPanel)
        val liveBadge = findViewById<LinearLayout>(R.id.liveBadge)
        val tvLiveTime = findViewById<TextView>(R.id.tvLiveTime)
        val btnEndLive = findViewById<Button>(R.id.btnEndLive)
        val btnGoLive = findViewById<Button>(R.id.btnGoLive)
        val btnMultiStream = findViewById<Button>(R.id.btnMultiStream)
        val btnIntro = findViewById<Button>(R.id.btnIntro)
        val btnLogo = findViewById<Button>(R.id.btnLogo)
        val btnRotate = findViewById<Button>(R.id.btnRotate)
        val btnMute = findViewById<Button>(R.id.btnMute)
        val btnAutoTimer = findViewById<Button>(R.id.btnAutoTimer)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvTicker = findViewById<TextView>(R.id.tvTicker)
        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        val tvProgram = findViewById<TextView>(R.id.tvProgram)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etTicker = findViewById<EditText>(R.id.etTicker)
        val btnSetTitle = findViewById<Button>(R.id.btnSetTitle)
        val btnSetTicker = findViewById<Button>(R.id.btnSetTicker)
        val videoIntro = findViewById<VideoView>(R.id.videoIntro)

        // Make Title, Logo, Ticker DRAGGABLE
        makeDraggable(tvTitle)
        makeDraggable(tvLogo)
        makeDraggable(tvTicker)

        // Start ticker scrolling
        tvTicker.isSelected = true

        // SET TITLE
        btnSetTitle.setOnClickListener {
            val t = etTitle.text.toString().trim()
            if (t.isNotEmpty()) {
                tvTitle.text = t
                tvTitle.visibility = View.VISIBLE
                Toast.makeText(this, "Title Set - Drag it anywhere", Toast.LENGTH_SHORT).show()
            }
        }

        // SET SCROLLING TEXT - Single Line
        btnSetTicker.setOnClickListener {
            val t = etTicker.text.toString().trim()
            if (t.isNotEmpty()) {
                tvTicker.text = "$t  •  $t  •  "
                tvTicker.isSelected = true
                tvTicker.visibility = View.VISIBLE
                Toast.makeText(this, "Scrolling Text Set - Drag it", Toast.LENGTH_SHORT).show()
            }
        }

        // INTRO VIDEO LOOP - No file needed
        btnIntro.setOnClickListener {
            if (!isIntroPlaying) {
                tvTitle.text = "🎬 JSTV INTRO PLAYING..."
                tvTitle.visibility = View.VISIBLE
                isIntroPlaying = true
                btnIntro.text = "■ STOP INTRO"
                Toast.makeText(this, "Intro Playing - Loop", Toast.LENGTH_SHORT).show()
            } else {
                videoIntro.stopPlayback()
                videoIntro.visibility = View.GONE
                isIntroPlaying = false
                btnIntro.text = "▶ INTRO"
            }
        }

        // LOGO SHOW / HIDE
        btnLogo.setOnClickListener {
            if (tvLogo.visibility == View.VISIBLE) {
                tvLogo.visibility = View.GONE
                btnLogo.text = "🖼 SHOW LOGO"
            } else {
                tvLogo.visibility = View.VISIBLE
                btnLogo.text = "🖼 LOGO"
                Toast.makeText(this, "Logo - Drag it to move", Toast.LENGTH_SHORT).show()
            }
        }

        // ROTATE - Portrait / Landscape
        btnRotate.setOnClickListener {
            if (!isLandscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                isLandscape = true
                Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show()
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                isLandscape = false
                Toast.makeText(this, "Portrait", Toast.LENGTH_SHORT).show()
            }
        }

        // MUTE / UNMUTE AUDIO
        btnMute.setOnClickListener {
            isMuted = !isMuted
            if (isMuted) {
                btnMute.text = "🔇"
                Toast.makeText(this, "Audio Muted", Toast.LENGTH_SHORT).show()
            } else {
                btnMute.text = "🎤"
                Toast.makeText(this, "Audio On", Toast.LENGTH_SHORT).show()
            }
        }

        // AUTO-TIMER FOR PROGRAM CHANGING
        btnAutoTimer.setOnClickListener {
            autoTimerOn = !autoTimerOn
            if (autoTimerOn) {
                programIndex = -1
                seconds = 0
                btnAutoTimer.text = "⏱ AUTO ON"
                Toast.makeText(this, "Auto-Timer ON - Programs will change automatically", Toast.LENGTH_LONG).show()
                tvProgram.visibility = View.VISIBLE
            } else {
                btnAutoTimer.text = "⏱ AUTO"
                Toast.makeText(this, "Auto-Timer OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // MULTI-STREAM - YouTube + Facebook + TikTok + Custom RTMP
        btnMultiStream.setOnClickListener {
            val platforms = arrayOf("YouTube Live", "Facebook Live", "TikTok Live", "Custom RTMP", "Church Website")
            val checked = booleanArrayOf(true, false, false, false, false)
            AlertDialog.Builder(this)
                .setTitle("📡 Multi-Stream To:")
                .setMultiChoiceItems(platforms, checked) { _, which, isChecked -> checked[which] = isChecked }
                .setPositiveButton("SAVE") { _, _ ->
                    val selected = platforms.filterIndexed { i, _ -> checked[i] }
                    Toast.makeText(this, "Streaming to: ${selected.joinToString(", ")}", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // GO LIVE -> FULLSCREEN - Only END button shows
        btnGoLive.setOnClickListener {
            isLive = true
            seconds = 0
            programIndex = -1
            controlPanel.visibility = View.GONE
            liveBadge.visibility = View.VISIBLE
            btnEndLive.visibility = View.VISIBLE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            liveRunnable = Runnable {
                seconds++
                val m = seconds / 60
                val s = seconds % 60
                tvLiveTime.text = String.format(" %02d:%02d", m, s)

                // AUTO CHANGE PROGRAM
                if (autoTimerOn) {
                    for (i in programs.indices) {
                        if (seconds >= programs[i].first && (i == programs.size - 1 || seconds < programs[i + 1].first)) {
                            if (programIndex != i) {
                                programIndex = i
                                tvTitle.text = programs[i].second
                                tvTicker.text = programs[i].third + "  •  "
                                tvTicker.isSelected = true
                                tvProgram.text = "PROGRAM: ${programs[i].second}"
                                Toast.makeText(this, "Now: ${programs[i].second}", Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                    }
                }
                handler.postDelayed(liveRunnable, 1000)
            }
            handler.post(liveRunnable)
            Toast.makeText(this, "🔴 YOU ARE LIVE!", Toast.LENGTH_LONG).show()
        }

        // END LIVE -> Back to Studio
        btnEndLive.setOnClickListener {
            isLive = false
            handler.removeCallbacks(liveRunnable)
            controlPanel.visibility = View.VISIBLE
            liveBadge.visibility = View.GONE
            btnEndLive.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            Toast.makeText(this, "Live Ended", Toast.LENGTH_LONG).show()
        }
    }

    // DRAG FUNCTION - Move any view with finger
    private fun makeDraggable(view: View) {
        var dX = 0f
        var dY = 0f
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }
            true
        }
    }
}
