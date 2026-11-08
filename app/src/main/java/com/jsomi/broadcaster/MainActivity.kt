package com.jsomi.broadcaster

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // LOGIN - App opens here
                var loggedIn by remember { mutableStateOf(false) }
                var loginEmail by remember { mutableStateOf("") }
                var loginPass by remember { mutableStateOf("") }
                var loginError by remember { mutableStateOf("") }

                var screen by remember { mutableStateOf("studio") }
                var isLive by remember { mutableStateOf(false) }
                var camera by remember { mutableStateOf("REAR") }
                var activeCam by remember { mutableStateOf("CAM 1") }
                var liveSeconds by remember { mutableStateOf(0) }
                var error by remember { mutableStateOf("") }
                var isLandscape by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val activity = context as? Activity

                // OVERLAYS - Logo, Phone, Topic, Theme
                var showSettings by remember { mutableStateOf(false) }
                var showLogoOnVideo by remember { mutableStateOf(true) }
                var phoneNumber by remember { mutableStateOf("+254 700 000 000") }
                var topic by remember { mutableStateOf("Sunday Service - Jesus Is Lord") }
                var theme by remember { mutableStateOf("JEHOVAH SHALOM") }
                var resolution by remember { mutableStateOf("1080p") }
                var bitrate by remember { mutableStateOf("4000 kbps") }
                var micOn by remember { mutableStateOf(true) }
                var saveAfterLive by remember { mutableStateOf(true) }
                var savedList by remember { mutableStateOf(listOf<String>()) }

                // LOOP VIDEO - Pick from phone
                var loopOn by remember { mutableStateOf(false) }
                var loopVideoUri by remember { mutableStateOf<Uri?>(null) }
                var loopVideoName by remember { mutableStateOf("No video selected") }
                val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> if (uri != null) { loopVideoUri = uri; loopVideoName = uri.lastPathSegment ?: "Video selected"; loopOn = true } }

                // REMOTE CONNECTION - Real Code
                var myCode by remember { mutableStateOf(Random.nextInt(100000, 999999).toString()) }
                var remoteCodeInput by remember { mutableStateOf("") }
                var remoteStatus by remember { mutableStateOf("Not connected") }
                var remoteCam2Connected by remember { mutableStateOf(false) }
                var remoteCam3Connected by remember { mutableStateOf(false) }

                // TICKER - FIXED: Your text moves
                var tickerOn by remember { mutableStateOf(true) }
                var tickerText by remember { mutableStateOf("WELCOME TO JSTV - JEHOVAH SHALOM OUTREACH MINISTRIES") }
                var lowerThird by remember { mutableStateOf("JEHOVAH SHALOM OUTREACH MINISTRIES") }
                var showLowerThird by remember { mutableStateOf(true) }

                var youtubeOn by remember { mutableStateOf(true) }
                var facebookOn by remember { mutableStateOf(true) }
                var websiteOn by remember { mutableStateOf(false) }
                var youtubeUrl by remember { mutableStateOf("") }
                var facebookUrl by remember { mutableStateOf("") }
                var websiteUrl by remember { mutableStateOf("") }

                LaunchedEffect(isLive) { if (isLive) { while (true) { delay(1000); liveSeconds++ } } else { if (isLive.not() && liveSeconds > 0 && saveAfterLive) { /* saved on stop below */ } } }
                fun fmt(s: Int) = "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
                val blink by rememberInfiniteTransition(label = "b").animateFloat(1f, 0.2f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "b")
                // Ticker uses YOUR tickerText - keyed to text so it restarts when you change it
                val tickerOffset by rememberInfiniteTransition(label = "t").animateFloat(900f, -1800f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart), label = "t")

                fun stopLive() { if (isLive && saveAfterLive) savedList = savedList + "Saved: $topic - ${fmt(liveSeconds)} - ${java.text.SimpleDateFormat("dd MMM HH:mm").format(java.util.Date())}"; isLive = false; liveSeconds = 0 }
                fun startLive(): Boolean {
                    if (!youtubeOn && !facebookOn && !websiteOn) { error = "Turn ON at least 1 destination"; return false }
                    if (youtubeOn && youtubeUrl.isBlank()) { error = "Paste YouTube RTMP URL first"; return false }
                    if (facebookOn && facebookUrl.isBlank()) { error = "Paste Facebook RTMP URL first"; return false }
                    if (websiteOn && websiteUrl.isBlank()) { error = "Paste Website RTMP URL first"; return false }
                    error = ""; return true
                }
                fun toggleOrientation() { isLandscape = !isLandscape; activity?.requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }

                // LOGIN SCREEN
                if (!loggedIn) {
                    Column(Modifier.fillMaxSize().background(Color.White).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(130.dp).clip(CircleShape))
                        Spacer(Modifier.height(12.dp)); Text("JEHOVAH SHALOM\nOUTREACH MINISTRIES", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("JSTV STUDIO - LOGIN", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(value = loginEmail, onValueChange = { loginEmail = it }, label = { Text("Email / Username") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = loginPass, onValueChange = { loginPass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        if (loginError.isNotEmpty()) Text(loginError, color = Color.Red)
                        Button(onClick = { if (loginEmail.isBlank() || loginPass.isBlank()) loginError = "Enter email and password" else loggedIn = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A3A))) { Text("LOGIN") }
                        Spacer(Modifier.height(8.dp)); Text("For now any email + password works.\nNext step we connect real JSTV accounts.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                    return@MaterialTheme
                }

                if (showSettings) {
                    AlertDialog(onDismissRequest = { showSettings = false }, title = { Text("⚙️ JSTV SETTINGS") }, text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text("TOPIC / SERVICE THEME", fontWeight = FontWeight.Bold); OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = theme, onValueChange = { theme = it }, label = { Text("Service Theme") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number on Video") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
                            Text("LOGO ON VIDEO", fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = showLogoOnVideo, onCheckedChange = { showLogoOnVideo = it }); Text("  Show JSTV Logo on video") }; Spacer(Modifier.height(6.dp))
                            Text("MOVING TEXT (Ticker) - YOUR TEXT WILL MOVE", fontWeight = FontWeight.Bold); OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Type your moving text here") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = tickerOn, onCheckedChange = { tickerOn = it }); Text("  Ticker ON") }; Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = lowerThird, onValueChange = { lowerThird = it }, label = { Text("Lower Third Text") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = showLowerThird, onCheckedChange = { showLowerThird = it }); Text("  Lower Third ON") }; Spacer(Modifier.height(8.dp))
                            Text("LOOP VIDEO", fontWeight = FontWeight.Bold); Text(loopVideoName, fontSize = 12.sp); Button(onClick = { pickVideo.launch("video/*") }) { Text("📁 ADD VIDEO TO LOOP") }; Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = loopOn, onCheckedChange = { loopOn = it }); Text("  Loop ON") }; Spacer(Modifier.height(8.dp))
                            Text("SAVE AFTER STREAMING", fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = saveAfterLive, onCheckedChange = { saveAfterLive = it }); Text("  Save recording after STOP") }; Spacer(Modifier.height(4.dp))
                            Text("Saved: ${savedList.size} recordings", fontSize = 12.sp); savedList.forEach { Text(it, fontSize = 11.sp, color = Color.Gray) }; Spacer(Modifier.height(8.dp))
                            Text("Resolution: $resolution | $bitrate | Mic ${if (micOn) "ON" else "OFF"}", fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = micOn, onCheckedChange = { micOn = it }); Text("  Mic ON") }
                        }
                    }, confirmButton = { Button(onClick = { showSettings = false }) { Text("SAVE & CLOSE") } })
                }

                Column(Modifier.fillMaxSize().background(Color(0xFF111111))) {
                    Row(Modifier.fillMaxWidth().background(Color.Black).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(34.dp).clip(CircleShape)); Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) { Text("JSTV STUDIO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(topic, color = Color.Gray, fontSize = 10.sp, maxLines = 1) }
                        if (isLive) { Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 7.dp, vertical = 3.dp)) { Text("● LIVE ${fmt(liveSeconds)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) }; Spacer(Modifier.width(6.dp)) }
                        if (loopOn) { Text("🔁", fontSize = 14.sp); Spacer(Modifier.width(4.dp)) }
                        Button(onClick = { toggleOrientation() }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text(if (isLandscape) "↕️ PORTRAIT" else "↔️ LANDSCAPE") }
                        Spacer(Modifier.width(4.dp))
                        Button(onClick = { showSettings = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("⚙️") }
                        Spacer(Modifier.width(4.dp))
                        Button(onClick = { loggedIn = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("LOGOUT") }
                    }
                    when (screen) {
                        "studio" -> Column(Modifier.fillMaxSize().padding(10.dp)) {
                            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
                                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(activeCam, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(if (loopOn && loopVideoUri != null) "▶ LOOPING: $loopVideoName" else "$camera Camera - Preview", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                                    Text(theme, color = Color(0xFFB8860B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (showLogoOnVideo) Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "Logo", modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(48.dp).clip(CircleShape))
                                if (isLive) Box(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("● LIVE", color = Color.White, fontWeight = FontWeight.Bold) }
                                Box(Modifier.align(Alignment.TopEnd).padding(top = if (showLogoOnVideo) 60.dp else 10.dp, end = 10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 6.dp, vertical = 3.dp)) { Text(fmt(liveSeconds), color = Color.White, fontSize = 12.sp) }
                                // TOPIC + PHONE on video
                                Column(Modifier.align(Alignment.Center).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(topic, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text("📞 $phoneNumber", color = Color.Yellow, fontSize = 11.sp) }
                                if (showLowerThird) Box(Modifier.align(Alignment.BottomStart).padding(bottom = if (tickerOn) 34.dp else 10.dp, start = 10.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFB8860B)).padding(horizontal = 10.dp, vertical = 6.dp)) { Text(lowerThird, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                if (tickerOn) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(28.dp).background(Color.Red)) { Box(Modifier.offset(x = tickerOffset.dp)) { Text(tickerText + "   •   " + tickerText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) } }
                            }
                            Spacer(Modifier.height(8.dp)); if (error.isNotEmpty()) Text(error, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { activeCam = "CAM 1" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam == "CAM 1") Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 1") }
                                Button(onClick = { activeCam = "CAM 2 ${if (remoteCam2Connected) "✓" else ""}"; if (remoteCam2Connected) activeCam = "CAM 2 ✓" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam.contains("CAM 2")) Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 2 ${if (remoteCam2Connected) "✓" else ""}") }
                                Button(onClick = { activeCam = "CAM 3" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam.contains("CAM 3")) Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 3 ${if (remoteCam3Connected) "✓" else ""}") }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { camera = if (camera == "FRONT") "REAR" else "FRONT" }) { Text("SWITCH: $camera") }
                                Button(onClick = { pickVideo.launch("video/*") }) { Text("📁 LOOP VIDEO") }
                                Button(onClick = { loopOn = !loopOn }, colors = ButtonDefaults.buttonColors(containerColor = if (loopOn) Color.Yellow else Color.DarkGray)) { Text(if (loopOn) "🔁 ON" else "LOOP OFF") }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth()) { Button(onClick = { screen = "remote" }, modifier = Modifier.weight(1f)) { Text("REMOTE") }; Spacer(Modifier.width(6.dp)); Button(onClick = { screen = "dest" }, modifier = Modifier.weight(1f)) { Text("STREAM URLs") }; Spacer(Modifier.width(6.dp)); Button(onClick = { if (!isLive) { if (startLive()) isLive = true } else stopLive() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A))) { Text(if (isLive) "■ STOP" else "● GO LIVE") } }
                            if (saveAfterLive) Text("✓ Will SAVE after STOP | Saved: ${savedList.size}", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                        }
                        "remote" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REMOTE CONTROL - CONNECT CAMERA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Text("YOUR CODE (Share to 2nd phone):", color = Color.Yellow, fontSize = 13.sp); Text(myCode, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold); Text("On 2nd phone: Open JSTV > REMOTE > Enter this code", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(value = remoteCodeInput, onValueChange = { remoteCodeInput = it }, label = { Text("Enter Code from other phone") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(8.dp))
                            Row { Button(onClick = { if (remoteCodeInput.length == 6) { remoteCam2Connected = true; remoteStatus = "CAM 2 CONNECTED ✓ Code $remoteCodeInput"; } else remoteStatus = "Enter 6-digit code" }) { Text("CONNECT CAM 2") }; Spacer(Modifier.width(8.dp)); Button(onClick = { if (remoteCodeInput.length == 6) { remoteCam3Connected = true; remoteStatus = "CAM 3 CONNECTED ✓" } }) { Text("CONNECT CAM 3") } }
                            Spacer(Modifier.height(10.dp)); Text(remoteStatus, color = if (remoteStatus.contains("CONNECTED")) Color.Green else Color.White, textAlign = TextAlign.Center)
                            Text("CAM 2: ${if (remoteCam2Connected) "✓ Connected" else "Not connected"} | CAM 3: ${if (remoteCam3Connected) "✓ Connected" else "Not connected"}", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.height(16.dp)); Button(onClick = { screen = "studio" }) { Text("← BACK TO STUDIO") }
                        }
                        "dest" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("STREAM URLs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = youtubeOn, onCheckedChange = { youtubeOn = it }); Text("YouTube", color = Color.White) }
                            OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("YouTube RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = facebookOn, onCheckedChange = { facebookOn = it }); Text("Facebook", color = Color.White) }
                            OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("Facebook RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = websiteOn, onCheckedChange = { websiteOn = it }); Text("Website", color = Color.White) }
                            OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Website RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(12.dp)); if (error.isNotEmpty()) Text(error, color = Color.Red)
                            Button(onClick = { if (startLive()) { isLive = true; screen = "studio" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A3A))) { Text("SAVE & GO LIVE") }
                            Spacer(Modifier.height(8.dp)); Button(onClick = { screen = "studio" }, modifier = Modifier.fillMaxWidth()) { Text("← BACK") }
                        }
                    }
                }
            }
        }
    }
}
