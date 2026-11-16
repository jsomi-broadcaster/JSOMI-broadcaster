package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jsomi.broadcaster.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = getSharedPreferences("jstv", MODE_PRIVATE).getString("name", "JSTV User") ?: "JSTV User"
        binding.tvWelcome.text = "Welcome, $name"

        binding.btnMainStudio.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "MAIN_STUDIO")
            startActivity(i)
        }
        binding.btnExtraCamera.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "EXTRA_CAMERA")
            startActivity(i)
        }
        binding.btnRemote.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "REMOTE_CONTROL")
            startActivity(i)
        }
        binding.btnChurchScreen.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("mode", "CHURCH_SCREEN")
            startActivity(i)
        }
        binding.btnLogout.setOnClickListener {
            getSharedPreferences("jstv", MODE_PRIVATE).edit().putBoolean("signed_in", false).apply()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}
