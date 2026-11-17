// ============================================
// JSTV EMAIL SYSTEM - Powered by Brevo
// File: jstv-email.js
// SAFE VERSION - No API Key Inside
// ============================================

// --- 1. SETTINGS - SAFE ---
const BREVO_API_KEY = process.env.BREVO_API_KEY;
const SENDER_NAME = "JSTV";
const SENDER_EMAIL = "info.jsomitv@gmail.com";
const JSTV_WEBSITE = "https://jstv.com";
// ----------------------------------------------

// --- 2. MAIN SENDER ---
async function sendEmail(toEmail, toName, subject, htmlContent) {
  try {
    const response = await fetch("https://api.brevo.com/v3/smtp/email", {
      method: "POST",
      headers: {
        "accept": "application/json",
        "api-key": BREVO_API_KEY,
        "content-type": "application/json"
      },
      body: JSON.stringify({
        sender: { name: SENDER_NAME, email: SENDER_EMAIL },
        to: [{ email: toEmail, name: toName || "JSTV User" }],
        subject: subject,
        htmlContent: htmlContent
      })
    });

    const data = await response.json();
    if (data.messageId) {
      console.log("✅ JSTV Email Sent to:", toEmail);
      return { success: true, data: data };
    } else {
      console.log("❌ JSTV Email Failed:", data);
      return { success: false, data: data };
    }
  } catch (error) {
    console.log("❌ JSTV Email Error:", error);
    return { success: false, error: error };
  }
}

// --- 3. WELCOME EMAIL ---
async function sendWelcomeEmail(toEmail, toName) {
  const html = `
    <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
      <h1 style="color:#000;">Welcome to JSTV 🎉</h1>
      <p>Hello <b>${toName}</b>,</p>
      <p>Thanks for joining <b>JSTV</b>. Your account is now active.</p>
      <a href="${JSTV_WEBSITE}" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block; margin-top:10px;">Open JSTV</a>
      <br><br>
      <p style="color:#888; font-size:12px;">- JSTV Team<br>info.jsomitv@gmail.com</p>
    </div>
  `;
  return sendEmail(toEmail, toName, "Welcome to JSTV 🎉", html);
}

// --- 4. OTP / VERIFICATION CODE ---
async function sendOTPEmail(toEmail, toName, otpCode) {
  const html = `
    <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px; text-align:center;">
      <h2>Hello ${toName}</h2>
      <p>Your JSTV verification code is:</p>
      <h1 style="font-size:45px; letter-spacing:8px; background:#f5f5f5; padding:15px; border-radius:10px;">${otpCode}</h1>
      <p>This code expires in <b>10 minutes</b>. Do not share it with anyone.</p>
      <p style="color:#888; font-size:12px;">- JSTV Team</p>
    </div>
  `;
  return sendEmail(toEmail, toName, `Your JSTV Code: ${otpCode}`, html);
}

// --- 5. PASSWORD RESET ---
async function sendPasswordResetEmail(toEmail, toName, resetLink) {
  const html = `
    <div style="font-family:Arial; max-width:600px; margin:auto; padding:20px; border:1px solid #eee; border-radius:10px;">
      <h2>Hello ${toName}</h2>
      <p>You requested to reset your JSTV password.</p>
      <p>Click the button below:</p>
      <a href="${resetLink}" style="background:#000; color:#fff; padding:12px 25px; text-decoration:none; border-radius:5px; display:inline-block;">Reset Password</a>
      <p style="margin-top:20px;">Or copy this link:<br>${resetLink}</p>
      <p>If you did not request this, just ignore this email.</p>
      <p style="color:#888; font-size:12px;">- JSTV Team</p>
    </div>
  `;
  return sendEmail(toEmail, toName, "Reset Your JSTV Password", html);
}
