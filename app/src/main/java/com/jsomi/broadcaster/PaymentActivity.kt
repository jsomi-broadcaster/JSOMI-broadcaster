package com.jsomi.broadcaster

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

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etPhone = findViewById<EditText>(R.id.etPhonePayment)
        val tvStatus = findViewById<TextView>(R.id.tvPaymentStatus)

        findViewById<Button>(R.id.btnPayMpesa).setOnClickListener {
            val amount = etAmount.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            if (amount.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter Amount and Phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            tvStatus.text = "Processing M-PESA KSh $amount to $phone..."
            Toast.makeText(this, "M-PESA Prompt Sent to $phone", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btnPayCard).setOnClickListener {
            Toast.makeText(this, "Card Payment - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnBackPayment).setOnClickListener { finish() }
    }
}
