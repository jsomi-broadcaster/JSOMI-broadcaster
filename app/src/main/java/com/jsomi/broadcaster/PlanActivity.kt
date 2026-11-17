package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PlanActivity : AppCompatActivity() {

    private lateinit var premiumDetailsLayout: LinearLayout
    private var premiumOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        scroll.addView(layout)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "Choose Plan"
            textSize = 30f
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(title)

        val sub = TextView(this).apply {
            text = "EVERYTHING IS FREE FOR NOW ✅"
            textSize = 18f
            setTextColor(android.graphics.Color.GREEN)
        }
        layout.addView(sub)

        val sub2 = TextView(this).apply {
            text = "No Payment Needed - Tap to Continue"
            textSize = 14f
            setTextColor(android.graphics.Color.LTGRAY)
        }
        layout.addView(sub2)

        // FREE - Tap -> Next
        val btnFree = makeButton("FREE MODE - CONTINUE (FREE) →")
        btnFree.setOnClickListener {
            savePlan("FREE")
            goNext("FREE")
        }
        layout.addView(btnFree)

        // PREMIUM - Tap -> Opens list
        val btnPremium = makeButton("PREMIUM - WEEKLY 200 / MONTHLY 1000 (FREE FOR NOW) ▼ TAP TO SEE SERVICES")
        btnPremium.setOnClickListener {
            if (!premiumOpen) {
                premiumDetailsLayout.visibility = android.view.View.VISIBLE
                premiumOpen = true
                btnPremium.text = "PREMIUM - SERVICES OPEN BELOW ▼"
            } else {
                premiumDetailsLayout.visibility = android.view.View.GONE
                premiumOpen = false
                btnPremium.text = "PREMIUM - WEEKLY 200 / MONTHLY 1000 (FREE FOR NOW) ▼ TAP TO SEE SERVICES"
            }
        }
        layout.addView(btnPremium)

        // PREMIUM DETAILS
        premiumDetailsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(android.graphics.Color.DKGRAY)
            visibility = android.view.View.GONE
        }
        layout.addView(premiumDetailsLayout)

        val services = listOf(
            "✅ PREMIUM SERVICES - ALL FREE FOR NOW:",
            "",
            "🎥 1. HD Quality - 1080p Clear Video",
            "📡 2. Multi-Platform - Facebook + YouTube + TikTok SAME TIME",
            "🎙️ 3. Studio Mode - Logo, Verse, Ticker, Bible Overlay",
            "⏰ 4. No Time Limit - Broadcast as long as you want",
            "💾 5. Save & Replay - Your Lives are saved",
            "🎵 6. Background Music + Sound Effects",
            "👥 7. Unlimited Viewers",
            "🛡️ 8. Priority Support",
            "🚫 9. No Ads / No Watermark",
            "",
            "💰 NORMAL PRICES (NOT CHARGED NOW):",
            "• WEEKLY - KES 200 / week",
            "• MONTHLY - KES 1000 / month",
            "",
            "🆓 NOW: FREE - NO PAYMENT - JUST TAP CONTINUE"
        )
        for (s in services) {
            val tv = TextView(this).apply {
                text = s
                textSize = 16f
                setTextColor(if (s.startsWith("✅")) android.graphics.Color.YELLOW else android.graphics.Color.WHITE)
                setPadding(0, 6, 0, 6)
            }
            premiumDetailsLayout.addView(tv)
        }

        // WEEKLY - Tap -> Next, NO PAYMENT
        val btnWeekly = makeButton("WEEKLY - KES 200 - FREE NOW → CONTINUE")
        btnWeekly.setOnClickListener {
            savePlan("PREMIUM_WEEKLY_FREE")
            goNext("PREMIUM WEEKLY - FREE")
        }
        premiumDetailsLayout.addView(btnWeekly)

        // MONTHLY - Tap -> Next, NO PAYMENT
        val btnMonthly = makeButton("MONTHLY - KES 1000 - FREE NOW → CONTINUE ⭐ BEST VALUE")
        btnMonthly.setOnClickListener {
            savePlan("PREMIUM_MONTHLY_FREE")
            goNext("PREMIUM MONTHLY - FREE")
        }
        premiumDetailsLayout.addView(btnMonthly)

        val btnHide = makeButton("HIDE PREMIUM LIST ▲")
        btnHide.setOnClickListener {
            premiumDetailsLayout.visibility = android.view.View.GONE
            premiumOpen = false
            btnPremium.text = "PREMIUM - WEEKLY 200 / MONTHLY 1000 (FREE FOR NOW) ▼ TAP TO SEE SERVICES"
        }
        premiumDetailsLayout.addView(btnHide)

        val note = TextView(this).apply {
            text = "\nPayment (Manual / Pop-up) will be added later when you say. For now everything is FREE."
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
        }
        layout.addView(note)
    }

    private fun makeButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(android.graphics.Color.LTGRAY)
            setTextColor(android.graphics.Color.BLACK)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 20
            layoutParams = p
        }
    }

    private fun savePlan(plan: String) {
        getSharedPreferences("jstv", MODE_PRIVATE).edit().putString("plan", plan).apply()
    }

    private fun goNext(plan: String) {
        Toast.makeText(this, "Plan: $plan - FREE", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}
