package com.jsomi.broadcaster
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class ChurchScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_church)
        val title = getSharedPreferences("JSTV", MODE_PRIVATE).getString("title", "JSTV Live")
        findViewById<TextView>(R.id.tvChurchTitle).text = "⛪ $title - CHURCH SCREEN"
    }
}
