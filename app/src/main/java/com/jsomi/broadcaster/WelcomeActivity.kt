package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.gravity = Gravity.CENTER
        root.setPadding(40, 40, 40, 40)
        setContentView(root)

        val logo = TextView(this)
        logo.text = "✝️\nJSTV"
        logo.textSize = 70f
        logo.setTextColor(Color.WHITE)
        logo.gravity = Gravity.CENTER
        root.addView(logo)

        val title = TextView(this)
        title.text = "Jehovah Shalom TV"
        title.textSize = 28f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        root.addView(title)

        val sub = TextView(this)
        sub.text = "Broadcast the Gospel to the World"
        sub.setTextColor(Color.YELLOW)
        sub.gravity = Gravity.CENTER
        root.addView(sub)

        val btn = Button(this)
        btn.text = "GET STARTED →"
        btn.setOnClickListener { startActivity(Intent(this, AuthActivity::class.java)) }
        root.addView(btn)
    }
}
