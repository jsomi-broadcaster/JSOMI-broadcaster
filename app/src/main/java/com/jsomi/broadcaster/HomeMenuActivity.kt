package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jsomi.broadcaster.databinding.ActivityHomeMenuBinding

class HomeMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show name - same as screenshot: Welcome, kevin
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        val name = prefs.getString("username", "kevin") ?: "kevin"
        binding.tvWelcome.text = "Welcome, $name"

        // 1. CHURCH SCREEN
        binding.btnChurchScreen.setOnClickListener {
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }

        // 2. STUDIO1 - Main Events - Opens Camera as STUDIO1
        binding.btnStudio1.setOnClickListener {
            val i = Intent(this, CameraActivity::class.java)
            i.putExtra("STUDIO", 1)
            i.putExtra("STUDIO_NAME", "STUDIO1 - Main Events")
            startActivity(i)
        }

        // 3. STUDIO2 - Other Events - Opens Camera as STUDIO2
        binding.btnStudio2.setOnClickListener {
            val i = Intent(this, CameraActivity::class.java)
            i.putExtra("STUDIO", 2)
            i.putExtra("STUDIO_NAME", "STUDIO2 - Other Events")
            startActivity(i)
        }

        // 4. REMOTE CONTROL
        binding.btnRemote.setOnClickListener {
            startActivity(Intent(this, RemoteControlActivity::class.java))
        }

        // 5. ADMIN PORTAL
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, DeveloperActivity::class.java))
        }

        // LOGOUT - back to Login, clear stay-logged-in
        binding.btnLogout.setOnClickListener {
            prefs.edit().putBoolean("stay_logged_in", false).putBoolean("is_logged_in", false).apply()
            val i = Intent(this, AuthActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
        }

        // Hidden - MAIN SCREEN is reached from Church Screen / Studio / Remote, not from here
        // If you want it visible, change visibility in XML to visible
        binding.btnMainScreen.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
