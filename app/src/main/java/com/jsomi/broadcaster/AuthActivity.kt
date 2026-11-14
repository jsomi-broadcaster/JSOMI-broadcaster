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
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putString("name", name).putString("email", email).putString("pass", pass).apply()
            Toast.makeText(this, "Welcome $name", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeMenuActivity::class.java)); finish()
        }
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val savedEmail = prefs.getString("email", "")
            val savedPass = prefs.getString("pass", "")
            if (savedEmail.isNullOrEmpty() || (email == savedEmail && pass == savedPass)) {
                Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeMenuActivity::class.java)); finish()
            } else Toast.makeText(this, "Wrong Email or Password. Tap SIGN UP first.", Toast.LENGTH_LONG).show()
        }
    }
}
