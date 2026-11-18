package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        val logo = ImageView(this).apply {
            setImageResource(R.mipmap_jstv_logo) // YOUR OLD LOGO - keep
            layoutParams = LinearLayout.LayoutParams(500, 500)
        }
        layout.addView(logo)
        setContentView(layout)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, BroadcastActivity::class.java))
            finish()
        }, 2000)
    }
}
