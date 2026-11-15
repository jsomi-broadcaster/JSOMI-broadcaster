package com.jsomi.broadcaster

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "Second Camera - Coming Soon - Main Studio is working first"
        tv.textSize = 18f
        tv.setPadding(32, 32, 32, 32)
        setContentView(tv)
    }
}
