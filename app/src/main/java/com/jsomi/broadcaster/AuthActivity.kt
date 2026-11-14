package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        val prefs = getSharedPreferences("JSTV_PREFS", MODE_PRIVATE)

        // If already signed in, go straight to Camera + Menu (Screen 2+3)
        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, HomeMenuActivity::class.java))
            finish()
            return
        }

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill Name, Phone, Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Save user locally for now - Firebase later in Admin App
            prefs.edit().apply {
                putString("userName", name)
                putString("userPhone", phone)
                putString("userPass", pass)
                putBoolean("isLoggedIn", true)
                putBoolean("isPaid", false) // Not paid yet - settings locked
                apply()
            }
            Toast.makeText(this, "Welcome $name", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeMenuActivity::class.java))
            finish()
        }

        btnSignIn.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val savedPhone = prefs.getString("userPhone", "")
            val savedPass = prefs.getString("userPass", "")
            if (phone == savedPhone && pass == savedPass && phone.isNotEmpty()) {
                prefs.edit().putBoolean("isLoggedIn", true).apply()
                startActivity(Intent(this, HomeMenuActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Wrong Phone or Password. Sign Up first.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
