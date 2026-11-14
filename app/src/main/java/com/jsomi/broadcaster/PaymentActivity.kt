package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val prefs = getSharedPreferences("JSTV_PREFS", MODE_PRIVATE)

        val tvPayInfo = findViewById<TextView>(R.id.tvPayInfo)
        val etCode = findViewById<EditText>(R.id.etMpesaCode)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmPayment)
        val btnBack = findViewById<Button>(R.id.btnBackToMenu)

        tvPayInfo.text = "Pay to Unlock Premium\n\nM-PESA Paybill: 123456\nAccount: JSTV\nAmount: KSH 500 / Month\n\n1. Go to M-PESA -> Paybill\n2. Enter Code from SMS below\n3. Tap Confirm"

        btnConfirm.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.length < 5) {
                Toast.makeText(this, "Enter valid M-PESA Code e.g. QK123ABC", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Save as Paid - Admin will verify code later in Admin App
            prefs.edit().apply {
                putBoolean("isPaid", true)
                putString("mpesaCode", code)
                apply()
            }
            Toast.makeText(this, "Payment Received! Premium Unlocked", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, HomeMenuActivity::class.java))
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
