package com.jstv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVFullApp() }
    }
}

@Composable
fun JSTVFullApp() {
    // --- STATE ---
    var isLive by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var camera2Active by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf(1) }
    var rtmpUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/XXXX-XXXX") }
    var tickerText by remember { mutableStateOf("JSTV - JEHOVAH SHALOM | PREMIUM: Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000 | Paybill 516600 Acc 5600935001 | WhatsApp 0763052911") }
    var tickerEditing by remember { mutableStateOf(false) }
    var premiumCode by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }
    var selectedGallery by remember { mutableStateOf<String?>(null) }
    var showRemote by remember { mutableStateOf(false) }
    var micMuted by remember { mutableStateOf(false) }

    val validCodes = listOf(
        "JSTV-50-1A2B","JSTV-1K-3C4D","JSTV-3M-5E6F","JSTV-YR-7G8H",
        "JSTV-50-2B3C","JSTV-1K-4D5E","JSTV-3M-6F7G","JSTV-YR-8H9I",
        "JSTV-50-9Z8Y","JSTV-1K-7X6W"
    )
    val galleryItems = listOf("Church Service","Choir","Preaching","Testimony","Youth","Prayer")

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // ===== HEADER - JSTV =====
            Text("JSTV", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 4.sp)
            Text("By Kevin", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.85f))
            Text("Jehovah Shalom", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
            Spacer(Modifier.height(6.dp))
            Text(if (isLive) "● LIVE - ON AIR" else "○ OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Scene S$scene  |  Camera: ${if (isFrontCamera) "FRONT" else "BACK"} ${if (camera2Active) "+ CAM 2" else ""}  |  Mic: ${if (micMuted) "MUTED" else "ON"}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            // ===== CAMERA PREVIEW - FULL =====
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp)
                    .background(if (scene == 4) Color.DarkGray else Color(0xFF1A1A1A), RoundedCornerShape(14.dp))
                    .border(2.dp, if (isLive) Color.Red else Color.Gray, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (scene == 4) "S4 - GALLERY" else "CAMERA PREVIEW - S$scene", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (isFrontCamera) "Front Camera" else "Back Camera - Main", color = Color.Yellow, fontSize = 12.sp)
                    if (camera2Active) Text("CAM 2 ACTIVE - Picture in Picture", color = Color.Green, fontSize = 12.sp)
                    if (selectedGallery != null) Text("Playing: $selectedGallery", color = Color.Cyan, fontSize = 12.sp)
                    Text("Your real camera will show here on phone", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                // PIP for Cam 2
                if (camera2Active) {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(80.dp, 55.dp).background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("CAM 2", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Logo overlay on preview
                Box(modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("JSTV - Jehovah Shalom", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))

            // ===== MULTI-CAMERA =====
            Text("MULTI-CAMERA", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { isFrontCamera = false }, modifier = Modifier.weight(1f)) { Text("CAM 1 BACK") }
                Button(onClick = { isFrontCamera = true }, modifier = Modifier.weight(1f)) { Text("CAM 1 FRONT") }
                Button(onClick = { camera2Active = !camera2Active }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (camera2Active) Color.Green else Color.Gray)) { Text(if (camera2Active) "CAM 2 ON" else "CAM 2 OFF") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { micMuted = !micMuted }, modifier = Modifier.weight(1f)) { Text(if (micMuted) "UNMUTE MIC" else "MUTE MIC") }
                Button(onClick = { showRemote = !showRemote }, modifier = Modifier.weight(1f)) { Text(if (showRemote) "HIDE REMOTE" else "SHOW REMOTE") }
            }
            Spacer(Modifier.height(10.dp))

            // ===== REMOTE CONTROL =====
            if (showRemote) {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF222222), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REMOTE CONTROL - Use 2nd Phone / Same Phone", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Remote = Control JSTV from far. Start/Stop, Switch Camera, Switch Scene", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { isLive = !isLive }) { Text(if (isLive) "REMOTE: STOP" else "REMOTE: GO LIVE") }
                            Button(onClick = { isFrontCamera = !isFrontCamera }) { Text("REMOTE: SWITCH CAM") }
                            Button(onClick = { scene = if (scene >= 4) 1 else scene + 1 }) { Text("REMOTE: NEXT SCENE") }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // ===== SCENES S1-S4 =====
            Text("SCENES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scene = 1; selectedGallery = null }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == 1) Color.Red else Color.DarkGray)) { Text("S1 STUDIO") }
                Button(onClick = { scene = 2; selectedGallery = null }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == 2) Color.Red else Color.DarkGray)) { Text("S2 CHURCH") }
                Button(onClick = { scene = 3; selectedGallery = null }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == 3) Color.Red else Color.DarkGray)) { Text("S3 PREACHER") }
                Button(onClick = { scene = 4 }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == 4) Color.Red else Color.DarkGray)) { Text("S4 GALLERY") }
            }
            Spacer(Modifier.height(10.dp))

            // ===== BROADCAST / RTMP =====
            Text("BROADCAST", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = rtmpUrl, onValueChange = { rtmpUrl = it }, label = { Text("RTMP URL - YouTube / Facebook / TikTok") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { isLive = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("START LIVE") }
                Button(onClick = { isLive = false }, modifier = Modifier.weight(1f)) { Text("STOP LIVE") }
            }
            Spacer(Modifier.height(12.dp))

            // ===== TICKER =====
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("TICKER", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (tickerEditing) "DONE" else "EDIT", color = Color.Yellow, modifier = Modifier.clickable { tickerEditing = !tickerEditing })
            }
            if (tickerEditing) {
                OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Edit Ticker Text") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            } else {
                Text(tickerText, color = Color.White, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color.DarkGray, RoundedCornerShape(8.dp)).padding(10.dp))
            }
            Spacer(Modifier.height(12.dp))

            // ===== GALLERY =====
            Text("GALLERY - Tap to Play in S4", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(galleryItems) { g ->
                    Box(modifier = Modifier.size(110.dp, 70.dp).background(if (selectedGallery == g) Color.Red else Color.DarkGray, RoundedCornerShape(10.dp)).clickable { selectedGallery = g; scene = 4 }, contentAlignment = Alignment.Center) {
                        Text(g, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            // ===== PREMIUM =====
            Text(if (isPremium) "✅ PREMIUM ACTIVE - JEHOVAH SHALOM" else "JSTV PREMIUM", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("Paybill 516600 Acc 5600935001 | WhatsApp 0763052911", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = premiumCode, onValueChange = { premiumCode = it }, label = { Text("Enter Premium Code") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (validCodes.contains(premiumCode.trim().uppercase())) { isPremium = true; premiumMessage = "✅ Welcome to Premium! Code Accepted - Jehovah Shalom" }
                else { premiumMessage = "❌ Wrong Code. Pay via Paybill 516600 then WhatsApp 0763052911" }
            }, modifier = Modifier.fillMaxWidth()) { Text("UNLOCK PREMIUM") }
            if (premiumMessage.isNotEmpty()) Text(premiumMessage, color = if (isPremium) Color.Green else Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("JSTV - Jehovah Shalom - By Kevin - Mombasa, Kenya", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
        }
    }
}
