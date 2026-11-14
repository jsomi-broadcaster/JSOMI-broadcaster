package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AuthActivity : AppCompatActivity() {

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val etName = findViewById<EditText>(R.id.etName)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        // Ask for Notification permission - THIS IS THE PERMISSION POPUP
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // SIGN UP - Goes to Home, does NOT open Files anymore
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (name.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill name and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
            prefs.edit().putString("name", name).apply()

            Toast.makeText(this, "Welcome $name", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
