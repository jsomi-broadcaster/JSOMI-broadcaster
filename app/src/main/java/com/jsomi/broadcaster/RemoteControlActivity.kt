package com.jsomi.broadcaster
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class RemoteControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        findViewById<Button>(R.id.btnRemoteGoLive).setOnClickListener { Toast.makeText(this, "Remote: GO LIVE sent to Studio", Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteEnd).setOnClickListener { Toast.makeText(this, "Remote: END LIVE sent", Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteSwitch).setOnClickListener { Toast.makeText(this, "Remote: Switch Camera sent", Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteMic).setOnClickListener { Toast.makeText(this, "Remote: Mic Mute toggled", Toast.LENGTH_SHORT).show() }
    }
}
