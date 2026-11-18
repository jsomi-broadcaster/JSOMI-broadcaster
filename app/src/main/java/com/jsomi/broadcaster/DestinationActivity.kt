package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DestinationActivity : AppCompatActivity() {

    private val FB_RTMP = "rtmps://live-api-s.facebook.com:443/rtmp/"
    private val YT_RTMP = "rtmp://a.youtube.com/live2/"
    private val TT_RTMP = "rtmp://rtmp-push.tiktok.com/live/"

    private lateinit var tvPlanInfo: TextView
    private lateinit var tvSelected: TextView
    private lateinit var tvError: TextView

    private lateinit var fbCard: LinearLayout
    private lateinit var fbKeyInput: EditText
    private lateinit var ytCard: LinearLayout
    private lateinit var ytKeyInput: EditText
    private lateinit var ttCard: LinearLayout
    private lateinit var ttKeyInput: EditText

    private var selected = mutableSetOf<String>()
    private var plan = "FREE"
    private var maxChannels = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        plan = prefs.getString("plan", "FREE") ?: "FREE"
        if (plan.contains("PREMIUM")) {
            maxChannels = 3
        } else {
            maxChannels = 1
        }

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 48)
            setBackgroundColor(Color.BLACK)
        }
        scroll.addView(layout)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "Where to Broadcast?"
            textSize = 30f
            setTextColor(Color.WHITE)
        }
        layout.addView(title)

        tvPlanInfo = TextView(this).apply {
            text = "Plan: " + plan + "\nFREE = 1 Channel Only\nPREMIUM (FREE NOW) = Up to 3 Channels"
            textSize = 16f
            setTextColor(Color.GREEN)
        }
        layout.addView(tvPlanInfo)

        tvSelected = TextView(this).apply {
            text = "Selected: 0 / " + maxChannels
            textSize = 16f
            setTextColor(Color.YELLOW)
        }
        layout.addView(tvSelected)

        tvError = TextView(this).apply {
            text = ""
            textSize = 16f
            setTextColor(Color.RED)
        }
        layout.addView(tvError)

        // FACEBOOK
        val btnFb = makeButton("FACEBOOK LIVE - f")
        btnFb.setBackgroundColor(Color.parseColor("#1877F2"))
        btnFb.setTextColor(Color.WHITE)
        btnFb.setOnClickListener { showOnly(fbCard) }
        layout.addView(btnFb)

        fbCard = makeCard()
        fbCard.addView(makeRtmpText("Facebook RTMP:\n" + FB_RTMP))
        fbKeyInput = makeKeyInput("Paste FACEBOOK STREAM KEY here")
        fbCard.addView(fbKeyInput)
        fbCard.addView(makeHelp("Get Key: Facebook App - Live - Go Live - Stream Key - Copy"))
        fbCard.addView(makeAddButton("FACEBOOK", fbKeyInput))
        layout.addView(fbCard)

        // YOUTUBE
        val btnYt = makeButton("YOUTUBE LIVE - Play")
        btnYt.setBackgroundColor(Color.parseColor("#FF0000"))
        btnYt.setTextColor(Color.WHITE)
        btnYt.setOnClickListener { showOnly(ytCard) }
        layout.addView(btnYt)

        ytCard = makeCard()
        ytCard.addView(makeRtmpText("YouTube RTMP:\n" + YT_RTMP))
        ytKeyInput = makeKeyInput("Paste YOUTUBE STREAM KEY here")
        ytCard.addView(ytKeyInput)
        ytCard.addView(makeHelp("Get Key: YouTube Studio - Go Live - Stream - Stream Key - Copy"))
        ytCard.addView(makeAddButton("YOUTUBE", ytKeyInput))
        layout.addView(ytCard)

        // TIKTOK
        val btnTt = makeButton("TIKTOK LIVE - Music")
        btnTt.setBackgroundColor(Color.WHITE)
        btnTt.setTextColor(Color.BLACK)
        btnTt.setOnClickListener { showOnly(ttCard) }
        layout.addView(btnTt)

        ttCard = makeCard()
        ttCard.addView(makeRtmpText("TikTok RTMP:\n" + TT_RTMP))
        ttKeyInput = makeKeyInput("Paste TIKTOK STREAM KEY here")
        ttCard.addView(ttKeyInput)
        ttCard.addView(makeHelp("Get Key: TikTok Live Center - Start Live - Get Stream Key"))
        ttCard.addView(makeAddButton("TIKTOK", ttKeyInput))
        layout.addView(ttCard)

        // GO LIVE
        val btnGo = makeButton("GO LIVE - BROADCASTING")
        btnGo.setBackgroundColor(Color.GREEN)
        btnGo.setTextColor(Color.BLACK)
        btnGo.setOnClickListener { validateAndGo() }
        layout.addView(btnGo)

        val btnBack = makeButton("BACK TO SETTINGS")
        btnBack.setOnClickListener { finish() }
        layout.addView(btnBack)

        val note = TextView(this).apply {
            text = "\nCORRECT Key = 10+ chars, no spaces - Goes to Broadcasting\nWRONG Key = Empty, short, has space - REJECTED\nTEST KEY: FB1234567890ABC"
            textSize = 13f
            setTextColor(Color.GRAY)
        }
        layout.addView(note)
    }

    private fun showOnly(card: LinearLayout) {
        fbCard.visibility = View.GONE
        ytCard.visibility = View.GONE
        ttCard.visibility = View.GONE
        card.visibility = View.VISIBLE
        tvError.text = ""
    }

    private fun makeButton(t: String): Button {
        return Button(this).apply {
            text = t
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
            textSize = 18f
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 20
            layoutParams = p
        }
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.DKGRAY)
            visibility = View.GONE
        }
    }

    private fun makeRtmpText(t: String): TextView {
        return TextView(this).apply {
            text = t
            textSize = 14f
            setTextColor(Color.YELLOW)
        }
    }

    private fun makeKeyInput(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
        }
    }

    private fun makeHelp(t: String): TextView {
        return TextView(this).apply {
            text = t
            textSize = 13f
            setTextColor(Color.LTGRAY)
        }
    }

    private fun makeAddButton(channel: String, input: EditText): Button {
        val btn = Button(this).apply {
            text = "ADD " + channel
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
        }
        btn.setOnClickListener {
            if (selected.contains(channel)) {
                selected.remove(channel)
                btn.text = "ADD " + channel
                Toast.makeText(this, channel + " Removed", Toast.LENGTH_SHORT).show()
            } else {
                if (selected.size >= maxChannels) {
                    tvError.text = "Only " + maxChannels + " channel(s) for " + plan
                    Toast.makeText(this, "Limit: " + maxChannels, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!isKeyValid(input.text.toString())) {
                    tvError.text = channel + " Key WRONG - Must be 10+ chars, no spaces."
                    Toast.makeText(this, "Wrong " + channel + " Key - Rejected", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                selected.add(channel)
                btn.text = channel + " ADDED - Tap to Remove"
                tvError.text = ""
                Toast.makeText(this, channel + " Added", Toast.LENGTH_SHORT).show()
            }
            tvSelected.text = "Selected: " + selected.size + " / " + maxChannels + " - " + selected.joinToString(", ")
        }
        return btn
    }

    private fun isKeyValid(key: String): Boolean {
        val k = key.trim()
        if (k.isEmpty()) return false
        if (k.length < 10) return false
        if (k.contains(" ")) return false
        if (k == "123" || k == "wrong" || k == "test") return false
        return true
    }

    private fun validateAndGo() {
        if (selected.isEmpty()) {
            tvError.text = "Select at least 1 channel first"
            Toast.makeText(this, "Select 1 channel first", Toast.LENGTH_SHORT).show()
            return
        }
        for (ch in selected) {
            val key = when (ch) {
                "FACEBOOK" -> fbKeyInput.text.toString()
                "YOUTUBE" -> ytKeyInput.text.toString()
                else -> ttKeyInput.text.toString()
            }
            if (!isKeyValid(key)) {
                tvError.text = ch + " Key is WRONG - Rejected"
                Toast.makeText(this, ch + " Key Wrong - Rejected", Toast.LENGTH_LONG).show()
                return
            }
        }
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE).edit()
        prefs.putString("selected_channels", selected.joinToString(","))
        if (selected.contains("FACEBOOK")) {
            prefs.putString("fb_full_url", FB_RTMP + fbKeyInput.text.toString().trim())
        }
        if (selected.contains("YOUTUBE")) {
            prefs.putString("yt_full_url", YT_RTMP + ytKeyInput.text.toString().trim())
        }
        if (selected.contains("TIKTOK")) {
            prefs.putString("tt_full_url", TT_RTMP + ttKeyInput.text.toString().trim())
        }
        prefs.apply()
        Toast.makeText(this, "Keys Correct - Going to Broadcasting", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, RealStreamActivity::class.java))
    }
}
