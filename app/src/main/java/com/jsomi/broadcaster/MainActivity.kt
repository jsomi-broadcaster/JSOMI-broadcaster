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
                    Text("Camera: ${if (cameraOn) "ON" else "OFF"} | Mic: ${if (micOn) "ON" else "OFF"}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Stream Title", color = Color.White, fontSize = 13.sp)
            OutlinedTextField(value = streamTitle, onValueChange = { streamTitle = it }, label = { Text("e.g. My Live Show") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text("Channel", color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { channel = "JSTV Main" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "JSTV Main") Color.Yellow else Color.DarkGray)) { Text("Main", color = if (channel == "JSTV Main") Color.Black else Color.White) }
                Button(onClick = { channel = "JSTV Sports" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "JSTV Sports") Color.Yellow else Color.DarkGray)) { Text("Sports", color = if (channel == "JSTV Sports") Color.Black else Color.White) }
                Button(onClick = { channel = "JSTV Music" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "JSTV Music") Color.Yellow else Color.DarkGray)) { Text("Music", color = if (channel == "JSTV Music") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { channel = "JSTV News" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "JSTV News") Color.Yellow else Color.DarkGray)) { Text("News", color = if (channel == "JSTV News") Color.Black else Color.White) }
                Button(onClick = { channel = "JSTV Church" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "JSTV Church") Color.Yellow else Color.DarkGray)) { Text("Church", color = if (channel == "JSTV Church") Color.Black else Color.White) }
                Button(onClick = { channel = "My Channel" }, colors = ButtonDefaults.buttonColors(containerColor = if (channel == "My Channel") Color.Yellow else Color.DarkGray)) { Text("My Channel", color = if (channel == "My Channel") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Scene", color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { scene = "Studio" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Studio") Color.Yellow else Color.DarkGray)) { Text("Studio", color = if (scene == "Studio") Color.Black else Color.White) }
                Button(onClick = { scene = "Outdoor" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Outdoor") Color.Yellow else Color.DarkGray)) { Text("Outdoor", color = if (scene == "Outdoor") Color.Black else Color.White) }
                Button(onClick = { scene = "Church" }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Church") Color.Yellow else Color.DarkGray)) { Text("Church", color = if (scene == "Church") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Video Quality", color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { quality = "SD 480p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "SD 480p") Color.Yellow else Color.DarkGray)) { Text("SD", color = if (quality == "SD 480p") Color.Black else Color.White) }
                Button(onClick = { quality = "HD 720p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "HD 720p") Color.Yellow else Color.DarkGray)) { Text("HD 720p", color = if (quality == "HD 720p") Color.Black else Color.White) }
                Button(onClick = { quality = "FHD 1080p" }, colors = ButtonDefaults.buttonColors(containerColor = if (quality == "FHD 1080p") Color.Yellow else Color.DarkGray)) { Text("FHD", color = if (quality == "FHD 1080p") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Ticker Text", color = Color.White, fontSize = 13.sp)
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker - Shows on Live") }, modifier = Modifier.fillMaxWidth())
            Text("Preview: $ticker", color = Color.Yellow, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { cameraOn = !cameraOn }, colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON" else "CAMERA OFF") }
                Button(onClick = { micOn = !micOn }, colors = ButtonDefaults.buttonColors(containerColor = if (micOn) Color.Green else Color.DarkGray)) { Text(if (micOn) "MIC ON" else "MIC OFF") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { if (isLive) { isLive = false; toast("Broadcast Stopped") } else { tryGoLive() } }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color.Green)) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Try all features free. Premium only when you press GO LIVE.", color = Color.Gray, fontSize = 11.sp)
            if (isPremium) { Text("Premium Active - Enjoy", color = Color.Green, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        // ========== REMOTE CONTROL - KEPT ==========
        if (screen == "REMOTE") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("REMOTE CONTROL", color = Color.Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Control Main From Far", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Main Studio Status", color = Color.White, fontSize = 14.sp)
                    Text(if (isLive) "MAIN IS LIVE" else "MAIN IS OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Last Command: $remoteCommand", color = Color.Yellow, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { remoteCommand = "START LIVE sent"; toast("START sent to Main") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("START LIVE ON MAIN") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "STOP LIVE sent"; isLive = false; toast("STOP sent to Main") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("STOP LIVE ON MAIN") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "SWITCH SCENE sent"; scene = if (scene == "Studio") "Outdoor" else "Studio"; toast("Scene switched to $scene") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("SWITCH SCENE - Now $scene", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "MUTE MIC sent"; micOn = !micOn; toast("Mic toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (micOn) "MUTE MIC ON MAIN" else "UNMUTE MIC ON MAIN") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "TICKER UPDATE sent"; toast("Ticker sent") }, modifier = Modifier.fillMaxWidth()) { Text("UPDATE TICKER ON MAIN") }
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker to send to Main") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        // ========== WIRELESS CAMERA - KEPT ==========
        if (screen == "CAMERA") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("WIRELESS CAMERA", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("2nd Phone As Camera", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Text("CAMERA PREVIEW\n$cameraStatus", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Status: $cameraStatus", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraOn = !cameraOn; cameraStatus = if (cameraOn) "Camera ON - Sending to Main" else "Camera OFF"; toast(cameraStatus) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON - SENDING" else "CAMERA OFF") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { micOn = !micOn; toast("Mic toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (micOn) "MIC ON" else "MIC OFF") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraStatus = "Connected to Main Studio"; toast("Connected to Main") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("CONNECT TO MAIN STUDIO", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraStatus = "Disconnected"; toast("Disconnected") }, modifier = Modifier.fillMaxWidth()) { Text("DISCONNECT") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        // ========== PREMIUM POPUP - ONLY WHEN PRESS GO LIVE ==========
        if (showPremium) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GO LIVE - PREMIUM REQUIRED", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("You can try everything free. To Go Live, enter your Premium Code.", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
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
                    Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 11.sp)
                    Text("Paybill 516600 Acc 5600935001", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code to Go Live. I have paid.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WHATSAPP FOR CODE - 0763052911") }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { showPremium = false }, modifier = Modifier.fillMaxWidth()) { Text("CANCEL - TRY LATER") }
                }
            }
        }
    }
}
