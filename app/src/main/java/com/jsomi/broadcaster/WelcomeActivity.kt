package com.jsomi.broadcaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        // NO button code, NO intent, just show screen
        // If this still crashes, the problem is in the LAYOUT or THEME, not the code
    }
}
