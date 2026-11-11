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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// JSTV BROADCASTER - FOR EVERYONE TO TRY - PREMIUM ONLY ON GO LIVE
// NO ADMIN - NO MONEY RECORD - NO GENERATE CODE
// WhatsApp: 0763052911

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BroadcasterApp()
            }
        }
    }
}

@Composable
fun BroadcasterApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("HOME") }
    var channel by remember { mutableStateOf("JSTV Main") }
    var scene by remember { mutableStateOf("Studio") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Your Voice, Your Channel") }
    var cameraOn by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("HD 720p") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var streamTitle by remember { mutableStateOf("My Live Show") }

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
    fun tryGoLive() {
        if (isPremium) {
            isLive = true
            toast("You Are LIVE - $channel")
        } else {
            showPremium = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP - BROADCASTER NOT VIEWERS
        Text("JSTV", color = Color.Yellow, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text("BROADCASTER", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Create Your Own Live Channel - Free To Try", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // TABS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { screen = "HOME" }, colors = ButtonDefaults.buttonColors(containerColor = if (screen == "HOME") Color.Yellow else Color.DarkGray)) { Text("HOME", color = if (screen == "HOME") Color.Black else Color.White) }
            Button(onClick = { screen = "SETUP" }, colors = ButtonDefaults.buttonColors(containerColor = if (screen == "SETUP") Color.Yellow else Color.DarkGray)) { Text("SETUP", color = if (screen == "SETUP") Color.Black else Color.White) }
            Button(onClick = { screen = "CHANNELS" }, colors = ButtonDefaults.buttonColors(containerColor = if (screen == "CHANNELS") Color.Yellow else Color.DarkGray)) { Text("CHANNELS", color = if (screen == "CHANNELS") Color.Black else Color.White) }
            Button(onClick = { screen = "HELP" }, colors = ButtonDefaults.buttonColors(containerColor = if (screen == "HELP") Color.Yellow else Color.DarkGray)) { Text("HELP", color = if (screen == "HELP") Color.Black else Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (screen == "HOME") {
            // PREVIEW - LIKE BEFORE
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isLive) "ON AIR - LIVE" else "PREVIEW", color = if (isLive) Color.Red else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Channel: $channel", color = Color.White, fontSize = 14.sp)
                    Text("Scene: $scene", color = Color.White, fontSize = 14.sp)
                    Text("Camera: ${if (cameraOn) "ON" else "OFF"} | Mic: ${if (micOn) "ON" else "OFF"}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Stream Title", color = Color.White, fontSize = 13.sp)
            OutlinedTextField(value = streamTitle, onValueChange = { streamTitle = it }, label = { Text("e.g. My Live Show") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { cameraOn = !cameraOn }, colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON" else "CAMERA OFF") }
                Button(onClick = { micOn = !micOn }, colors = ButtonDefaults.buttonColors(containerColor = if (micOn) Color.Green else Color.DarkGray)) { Text(if (micOn) "MIC ON" else "MIC OFF") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Ticker: $ticker", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            // GO LIVE - THIS IS WHERE PREMIUM IS ASKED
            Button(onClick = { if (isLive) { isLive = false; toast("Broadcast Stopped") } else { tryGoLive() } }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color.Green)) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Try all features free. Go Live unlocks with Premium Code.", color = Color.Gray, fontSize = 11.sp)
            if (isPremium) {
                Text("Premium Active - Enjoy", color = Color.Green, fontSize = 12.sp)
            }
        }

        if (screen == "SETUP") {
            Text("SETUP - YOUR STUDIO", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Video Quality", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { quality = "SD 480p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "SD 480p") Color.Yellow else Color.DarkGray)) { Text("SD", color = if (quality == "SD 480p") Color.Black else Color.White) }
                Button(onClick = { quality = "HD 720p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "HD 720p") Color.Yellow else Color.DarkGray)) { Text("HD 720p", color = if (quality == "HD 720p") Color.Black else Color.White) }
                Button(onClick = { quality = "FHD 1080p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "FHD 1080p") Color.Yellow else Color.DarkGray)) { Text("FHD 1080p", color = if (quality == "FHD 1080p") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Selected: $quality", color = Color.Yellow, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Scene", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { scene = "Studio" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Studio") Color.Yellow else Color.DarkGray)) { Text("Studio", color = if (scene == "Studio") Color.Black else Color.White) }
                Button(onClick = { scene = "Outdoor" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Outdoor") Color.Yellow else Color.DarkGray)) { Text("Outdoor", color = if (scene == "Outdoor") Color.Black else Color.White) }
                Button(onClick = { scene = "Church" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Church") Color.Yellow else Color.DarkGray)) { Text("Church", color = if (scene == "Church") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Ticker Text", color = Color.White, fontSize = 14.sp)
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker - Shows on Live") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "HOME" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("SAVE & BACK TO HOME", color = Color.Black) }
        }

        if (screen == "CHANNELS") {
            Text("YOUR CHANNELS", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Tap to select your channel", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val list = listOf("JSTV Main", "JSTV Sports", "JSTV Music", "JSTV News", "JSTV Church", "My Channel")
            list.forEach { c ->
                Button(onClick = { channel = c; toast("Channel: $c"); screen = "HOME" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (channel == c) Color.Yellow else Color.DarkGray)) { Text(c, color = if (channel == c) Color.Black else Color.White) }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        if (screen == "HELP") {
            Text("HELP & CONTACT", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Need Premium Code to Go Live?", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 13.sp)
            Text("Paybill 516600 Acc 5600935001", color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code to Go Live. I have paid.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WHATSAPP US - 0763052911", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need help with BROADCASTER app.") }, modifier = Modifier.fillMaxWidth()) { Text("WHATSAPP - NEED HELP") }
        }

        // PREMIUM POPUP - ONLY WHEN THEY PRESS GO LIVE
        if (showPremium) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GO LIVE - PREMIUM REQUIRED", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("You can try everything free. To Go Live, enter your Premium Code.", color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Enter Code - e.g. JSTV-200-XXXX") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (code.startsWith("JSTV-")) {
                            isPremium = true
                            showPremium = false
                            isLive = true
                            toast("Premium Unlocked - You Are LIVE!")
                        } else {
                            toast("Wrong Code - WhatsApp Us")
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("UNLOCK & GO LIVE", color = Color.Black, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Daily 200 | Monthly 1000 | Paybill 516600 Acc 5600935001", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code to Go Live.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WHATSAPP FOR CODE - 0763052911") }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { showPremium = false }, modifier = Modifier.fillMaxWidth()) { Text("CANCEL - TRY LATER") }
                }
            }
        }
    }
}
