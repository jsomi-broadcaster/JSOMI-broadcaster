package com.jsomi.broadcaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// JSTV - JEHOVAH SHALOM TV - BROADCASTER APP
// RESTORED EXACTLY AS BEFORE - ONLY ADMIN REMOVED
// Admin is ONLY in ADMIN STUDIO APP
// WhatsApp: 0763052911

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                JstvApp()
            }
        }
    }
}

@Composable
fun JstvApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("CHOOSE") }
    var channel by remember { mutableStateOf("JSTV Main") }
    var scene by remember { mutableStateOf("Studio") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Jehovah Shalom TV") }
    var cameraOn by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("HD 720p") }
    var streamTitle by remember { mutableStateOf("My Live Show") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var remoteCommand by remember { mutableStateOf("Ready") }
    var cameraStatus by remember { mutableStateOf("Camera Ready - Waiting for Main") }

    fun toast(m: String) {
        Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()
    }
    fun openWhatsApp(msg: String) {
        val num = "254763052911"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + num + "?text=" + Uri.encode(msg)))
        try {
            ctx.startActivity(i)
        } catch (e: Exception) {
            toast("WhatsApp not found")
        }
    }
    fun shareApp() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, "Download JSTV - Jehovah Shalom TV - BROADCASTER App. WhatsApp 0763052911 for link.")
        try {
            ctx.startActivity(Intent.createChooser(i, "Share JSTV App"))
        } catch (e: Exception) {
            toast("Cannot share")
        }
    }
    fun tryGoLive() {
        if (isPremium) {
            isLive = true
            toast("You Are LIVE to YouTube - $channel")
        } else {
            showPremium = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ========== CHOOSE ROLE SCREEN - EXACT AS YOUR SCREENSHOT ==========
        if (screen == "CHOOSE") {
            Spacer(modifier = Modifier.height(100.dp))
            Text("JSTV", color = Color.Yellow, fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text("JEHOVAH SHALOM TV", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Who Are You? Choose Your Phone Role:", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { screen = "MAIN" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MAIN STUDIO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Goes LIVE to YouTube", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { screen = "REMOTE" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REMOTE CONTROL", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Control Main From Far", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { screen = "CAMERA" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WIRELESS CAMERA", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("2nd Phone As Camera", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // NO ADMIN BUTTON HERE - REMOVED - ADMIN IS ONLY IN ADMIN STUDIO APP
            Text("Same App On All Phones - Just Choose Different Mode", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { shareApp() }, modifier = Modifier.fillMaxWidth(0.85f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B6BB0)), shape = MaterialTheme.shapes.extraLarge) {
                Text("SHARE JSTV APP TO 2ND PHONE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need help with BROADCASTER app.") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("WHATSAPP HELP - 0763052911", color = Color.White, fontSize = 12.sp)
            }
        }

        // ========== MAIN STUDIO - ALL FEATURES KEPT ==========
        if (screen == "MAIN") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("MAIN STUDIO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Goes LIVE to YouTube", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isLive) "ON AIR - LIVE" else "PREVIEW", color = if (isLive) Color.Red else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Channel: $channel", color = Color.White, fontSize = 14.sp)
                    Text("Scene: $scene", color = Color.White, fontSize = 14.sp)
                    Text("Quality: $quality", color = Color.White, fontSize = 12.sp)
                    Text("Camera: ${if (cameraOn) "
