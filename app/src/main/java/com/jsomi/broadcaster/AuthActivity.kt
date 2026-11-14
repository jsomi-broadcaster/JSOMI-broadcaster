package com.jstv.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AuthActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnLogin: Button
    private lateinit var imgProfile: ImageView

    // Only photo button opens Files - NOT Sign Up button
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imgProfile.setImageURI(uri)
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        imgProfile = findViewById(R.id.imgProfile)

        // Ask permission on open
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // PHOTO button -> Opens Files - CORRECT
        imgProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        // SIGN UP button -> Goes to Home - FIXED, no longer opens Files
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save and go to Home
            val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
            prefs.edit().putString("name", name).putString("phone", phone).apply()

            Toast.makeText(this, "Welcome $name", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            // Login also goes to Home for now
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
