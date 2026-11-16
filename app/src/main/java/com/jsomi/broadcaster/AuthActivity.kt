package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private val ADMIN_EMAIL = "admin@jehovahshalom.tv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentPrefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        val subPrefs = getSharedPreferences("JSTV_SUBSCRIBERS", MODE_PRIVATE)
        val adminPrefs = getSharedPreferences("JSTV_ADMIN", MODE_PRIVATE)

        // Build screen in code - no XML ids needed
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 60, 60, 60)
        layout.setBackgroundColor(Color.WHITE)
        layout.gravity = Gravity.CENTER

        val title = TextView(this)
        title.text = "Jehovah Shalom TV\nLogin / Sign Up"
        title.textSize = 24f
        title.setTextColor(Color.BLACK)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 40)
        layout.addView(title)

        val etName = EditText(this)
        etName.hint = "Full Name"
        layout.addView(etName)

        val etEmail = EditText(this)
        etEmail.hint = "Email"
        layout.addView(etEmail)

        val etPassword = EditText(this)
        etPassword.hint = "Password"
        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(etPassword)

        val cbShow = CheckBox(this)
        cbShow.text = "Show Password"
        layout.addView(cbShow)

        val btnSignUp = Button(this)
        btnSignUp.text = "SIGN UP"
        layout.addView(btnSignUp)

        val btnSignIn = Button(this)
        btnSignIn.text = "SIGN IN"
        layout.addView(btnSignIn)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)

        // SHOW / HIDE PASSWORD
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
                Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DeveloperActivity::class.java))
            } else {
                val count = subPrefs.getInt("totalSubscribers", 0) + 1
                subPrefs.edit().putInt("totalSubscribers", count).putString("sub_${count}_name", name).putString("sub_${count}_email", email).putString("sub_${count}_password", password).apply()
                Toast.makeText(this, "Welcome to Jehovah Shalom TV, $name", Toast.LENGTH_SHORT).show()
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
