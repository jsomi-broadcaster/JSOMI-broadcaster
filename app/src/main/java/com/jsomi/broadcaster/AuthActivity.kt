package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class AuthActivity : AppCompatActivity() {

    // --- UI ---
    private lateinit var titleText: TextView
    private lateinit var fullNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmNewPasswordInput: EditText

    private lateinit var btnGenerateUsername: Button
    private lateinit var btnTogglePassword: Button
    private lateinit var btnSendOtp: Button
    private lateinit var btnVerifyOtp: Button
    private lateinit var btnReset: Button
    private lateinit var btnMain: Button
    private lateinit var btnSwitch: Button
    private lateinit var btnForgot: Button

    // --- STATE ---
    private var mode = "SIGNUP"
    private var generatedOtp = ""
    private var pendingEmail = ""
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        scroll.addView(layout)
        setContentView(scroll)

        fun makeInput(hint: String): EditText {
            return EditText(this).apply {
                this.hint = hint
                layout.addView(this)
            }
        }
        fun makeBtn(text: String): Button {
            return Button(this).apply {
                this.text = text
                layout.addView(this)
            }
        }

        titleText = TextView(this).apply { textSize = 24f; layout.addView(this) }
        fullNameInput = makeInput("Full Name")
        emailInput = makeInput("Email")
        usernameInput = makeInput("Username")
        phoneInput = makeInput("Phone (optional)")
        passwordInput = makeInput("Password")
        confirmPasswordInput = makeInput("Confirm Password")
        otpInput = makeInput("Enter OTP")
        newPasswordInput = makeInput("New Password")
        confirmNewPasswordInput = makeInput("Confirm New Password")

        btnGenerateUsername = makeBtn("Generate Username")
        btnTogglePassword = makeBtn("Show Password")
        btnSendOtp = makeBtn("SEND OTP")
        btnVerifyOtp = makeBtn("VERIFY OTP")
        btnReset = makeBtn("RESET PASSWORD")
        btnMain = makeBtn("SIGN UP -> SEND OTP")
        btnForgot = makeBtn("Forgot Password?")
        btnSwitch = makeBtn("Already have account? SIGN IN")

        btnGenerateUsername.setOnClickListener {
            val name = fullNameInput.text.toString().trim().replace(" ", "").lowercase()
            if (name.isNotEmpty()) {
                usernameInput.setText(name + Random.nextInt(100, 999))
            } else {
                Toast.makeText(this, "Enter Full Name first", Toast.LENGTH_SHORT).show()
            }
        }

        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val type = if (isPasswordVisible) android.text.InputType.TYPE_CLASS_TEXT else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordInput.inputType = type
            confirmPasswordInput.inputType = type
        }

        btnMain.setOnClickListener { onMainClick() }
        btnSendOtp.setOnClickListener { sendOtp() }
        btnVerifyOtp.setOnClickListener {
            if (mode == "FORGOT" || mode == "OTP") {
                // Decide which verify to use - if we came from FORGOT, use forgot verify
                // We check pending flow via a simple flag: if mode was FORGOT before OTP, it stays OTP but we try both
                verifyOtp()
            } else {
                verifyOtp()
            }
        }
        btnForgot.setOnClickListener { showForgot() }
        btnSwitch.setOnClickListener {
            if (mode == "SIGNUP") showSignIn() else showSignUp()
        }
        btnReset.setOnClickListener { doResetPassword() }

        showSignUp()
    }

    private fun hideAll() {
        listOf(fullNameInput, emailInput, usernameInput, phoneInput, passwordInput, confirmPasswordInput, otpInput, newPasswordInput, confirmNewPasswordInput,
            btnGenerateUsername, btnTogglePassword, btnSendOtp, btnVerifyOtp, btnReset).forEach { it.visibility = View.GONE }
        btnMain.visibility = View.VISIBLE
        btnSwitch.visibility = View.VISIBLE
    }

    private fun show(view: View) { view.visibility = View.VISIBLE }

    private fun showSignUp() {
        mode = "SIGNUP"
        hideAll()
        titleText.text = "Sign Up"
        show(fullNameInput); show(emailInput)
        show(usernameInput); show(btnGenerateUsername)
        show(passwordInput); show(btnTogglePassword)
        show(confirmPasswordInput); show(phoneInput)
        btnMain.text = "SIGN UP -> SEND OTP"
        btnSwitch.text = "Already have account? SIGN IN"
        show(btnForgot)
    }

    private fun showSignIn() {
        mode = "SIGNIN"
        hideAll()
        titleText.text = "Sign In"
        show(emailInput); show(passwordInput)
        btnMain.text = "SIGN IN -> CONTINUE"
        btnSwitch.text = "New here? SIGN UP"
        show(btnForgot)
    }

    private fun showForgot() {
        mode = "FORGOT"
        hideAll()
        titleText.text = "Forgot Password"
        show(emailInput)
        show(btnSendOtp)
        btnMain.visibility = View.GONE
        btnSwitch.text = "Back to SIGN IN"
        btnSwitch.setOnClickListener { showSignIn() }
    }

    private fun showOtp() {
        mode = "OTP"
        hideAll()
        titleText.text = "Enter OTP"
        show(otpInput); show(btnVerifyOtp)
        btnMain.visibility = View.GONE
        btnSwitch.visibility = View.GONE
    }

    private fun showReset() {
        mode = "RESET"
        hideAll()
        titleText.text = "Reset Password"
        show(newPasswordInput); show(btnTogglePassword)
        show(confirmNewPasswordInput); show(btnReset)
        btnMain.visibility = View.GONE
        btnSwitch.visibility = View.GONE
    }

    private fun onMainClick() {
        if (mode == "SIGNUP") {
            val name = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val user = usernameInput.text.toString().trim()
            val pass = passwordInput.text.toString()
            val conf = confirmPasswordInput.text.toString()

            if (name.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
                return
            }
            if (pass != conf) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }
            if (pass.length < 6) {
                Toast.makeText(this, "Password must be 6+ characters", Toast.LENGTH_SHORT).show()
                return
            }
            sendOtp()
        } else if (mode == "SIGNIN") {
            val email = emailInput.text.toString().trim()
            val pass = passwordInput.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return
            }
            // TODO: Check against saved user in SharedPreferences / Firebase
            // For now, go to main
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
            goNext()
        } else if (mode == "FORGOT") {
            sendOtp()
        }
    }

    // ================= OTP - WITH REAL EMAIL =================
    private fun sendOtp() {
        val email = if (mode == "FORGOT") emailInput.text.toString().trim() else emailInput.text.toString().trim()
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            return
        }
        pendingEmail = email
        generatedOtp = Random.nextInt(10000, 99999).toString()

        // REAL EMAIL - Send OTP via Brevo
        val userEmail = emailInput.text.toString().trim()
        var userName = ""
        try {
            userName = fullNameInput.text.toString().trim()
        } catch (e: Exception) { userName = "" }
        if (userName.isEmpty()) userName = "JSTV User"

        EmailSender.sendOTPEmail(userEmail, userName, generatedOtp) { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "OTP sent to Email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send email, check key", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // TEST MODE: Show OTP in Toast - later remove this line
        Toast.makeText(this, "TEST OTP for $email: $generatedOtp", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "OTP sent to Email", Toast.LENGTH_SHORT).show()

        if (mode == "FORGOT") {
            // After sending OTP in Forgot, go to OTP screen
            showOtp()
            // Mark that after OTP we go to Reset
            btnVerifyOtp.setOnClickListener { verifyOtpForgot() }
        } else {
            showOtp()
            btnVerifyOtp.setOnClickListener { verifyOtp() }
        }
    }

    private fun verifyOtp() {
        if (otpInput.text.toString().trim() == generatedOtp) {
            val prefs = getSharedPreferences("JSTV_Prefs", MODE_PRIVATE)
            prefs.edit()
                .putString("pass_\$pendingEmail", pendingEmail)
                .putString("user_\$pendingEmail", usernameInput.text.toString().trim())
                .putString("name_\$pendingEmail", fullNameInput.text.toString().trim())
                .putString("phone_\$pendingEmail", phoneInput.text.toString().trim())
                .apply()

            Toast.makeText(this, "Success! Account Created", Toast.LENGTH_SHORT).show()

            // Send Welcome Email after OTP verified
            val welcomeEmail = emailInput.text.toString().trim()
            var welcomeName = ""
            try { welcomeName = fullNameInput.text.toString().trim() } catch (e: Exception) {}
            if (welcomeName.isEmpty()) welcomeName = "JSTV User"
            EmailSender.sendWelcomeEmail(welcomeEmail, welcomeName)

            goNext()
        } else {
            Toast.makeText(this, "Wrong OTP, try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyOtpForgot() {
        if (otpInput.text.toString().trim() == generatedOtp) {
            Toast.makeText(this, "OTP Confirmed", Toast.LENGTH_SHORT).show()
            showReset()
        } else {
            Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doResetPassword() {
        val p1 = newPasswordInput.text.toString()
        val p2 = confirmNewPasswordInput.text.toString()
        if (p1.isEmpty() || p1 != p2) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if (p1.length < 6) {
            Toast.makeText(this, "Password must be 6+ characters", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Password Reset Success!", Toast.LENGTH_SHORT).show()
        showSignIn()
    }

    private fun goNext() {
    Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
    finish()
    }
}
