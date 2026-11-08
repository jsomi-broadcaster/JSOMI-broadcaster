package com.jsomi.broadcaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var screen by remember { mutableStateOf("studio") }
                var isLive by remember { mutableStateOf(false) }
                var camera by remember { mutableStateOf("REAR") }
                var activeCam by remember { mutableStateOf("CAM 1") }
                var liveSeconds by remember { mutableStateOf(0) }
                var error by remember { mutableStateOf("") }

                // SETTINGS - Inside the app
                var showSettings by remember { mutableStateOf(false) }
                var resolution by remember { mutableStateOf("1080p") }
                var bitrate by remember { mutableStateOf("4000 kbps") }
                var micOn by remember { mutableStateOf(true) }
                var loopOn by remember { mutableStateOf(false) }
                var tickerOn by remember { mutableStateOf(true) }
                var tickerText by remember { mutableStateOf("WELCOME TO JSTV - JEHOVAH SHALOM OUTREACH MINISTRIES - JESUS IS LORD - JOIN US LIVE - - -") }
                var lowerThird by remember { mutableStateOf("JEHOVAH SHALOM OUTREACH MINISTRIES") }
                var showLowerThird by remember { mutableStateOf(true) }

                // STREAM URLs - You type when live
                var youtubeOn by remember { mutableStateOf(true) }
                var facebookOn by remember { mutableStateOf(true) }
                var websiteOn by remember { mutableStateOf(false) }
                var youtubeUrl by remember { mutableStateOf("") }
                var facebookUrl by remember { mutableStateOf("") }
                var websiteUrl by remember { mutableStateOf("") }

                // LIVE TIMER
                LaunchedEffect(isLive) { if (isLive) { while (true) { delay(1000); liveSeconds++ } } else liveSeconds = 0 }
                fun fmt(s: Int): String = "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)

                // BLINKING LIVE DOT
                val blink by rememberInfiniteTransition(label = "blink").animateFloat(initialValue = 1f, targetValue = 0.2f, animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "blink")

                // MOVING TICKER
                val tickerOffset by rememberInfiniteTransition(label = "ticker").animateFloat(initialValue = 800f, targetValue = -1400f, animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart), label = "ticker")

                fun startLive(): Boolean {
                    if (!youtubeOn && !facebookOn && !websiteOn) { error = "Turn ON at least 1 destination"; return false }
                    if (youtubeOn && youtubeUrl.isBlank()) { error = "Paste YouTube RTMP URL first"; return false }
                    if (facebookOn && facebookUrl.isBlank()) { error = "Paste Facebook RTMP URL first"; return false }
                    if (websiteOn && websiteUrl.isBlank()) { error = "Paste Website RTMP URL first"; return false }
                    error = ""; return true
                }

                // SETTINGS SHEET - On every screen
                if (showSettings) {
                    AlertDialog(onDismissRequest = { showSettings = false }, title = { Text("⚙️ JSTV SETTINGS") }, text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text("Resolution", fontWeight = FontWeight.Bold)
                            Row { listOf("720p","1080p").forEach { r -> FilterChip(selected = resolution == r, onClick = { resolution = r }, label = { Text(r) }); Spacer(Modifier.width(6.dp)) } }
                            Spacer(Modifier.height(8.dp))
                            Text("Bitrate", fontWeight = FontWeight.Bold)
                            Row { listOf("2500 kbps","4000 kbps","6000 kbps").forEach { b -> FilterChip(selected = bitrate == b, onClick = { bitrate = b }, label = { Text(b) }); Spacer(Modifier.width(4.dp)) } }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = micOn, onCheckedChange = { micOn = it }); Text("  Mic ON") }
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = loopOn, onCheckedChange = { loopOn = it }); Text("  🔁 LOOP / Replay") }
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = tickerOn, onCheckedChange = { tickerOn = it }); Text("  Moving Text (Ticker) ON") }
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = showLowerThird, onCheckedChange = { showLowerThird = it }); Text("  Lower Third ON") }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Moving Text") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = lowerThird, onValueChange = { lowerThird = it }, label = { Text("Lower Third Text") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text("Camera: $camera | $resolution | $bitrate | Mic: ${if (micOn) "ON" else "OFF"} | Loop: ${if (loopOn) "ON" else "OFF"}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }, confirmButton = { Button(onClick = { showSettings = false }) { Text("SAVE & CLOSE") } })
                }

                // TOP BAR - On every screen
                Column(Modifier.fillMaxSize().background(Color(0xFF111111))) {
                    Row(Modifier.fillMaxWidth().background(Color.Black).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(36.dp).clip(CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) { Text("JSTV STUDIO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("JEHOVAH SHALOM", color = Color.Gray, fontSize = 11.sp) }
                        if (isLive) { Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("● LIVE ${fmt(liveSeconds)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.width(8.dp)) }
                        if (loopOn) { Text("🔁 LOOP", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)) }
                        Button(onClick = { showSettings = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("⚙️") }
                    }

                    when (screen) {
                        "studio" -> Column(Modifier.fillMaxSize().padding(12.dp)) {
                            // TV DISPLAY
                            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
                                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(activeCam, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("$camera Camera - Preview", color = Color.Gray, fontSize = 14.sp)
                                    if (!isLive) Text("Tap GO LIVE to start", color = Color.Gray, fontSize = 12.sp)
                                    if (loopOn) Text("🔁 LOOP MODE", color = Color.Yellow, fontSize = 12.sp)
                                }
                                // LIVE BADGE ON VIDEO
                                if (isLive) Box(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("● LIVE", color = Color.White, fontWeight = FontWeight.Bold) }
                                Box(Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 6.dp, vertical = 3.dp)) { Text(fmt(liveSeconds), color = Color.White, fontSize = 12.sp) }
                                // LOWER THIRD ON VIDEO
                                if (showLowerThird) Box(Modifier.align(Alignment.BottomStart).padding(bottom = if (tickerOn) 34.dp else 10.dp).padding(start = 10.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFB8860B)).padding(horizontal = 10.dp, vertical = 6.dp)) { Text(lowerThird, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                // MOVING TICKER ON VIDEO
                                if (tickerOn) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(28.dp).background(Color.Red)) { Box(Modifier.offset(x = tickerOffset.dp)) { Text(tickerText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) } }
                            }
                            Spacer(Modifier.height(10.dp))
                            if (error.isNotEmpty()) Text(error, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { activeCam = "CAM 1" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam == "CAM 1") Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 1") }
                                Button(onClick = { activeCam = "CAM 2" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam == "CAM 2") Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 2") }
                                Button(onClick = { activeCam = "CAM 3" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam == "CAM 3") Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 3") }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { camera = if (camera == "FRONT") "REAR" else "FRONT" }) { Text("SWITCH: $camera") }
                                Button(onClick = { loopOn = !loopOn }, colors = ButtonDefaults.buttonColors(containerColor = if (loopOn) Color.Yellow else Color.DarkGray)) { Text(if (loopOn) "🔁 LOOP ON" else "LOOP OFF") }
                                Button(onClick = { screen = "remote" }) { Text("REMOTE") }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { screen = "dest" }, modifier = Modifier.weight(1f)) { Text("STREAM URLs") }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { if (!isLive) { if (startLive()) isLive = true } else isLive = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A))) { Text(if (isLive) "■ STOP LIVE" else "● GO LIVE") }
                            }
                            if (isLive) Text("LIVE to: " + listOfNotNull(if (youtubeOn) "YouTube" else null, if (facebookOn) "Facebook" else null, if (websiteOn) "Website" else null).joinToString(", "), color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                        }
                        "remote" -> Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REMOTE CONTROL", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Text("2nd phone as CAM 2", color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {}) { Text("CONNECT CAM 2") }; Spacer(Modifier.height(8.dp)); Button(onClick = {}) { Text("CONNECT CAM 3") }
                            Spacer(Modifier.height(16.dp)); Button(onClick = { screen = "studio" }) { Text("← BACK TO STUDIO") }
                        }
                        "dest" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("STREAM URLs - Type When Live", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = youtubeOn, onCheckedChange = { youtubeOn = it }); Text("YouTube Live", color = Color.White) }
                            OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("YouTube RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = facebookOn, onCheckedChange = { facebookOn = it }); Text("Facebook Live", color = Color.White) }
                            OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("Facebook RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = websiteOn, onCheckedChange = { websiteOn = it }); Text("JSTV Website", color = Color.White) }
                            OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Website RTMP URL + Key") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                            Spacer(Modifier.height(14.dp))
                            if (error.isNotEmpty()) Text(error, color = Color.Red)
                            Button(onClick = { if (startLive()) { isLive = true; screen = "studio" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A3A))) { Text("SAVE & GO LIVE") }
                            Spacer(Modifier.height(8.dp)); Button(onClick = { screen = "studio" }, modifier = Modifier.fillMaxWidth()) { Text("← BACK TO STUDIO") }
                        }
                    }
                }
            }
        }
    }
}
