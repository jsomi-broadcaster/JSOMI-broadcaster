package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DestinationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(40, 40, 40, 40)
        setContentView(root)

        val title = TextView(this); title.text = "Where to Broadcast?"; title.textSize = 24f; title.setTextColor(Color.WHITE); root.addView(title)

        val etKey = EditText(this); etKey.hint = "Paste STREAM KEY here"; etKey.setHintTextColor(Color.GRAY); etKey.setTextColor(Color.WHITE); root.addView(etKey)

        fun go(dest: String, url: String) {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("dest", dest).putString("rtmpUrl", url).putString("streamKey", etKey.text.toString()).apply()
            startActivity(Intent(this, BroadcastActivity::class.java))
        }

        val btnFb = Button(this); btnFb.text = "FACEBOOK LIVE"; btnFb.setOnClickListener { go("Facebook", "rtmp://live-api-s.facebook.com:80/rtmp/") }; root.addView(btnFb)
        val btnYt = Button(this); btnYt.text = "YOUTUBE LIVE"; btnYt.setOnClickListener { go("YouTube", "rtmp://a.rtmp.youtube.com/live2/") }; root.addView(btnYt)
        val btnTt = Button(this); btnTt.text = "TIKTOK LIVE"; btnTt.setOnClickListener { go("TikTok", "rtmp://push-rtmp-l1.tiktokcdn.com/live/") }; root.addView(btnTt)
    }
}
