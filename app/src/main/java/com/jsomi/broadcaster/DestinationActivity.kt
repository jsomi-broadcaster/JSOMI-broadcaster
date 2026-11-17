package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DestinationActivity : AppCompatActivity() {

    // RTMP - REAL
    private val FB_RTMP = "rtmps://live-api-s.facebook.com:443/rtmp/"
    private val YT_RTMP = "rtmp://a.youtube.com/live2/"
    private val TT_RTMP = "rtmp://rtmp-push.tiktok.com/live/"

    private lateinit var tvPlanInfo: TextView
    private lateinit var tvSelected: TextView
    private lateinit var tvError: TextView

    // Facebook card
    private lateinit var fbCard: LinearLayout
    private lateinit var fbKeyInput: EditText
    private lateinit var fbRtmpText: TextView
    // YouTube card
    private lateinit var ytCard: LinearLayout
    private lateinit var ytKeyInput: EditText
    private lateinit var ytRtmpText: TextView
    // TikTok card
    private lateinit var ttCard: LinearLayout
    private lateinit var ttKeyInput: EditText
    private lateinit var ttRtmpText: TextView

    private var selected = mutableSetOf<String>()
    private var plan = "FREE"
    private var maxChannels = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 48)
            setBackgroundColor(Color.BLACK)
        }
        scroll.addView(layout)
        setContentView(scroll)

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        plan = prefs.getString("plan", "FREE") ?: "FREE"
        // Premium is FREE for now - so allow 3
        maxChannels = if (plan.contains("PREMIUM")) 3 else 1

        val title = TextView(this).apply {
            text = "Where to Broadcast?"
            textSize = 30f
            setTextColor(Color.WHITE)
        }
        layout.addView(title)

        tvPlanInfo = TextView(this).apply {
            text = "Plan: $plan\nFREE = 1 Channel Only\nPREMIUM (FREE NOW) = Up to 3 Channels"
            textSize = 16f
            setTextColor(Color.GREEN)
            setPadding(0, 8, 0, 8)
        }
        layout.addView(tvPlanInfo)

        tvSelected = TextView(this).apply {
            text = "Selected: 0 / $maxChannels"
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

        val hint = TextView(this).apply {
            text = "Tap an icon below to open its Stream Key request:"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }
        layout.addView(hint)

        // ===== FACEBOOK ICON BUTTON =====
        val btnFb = makeIconButton("ⓕ  FACEBOOK LIVE", Color.parseColor("#1877F2"))
        btnFb.setOnClickListener { toggleCard("FACEBOOK") }
        layout.addView(btnFb)

        fbCard = makeCard()
        fbRtmpText = makeRtmpText("Facebook RTMP:\n$FB_RTMP")
        fbKeyInput = makeKeyInput("Paste FACEBOOK STREAM KEY here")
        fbCard.addView(fbRtmpText)
        fbCard.addView(fbKeyInput)
        fbCard.addView(makeHelpText("Get Key: Facebook App → Live → Go Live → Stream Key → Copy"))
        fbCard.addView(makeToggleButton("FACEBOOK", fbCard, fbKeyInput))
        layout.addView(fbCard)

        // ===== YOUTUBE ICON BUTTON =====
        val btnYt = makeIconButton("▶  YOUTUBE LIVE", Color.parseColor("#FF0000"))
        btnYt.setOnClickListener { toggleCard("YOUTUBE") }
        layout.addView(btnYt)

        ytCard = makeCard()
        ytRtmpText = makeRtmpText("YouTube RTMP:\n$YT_RTMP")
        ytKeyInput = makeKeyInput("Paste YOUTUBE STREAM KEY here")
        ytCard.addView(ytRtmpText)
        ytCard.addView(ytKeyInput)
        ytCard.addView(makeHelpText("Get Key: YouTube Studio → Go Live → Stream → Stream Key → Copy"))
        ytCard.addView(makeToggleButton("YOUTUBE", ytCard, ytKeyInput))
        layout.addView(ytCard)

        // ===== TIKTOK ICON BUTTON =====
        val btnTt = makeIconButton("♪  TIKTOK LIVE", Color.WHITE)
        btnTt.setTextColor(Color.BLACK)
        btnTt.setOnClickListener { toggleCard("TIKTOK") }
        layout.addView(btnTt)

        ttCard = makeCard()
        ttRtmpText = makeRtmpText("TikTok RTMP:\n$TT_RTMP")
        ttKeyInput = makeKeyInput("Paste TIKTOK STREAM KEY here")
        ttCard.addView(ttRtmpText)
        ttCard.addView(ttKeyInput)
        ttCard.addView(makeHelpText("Get Key: TikTok Live Center → Start Live → Get Stream Key (Need 1000+ Followers)"))
        ttCard.addView(makeToggleButton("TIKTOK", ttCard, ttKeyInput))
        layout.addView(ttCard)

        // ===== GO LIVE BUTTON =====
        val btnGo = makeIconButton("🚀 GO LIVE → BROADCASTING SCREEN", Color.GREEN)
        btnGo.setTextColor(Color.BLACK)
        btnGo.setOnClickListener { validateAndGo() }
        layout.addView(btnGo)

        val btnBack = makeIconButton("← BACK TO SETTINGS", Color.LTGRAY)
        btnBack.setTextColor(Color.BLACK)
        btnBack.setOnClickListener { finish() }
        layout.addView(btnBack)

        val note = TextView(this).apply {
            text = "\nHow Key Check works:\n✅ CORRECT = 10+ chars, no spaces, letters+numbers → Goes to Broadcasting\n❌ WRONG = Empty, <10 chars, has space → REJECTED, stays here\n\nTEST KEY: Use any long key like FB1234567890ABC to test"
            textSize = 13f
            setTextColor(Color.GRAY)
        }
        layout.addView(note)
    }

    private fun makeIconButton(t: String, bg: Int): Button {
        return Button(this).apply {
            text = t; setBackgroundColor(bg); setTextColor(Color.WHITE)
            textSize = 18f
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 20; layoutParams = p
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
            text = t; textSize = 14f; setTextColor(Color.YELLOW); setPadding(0, 8, 0, 8)
        }
    }

    private fun makeKeyInput(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); textSize = 16f
        }
    }

    private fun makeHelpText(t: String): TextView {
        return TextView(this).apply { text = t; textSize = 13f; setTextColor(Color.LTGRAY) }
    }

    private fun makeToggleButton(channel: String, card: LinearLayout, input: EditText): Button {
        val btn = Button(this).apply {
            text = "ADD $channel ✓"
            setBackgroundColor(Color.LTGRAY); setTextColor(Color.BLACK)
        }
        btn.setOnClickListener {
            if (selected.contains(channel)) {
                selected.remove(channel)
                btn.text = "ADD $channel ✓"
                Toast.makeText(this, "$channel Removed", Toast.LENGTH_SHORT).show()
            } else {
                if (selected.size >= maxChannels) {
                    tvError.text = "❌ $plan Mode allows only $maxChannels channel(s). Upgrade to Premium (FREE NOW) for 3 channels."
                    Toast.makeText(this, "Limit: $maxChannels channel(s) for $plan", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                // Check key format before adding
                if (!isKeyValid(input.text.toString())) {
                    tvError.text = "❌ $channel Key WRONG - Must be 10+ chars, no spaces. Paste correct Stream Key."
                    Toast.makeText(this, "Wrong $channel Key - Rejected", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                selected.add(channel)
                btn.text = "✓ $channel ADDED - Tap to Remove"
                tvError.text = ""
                Toast.makeText(this, "$channel Added ✅", Toast.LENGTH_SHORT).show()
            }
            tvSelected.text = "Selected: ${selected.size} / $maxChannels - ${selected.joinToString(", ")}"
        }
        return btn
    }

    private fun toggleCard(channel: String) {
        // Hide all, show tapped one
        fbCard.visibility = View.GONE
        ytCard.visibility = View.GONE
        ttCard.visibility = View.GONE
        when (channel) {
            "FACEBOOK" -> fbCard.visibility = View.VISIBLE
            "YOUTUBE" -> ytCard.visibility = View.VISIBLE
            "TIKTOK" -> ttCard.visibility = View.VISIBLE
        }
        tvError.text = ""
        Toast.makeText(this, "$channel - Paste your Stream Key", Toast.LENGTH_SHORT).show()
    }

    // ===== KEY VALIDATION =====
    private fun isKeyValid(key: String): Boolean {
        val k = key.trim()
        if (k.isEmpty()) return false
        if (k.length < 10) return false
        if (k.contains(" ")) return false
        // Reject obvious test wrong keys
        if (k.equals("123", ignoreCase = true) || k.equals("wrong", ignoreCase = true) || k.equals("test", ignoreCase = true)) return false
        // Must be letters / numbers / - _ . : - real keys look like this
        if (!k.matches(Regex("^[A-Za-z0-9\\-_\\.:]+$"))) return false
        return true
    }

    private fun validateAndGo() {
        if (selected.isEmpty()) {
            tvError.text = "❌ Select at least 1 channel first - Tap Facebook / YouTube / TikTok icon, paste Key, tap ADD"
            Toast.makeText(this, "Select 1 channel first", Toast.LENGTH_SHORT).show()
            return
        }
        // Re-validate all selected keys - if any wrong, reject
        for (ch in selected) {
            val key = when (ch) {
                "FACEBOOK" -> fbKeyInput.text.toString()
                "YOUTUBE" -> ytKeyInput.text.toString()
                else -> ttKeyInput.text.toString()
            }
            if (!isKeyValid(key)) {
                tvError.text = "❌ $ch Key is WRONG - Rejected. Paste correct Stream Key (10+ chars, no space)."
                Toast.makeText(this, "$ch Key Wrong - Rejected", Toast.LENGTH_LONG).show()
                return
            }
        }

        // All correct - Build full RTMP URLs and save
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE).edit()
        prefs.putString("selected_channels", selected.joinToString(","))
        if (selected.contains("FACEBOOK")) prefs.putString("fb_full_url", FB_RTMP + fbKeyInput.text.toString().trim())
        if (selected.contains("YOUTUBE")) prefs.putString("yt_full_url", YT_RTMP + ytKeyInput.text.toString().trim())
        if (selected.contains("TIKTOK")) prefs.putString("tt_full_url", TT_RTMP + ttKeyInput.text.toString().trim())
        prefs.apply()

        Toast.makeText(this, "Keys Correct ✅ Going to Broadcasting...", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, BroadcastActivity::class.java))
    }
}
