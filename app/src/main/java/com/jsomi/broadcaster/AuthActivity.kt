package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity {
    private val ADMIN_EMAIL = "admin@jehovahshalom.tv"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbShow = findViewById<CheckBox>(R.id.cbShowPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val currentPrefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        val subPrefs = getSharedPreferences("JSTV_SUBSCRIBERS", MODE_PRIVATE)
        val adminPrefs = getSharedPreferences("JSTV_ADMIN", MODE_PRIVATE)
        cbShow.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentPrefs.edit().putString("name", name).putString("email", email).apply()
            if (email.equals(ADMIN_EMAIL, ignoreCase = true)) {
                adminPrefs.edit().putString("adminName", name).putString("adminEmail", email).apply()
                startActivity(Intent(this, DeveloperActivity::class.java))
            } else {
                val count = subPrefs.getInt("totalSubscribers", 0) + 1
                subPrefs.edit().putInt("totalSubscribers", count).putString("sub_${count}_name", name).putString("sub_${count}_email", email).putString("sub_${count}_password", password).apply()
                startActivity(Intent(this, LiveActivity::class.java))
            }
        }
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.equals(ADMIN_EMAIL, ignoreCase = true)) {
                startActivity(Intent(this, DeveloperActivity::class.java))
            } else {
                currentPrefs.edit().putString("email", email).apply()
                startActivity(Intent(this, LiveActivity::class.java))
            }
        }
    }
}
