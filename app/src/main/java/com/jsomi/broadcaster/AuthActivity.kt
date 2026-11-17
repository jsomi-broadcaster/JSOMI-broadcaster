package com.jsomi.broadcaster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(40, 40, 40, 40)
        setContentView(root)

        val title = TextView(this)
        title.text = "Sign Up / Sign In"
        title.textSize = 24f
        title.setTextColor(Color.WHITE)
        root.addView(title)

        val etName = EditText(this); etName.hint = "Full Name"; etName.setHintTextColor(Color.GRAY); etName.setTextColor(Color.WHITE); root.addView(etName)
        val etEmail = EditText(this); etEmail.hint = "Email"; etEmail.setHintTextColor(Color.GRAY); etEmail.setTextColor(Color.WHITE); root.addView(etEmail)
        val etPass = EditText(this); etPass.hint = "Password"; etPass.setHintTextColor(Color.GRAY); etPass.setTextColor(Color.WHITE); root.addView(etPass)

        val btnUp = Button(this); btnUp.text = "SIGN UP"
        btnUp.setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("name", etName.text.toString()).putString("email", etEmail.text.toString()).apply()
            Toast.makeText(this, "Welcome ${etName.text}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PlanActivity::class.java))
        }
        root.addView(btnUp)

        val btnIn = Button(this); btnIn.text = "SIGN IN"
        btnIn.setOnClickListener {
            Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PlanActivity::class.java))
        }
        root.addView(btnIn)
    }
}
