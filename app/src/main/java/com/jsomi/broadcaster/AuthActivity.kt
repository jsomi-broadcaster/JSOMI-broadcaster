package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvGoToSignIn = findViewById<TextView>(R.id.tvGoToSignIn)

        fun goHome() {
            startActivity(Intent(this, HomeMenuActivity::class.java))
            finish()
        }

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (name.isEmpty()) { etName.error = "Enter name"; return@setOnClickListener }
            if (email.isEmpty()) { etEmail.error = "Enter email"; return@setOnClickListener }
            if (pass.isEmpty()) { etPassword.error = "Enter password"; return@setOnClickListener }
            Toast.makeText(this, "Welcome $name - Signed Up!", Toast.LENGTH_SHORT).show()
            goHome()
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty()) { etEmail.error = "Enter email"; return@setOnClickListener }
            if (pass.isEmpty()) { etPassword.error = "Enter password"; return@setOnClickListener }
            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
            goHome()
        }

        tvGoToSignIn.setOnClickListener {
            Toast.makeText(this, "Enter Email + Password then tap SIGN IN", Toast.LENGTH_SHORT).show()
        }
    }
}
