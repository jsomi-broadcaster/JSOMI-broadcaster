package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jsomi.broadcaster.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val etPassword = findViewById<EditText>(R.id.etPassword)
val cbShow = findViewById<CheckBox>(R.id.cbShowPassword)

cbShow.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        // Show password
        etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        // Hide password
        etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    // Keep cursor at end
    etPassword.setSelection(etPassword.text.length)
}

        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)

        // If already signed in, go straight to Live
        if (prefs.getBoolean("loggedIn", false)) {
            goLive()
            return
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString("name", name).putString("email", email).putBoolean("loggedIn", true).apply()
            Toast.makeText(this, "Welcome $name!", Toast.LENGTH_SHORT).show()
            goLive()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email + password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Simple sign-in: if they signed up before, let them in
            prefs.edit().putBoolean("loggedIn", true).apply()
            goLive()
        }
    }
    private fun goLive() {
        startActivity(Intent(this, LiveActivity::class.java))
        finish()
    }
}
