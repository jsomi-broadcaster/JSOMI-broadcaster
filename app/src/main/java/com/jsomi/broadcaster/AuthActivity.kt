package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class AuthActivity : AppCompatActivity() {

    // Views
    private lateinit var titleText: TextView
    private lateinit var fullNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmNewPasswordInput: EditText

    private lateinit var btnGenerateUsername: Button
    private lateinit var btnTogglePass: Button
    private lateinit var btnToggleConfirmPass: Button
    private lateinit var btnToggleNewPass: Button
    private lateinit var btnToggleConfirmNewPass: Button

    private lateinit var btnMain: Button
    private lateinit var btnSwitch: Button
    private lateinit var btnForgot: Button
    private lateinit var btnSendOtp: Button
    private lateinit var btnVerifyOtp: Button
    private lateinit var btnReset: Button

    private var mode = "SIGNUP" // SIGNUP, SIGNIN, OTP, FORGOT, RESET
    private var generatedOtp = ""
    private var pendingEmail = ""
    private var pendingPass = ""
    private var pendingName = ""
    private var pendingPhone = ""
    private var pendingUsername = ""

    private var passVisible = false
    private var confirmPassVisible = false
    private var newPassVisible = false
    private var confirmNewPassVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(android.graphics.Color.BLACK)
        }
        scroll.addView(layout)
        setContentView(scroll)

        titleText = TextView(this).apply {
            text = "Sign Up"
            textSize = 30f
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(titleText)

        fullNameInput = makeInput("Full Name")
        emailInput = makeInput("Email - e.g. you@gmail.com")
        phoneInput = makeInput("Phone Number - e.g. 0712345678")
        phoneInput.inputType = InputType.TYPE_CLASS_PHONE
        usernameInput = makeInput("Username - write or Generate")
        passwordInput = makePassInput("Password")
        confirmPasswordInput = makePassInput("Confirm Password")
        otpInput = makeInput("Enter OTP sent to Email")
        otpInput.inputType = InputType.TYPE_CLASS_NUMBER
        newPasswordInput = makePassInput("New Password")
        confirmNewPasswordInput = makePassInput("Confirm New Password")

        layout.addView(fullNameInput)
        layout.addView(emailInput)
        layout.addView(phoneInput)

        // Username row + Generate button
        layout.addView(usernameInput)
        btnGenerateUsername = makeButton("GENERATE USERNAME 🎲")
        btnGenerateUsername.setOnClickListener {
            val base = emailInput.text.toString().substringBefore("@").ifEmpty { "jstv" }
            val gen = base + Random.nextInt(100, 999)
            usernameInput.setText(gen)
            Toast.makeText(this, "Generated: $gen", Toast.LENGTH_SHORT).show()
        }
        layout.addView(btnGenerateUsername)

        // Password + Eye
        layout.addView(passwordInput)
        btnTogglePass = makeButton("👁️ SHOW / HIDE PASSWORD")
        btnTogglePass.setOnClickListener {
            passVisible = !passVisible
            passwordInput.inputType = if (passVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordInput.setSelection(passwordInput.text.length)
        }
        layout.addView(btnTogglePass)

        layout.addView(confirmPasswordInput)
        btnToggleConfirmPass = makeButton("👁️ SHOW / HIDE CONFIRM PASSWORD")
        btnToggleConfirmPass.setOnClickListener {
            confirmPassVisible = !confirmPassVisible
            confirmPasswordInput.inputType = if (confirmPassVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
        }
        layout.addView(btnToggleConfirmPass)

        // OTP views
        layout.addView(otpInput)
        btnSendOtp = makeButton("SEND OTP TO EMAIL")
        btnSendOtp.setOnClickListener { sendOtp() }
        layout.addView(btnSendOtp)

        btnVerifyOtp = makeButton("VERIFY OTP → CONTINUE")
        btnVerifyOtp.setOnClickListener { verifyOtp() }
        layout.addView(btnVerifyOtp)

        // Reset password views
        layout.addView(newPasswordInput)
        btnToggleNewPass = makeButton("👁️ SHOW / HIDE NEW PASSWORD")
        btnToggleNewPass.setOnClickListener {
            newPassVisible = !newPassVisible
            newPasswordInput.inputType = if (newPassVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            newPasswordInput.setSelection(newPasswordInput.text.length)
        }
        layout.addView(btnToggleNewPass)

        layout.addView(confirmNewPasswordInput)
        btnToggleConfirmNewPass = makeButton("👁️ SHOW / HIDE CONFIRM NEW PASSWORD")
        btnToggleConfirmNewPass.setOnClickListener {
            confirmNewPassVisible = !confirmNewPassVisible
            confirmNewPasswordInput.inputType = if (confirmNewPassVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            confirmNewPasswordInput.setSelection(confirmNewPasswordInput.text.length)
        }
        layout.addView(btnToggleConfirmNewPass)

        btnReset = makeButton("RESET PASSWORD → SIGN IN")
        btnReset.setOnClickListener { doReset() }
        layout.addView(btnReset)

        // Main + Switch + Forgot
        btnMain = makeButton("SIGN UP → SEND OTP")
        btnMain.setOnClickListener { onMainClick() }
        layout.addView(btnMain)

        btnForgot = makeButton("FORGOT PASSWORD?")
        btnForgot.setOnClickListener { showForgot() }
        layout.addView(btnForgot)

        btnSwitch = makeButton("Already have account? SIGN IN")
        btnSwitch.setOnClickListener {
            if (mode == "SIGNUP") showSignIn() else showSignUp()
        }
        layout.addView(btnSwitch)

        showSignUp()
    }

    private fun makeInput(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            setHintTextColor(android.graphics.Color.GRAY)
            setTextColor(android.graphics.Color.WHITE)
            textSize = 18f
            setPadding(0, 24, 0, 24)
        }
    }

    private fun makePassInput(hint: String): EditText {
        return makeInput(hint).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    private fun makeButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(android.graphics.Color.LTGRAY)
            setTextColor(android.graphics.Color.BLACK)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 16
            layoutParams = p
        }
    }

    private fun hideAll() {
        listOf(fullNameInput, emailInput, phoneInput, usernameInput, passwordInput, confirmPasswordInput, otpInput, newPasswordInput, confirmNewPasswordInput,
            btnGenerateUsername, btnTogglePass, btnToggleConfirmPass, btnToggleNewPass, btnToggleConfirmNewPass,
            btnSendOtp, btnVerifyOtp, btnReset, btnForgot).forEach { it.visibility = android.view.View.GONE }
        btnMain.visibility = android.view.View.VISIBLE
        btnSwitch.visibility = android.view.View.VISIBLE
    }

    private fun show(view: android.view.View) { view.visibility = android.view.View.VISIBLE }

    private fun showSignUp() {
        mode = "SIGNUP"
        hideAll()
        titleText.text = "Sign Up"
        show(fullNameInput); show(emailInput); show(phoneInput)
        show(usernameInput); show(btnGenerateUsername)
        show(passwordInput); show(btnTogglePass)
        show(confirmPasswordInput); show(btnToggleConfirmPass)
        btnMain.text = "SIGN UP → SEND OTP"
        btnSwitch.text = "Already have account? SIGN IN"
        show(btnForgot)
    }

    private fun showSignIn() {
        mode = "SIGNIN"
        hideAll()
        titleText.text = "Sign In"
        show(emailInput); show(passwordInput); show(btnTogglePass)
        btnMain.text = "SIGN IN → CONTINUE"
        btnSwitch.text = "New here? SIGN UP"
        show(btnForgot)
    }

    private fun showForgot() {
        mode = "FORGOT"
        hideAll()
        titleText.text = "Forgot Password"
        show(emailInput)
        show(btnSendOtp)
        btnMain.visibility = android.view.View.GONE
        btnSwitch.text = "Back to SIGN IN"
        btnSwitch.setOnClickListener { showSignIn() }
    }

    private fun showOtp() {
        mode = "OTP"
        hideAll()
        titleText.text = "Enter OTP"
        show(otpInput); show(btnVerifyOtp)
        btnMain.visibility = android.view.View.GONE
        btnSwitch.visibility = android.view.View.GONE
    }

    private fun showReset() {
        mode = "RESET"
        hideAll()
        titleText.text = "Reset Password"
        show(newPasswordInput); show(btnToggleNewPass)
        show(confirmNewPasswordInput); show(btnToggleConfirmNewPass)
        show(btnReset)
        btnMain.visibility = android.view.View.GONE
        btnSwitch.visibility = android.view.View.GONE
    }

    private fun onMainClick() {
        if (mode == "SIGNUP") {
            val name = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val user = usernameInput.text.toString().trim()
            val pass = passwordInput.text.toString()
            val conf = confirmPasswordInput.text.toString()
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show(); return
            }
            if (!email.contains("@")) { Toast.makeText(this, "Enter valid Gmail", Toast.LENGTH_SHORT).show(); return }
            if (pass.length < 6) { Toast.makeText(this, "Password min 6 chars", Toast.LENGTH_SHORT).show(); return }
            if (pass != conf) { Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show(); return }
            pendingName = name; pendingEmail = email; pendingPhone = phone; pendingUsername = user; pendingPass = pass
            sendOtp()
        } else if (mode == "SIGNIN") {
            val email = emailInput.text.toString().trim()
            val pass = passwordInput.text.toString()
            if (email.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "Enter Email + Password", Toast.LENGTH_SHORT).show(); return }
            // Check saved user - if none saved, allow for testing
            val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
            val savedPass = prefs.getString("pass_$email", null)
            if (savedPass != null && savedPass != pass) { Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show(); return }
            Toast.makeText(this, "Welcome back ${prefs.getString("user_$email", email)}", Toast.LENGTH_SHORT).show()
            goNext()
        }
    }

    private fun sendOtp() {
        val email = if (mode == "FORGOT") emailInput.text.toString().trim() else pendingEmail.ifEmpty { emailInput.text.toString().trim() }
        if (email.isEmpty() || !email.contains("@")) { Toast.makeText(this, "Enter valid Email first", Toast.LENGTH_SHORT).show(); return }
        pendingEmail = email
        generatedOtp = Random.nextInt(100000, 999999).toString()
        // TEST MODE: Show OTP in Toast - later replace with real email server
        Toast.makeText(this, "TEST OTP for $email is: $generatedOtp", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "OTP sent to Email (Test Mode)", Toast.LENGTH_SHORT).show()
        if (mode == "FORGOT") {
            // After sending OTP in Forgot, go to OTP screen, then Reset
            showOtp()
            // Mark that after OTP we go to Reset, not Signup
            btnVerifyOtp.setOnClickListener { verifyOtpForgot() }
        } else {
            showOtp()
            btnVerifyOtp.setOnClickListener { verifyOtp() }
        }
    }

    private fun verifyOtp() {
        if (otpInput.text.toString().trim() == generatedOtp) {
            val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
            prefs.edit()
                .putString("pass_$pendingEmail", pendingPass)
                .putString("user_$pendingEmail", pendingUsername)
                .putString("name_$pendingEmail", pendingName)
                .putString("phone_$pendingEmail", pendingPhone)
                .apply()
            Toast.makeText(this, "Success! Welcome $pendingUsername", Toast.LENGTH_SHORT).show()
            goNext()
        } else {
            Toast.makeText(this, "Wrong OTP. Try again. Test OTP was: $generatedOtp", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyOtpForgot() {
        if (otpInput.text.toString().trim() == generatedOtp) {
            Toast.makeText(this, "OTP Confirmed - Now Reset Password", Toast.LENGTH_SHORT).show()
            showReset()
        } else {
            Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doReset() {
        val np = newPasswordInput.text.toString()
        val cnp = confirmNewPasswordInput.text.toString()
        if (np.length < 6) { Toast.makeText(this, "Password min 6 chars", Toast.LENGTH_SHORT).show(); return }
        if (np != cnp) { Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show(); return }
        val prefs = getSharedPreferences("jstv", MODE_PRIVATE)
        prefs.edit().putString("pass_$pendingEmail", np).apply()
        Toast.makeText(this, "Password Reset! Now Sign In", Toast.LENGTH_SHORT).show()
        showSignIn()
    }

    private fun goNext() {
        startActivity(Intent(this, PlanActivity::class.java))
        finish()
    }
}
