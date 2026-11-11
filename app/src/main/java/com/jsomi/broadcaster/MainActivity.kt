package com.jsomi.broadcaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BroadcasterApp() } }
    }
}

@Composable
fun BroadcasterApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("CHOOSE") }
    var roomCode by remember { mutableStateOf("JSTV-2026") }
    var enteredCode by remember { mutableStateOf("") }
    var isLinked by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var isDemo by remember { mutableStateOf(false) }
    var liveSeconds by remember { mutableStateOf(0) }
    var currentTime by remember { mutableStateOf("00:00:00") }
    var cameraOn by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var cameraStatus by remember { mutableStateOf("Camera Ready") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { while (true) { try { currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()) } catch (e: Exception) {}; delay(1000) } }
    LaunchedEffect(isLive, isDemo) { while (isLive || isDemo) { delay(1000); liveSeconds += 1 } }
    fun formatLive(s: Int): String { return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60) }
    fun toast(m: String) { Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(msg: String) { val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text=" + Uri.encode(msg))); try { ctx.startActivity(i) } catch (e: Exception) { toast("WhatsApp not found") } }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        // LOGO - TEXT ONLY - NO IMAGE FILE - NO R - CANNOT FAIL
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFF00BCD4)).border(3.dp, Color.Green), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("JSTV", color = Color.Black, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text("BROADCASTER STUDIO", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("CYAN + GREEN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("CYAN + GREEN = BROADCASTER STUDIO", color = Color.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (screen == "CHOOSE") {
            Text("Time: $currentTime", color = Color.Gray, fontSize = 12.sp)
            Text("Choose Your Phone Role:", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { if (!isLinked) toast("Link First") else screen = "MAIN" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MAIN STUDIO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Goes LIVE - Controlled by ADMIN", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { if (!isLinked) toast("Link First") else screen = "REMOTE" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("REMOTE CONTROL", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Control Main From Far", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { if (!isLinked) toast("Link First") else screen = "CAMERA" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("WIRELESS CAMERA", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("2nd Phone As Camera", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(16.dp))
            Text("LINK TO JSTV STUDIO ADMIN (RED LOGO)", color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            OutlinedTextField(value = enteredCode, onValueChange = { enteredCode = it.uppercase() }, label = { Text("Room Code from ADMIN - e.g. JSTV-2026") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (enteredCode.isBlank()) toast("Enter Room Code") else { roomCode = enteredCode; isLinked = true; toast("LINKED to ADMIN $enteredCode") } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Text(if (isLinked) "LINKED - $roomCode" else "LINK TO STUDIO ADMIN", color = Color.Black, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need Room Code.") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("WHATSAPP ADMIN - 0763052911", color = Color.White, fontSize = 12.sp) }
        }

        if (screen == "MAIN") {
            Text("MAIN - BROADCASTER STUDIO", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Linked to ADMIN (RED): $roomCode", color = Color.Yellow, fontSize = 12.sp)
            Text("Time: $currentTime | Live: ${if (isLive || isDemo) formatLive(liveSeconds) else "00:00:00"}", color = Color.White, fontSize = 12.sp)
            Text(if (isLive) "● LIVE ${formatLive(liveSeconds)}" else if (isDemo) "● DEMO ${formatLive(liveSeconds)}" else "● OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray).border(2.dp, Color(0xFF00BCD4)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(if (isLive) "ON AIR - LIVE" else if (isDemo) "DEMO PREVIEW" else "PREVIEW - OFFLINE", color = Color.White, fontWeight = FontWeight.Bold); Text("Camera: ${if (cameraOn) "ON" else "OFF"} | Mic: ${if (micOn) "ON" else "OFF"}", color = Color.Gray, fontSize = 12.sp) } }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Button(onClick = { cameraOn = !cameraOn }) { Text(if (cameraOn) "CAMERA ON" else "CAMERA OFF") }; Button(onClick = { micOn = !micOn }) { Text(if (micOn) "MIC ON" else "MIC OFF") } }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { if (isDemo) { isDemo = false; liveSeconds = 0 } else { isDemo = true; liveSeconds = 0 } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Text(if (isDemo) "STOP DEMO" else "PREVIEW / DEMO - FREE", color = Color.Black, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { if (!isPremium) showPremium = true else { isLive = true; liveSeconds = 0 } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("GO REAL LIVE - NEED PREMIUM CODE", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (screen == "REMOTE") {
            Text("REMOTE CONTROL", color = Color.Green, fontWeight = FontWeight.Bold)
            Text("Linked: $roomCode | $currentTime", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { isDemo = true; isLive = false; liveSeconds = 0 }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Text("START DEMO - FREE", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { if (!isPremium) showPremium = true else { isLive = true; isDemo = false } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("START REAL LIVE - PREMIUM") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isLive = false; isDemo = false; liveSeconds = 0 }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("STOP LIVE") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
        }

        if (screen == "CAMERA") {
            Text("WIRELESS CAMERA", color = Color.Green, fontWeight = FontWeight.Bold)
            Text("Linked: $roomCode | $currentTime", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray).border(2.dp, Color.Green), contentAlignment = Alignment.Center) { Text("CAMERA PREVIEW\n$cameraStatus", color = Color.White, textAlign = TextAlign.Center) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { cameraOn = !cameraOn; cameraStatus = if (cameraOn) "Camera ON - Sending to ADMIN" else "Camera OFF" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON - SENDING" else "CAMERA OFF") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraStatus = "Connected to ADMIN"; toast("Connected") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Text("CONNECT TO STUDIO ADMIN", color = Color.Black) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
        }

        if (showPremium) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REAL STREAMING - PREMIUM REQUIRED", color = Color.Yellow, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Enter Code - e.g. JSTV-200-XXXX") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { if (code.startsWith("JSTV-")) { isPremium = true; showPremium = false; toast("Premium Unlocked") } else toast("Wrong Code") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) { Text("UNLOCK", color = Color.Black, fontWeight = FontWeight.Bold) }
                    Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WHATSAPP ADMIN - 0763052911") }
                    Button(onClick = { showPremium = false }, modifier = Modifier.fillMaxWidth()) { Text("CANCEL - USE DEMO") }
                }
            }
        }
    }
}
