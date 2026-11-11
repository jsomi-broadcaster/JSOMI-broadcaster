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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// JSTV - JEHOVAH SHALOM TV - BROADCASTER - FULL
// NO ADMIN - Admin only in ADMIN STUDIO APP
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
    var ticker by remember { mutableStateOf("Welcome to JSTV - Jehovah Shalom TV - God Bless You") }
    var tickerOn by remember { mutableStateOf(true) }
    var tickerSpeed by remember { mutableStateOf("Medium") }
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

    // STREAMING
    var dest by remember { mutableStateOf("YouTube") }
    var streamUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2") }
    var streamKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }

    // LOGO
    var logoOn by remember { mutableStateOf(true) }
    var logoText by remember { mutableStateOf("JSTV") }
    var logoPos by remember { mutableStateOf("Top Right") }

    // TITLE LS
    var lsName by remember { mutableStateOf("Pastor Kevin") }
    var lsTitle by remember { mutableStateOf("Host - Jehovah Shalom TV") }
    var lsOn by remember { mutableStateOf(false) }

    // LOOPS
    var loopOn by remember { mutableStateOf(false) }
    var loopVideo by remember { mutableStateOf("Worship Loop 1") }
    var loopList by remember { mutableStateOf(listOf("Worship Loop 1", "Praise Loop 2", "Sermon Loop 3")) }

    // NEW - LIVE INDICATOR + TIME
    var liveSeconds by remember { mutableStateOf(0) }
    var currentTime by remember { mutableStateOf("") }
    var blink by remember { mutableStateOf(true) }

    // NEW - VIDEO INTRO
    var introOn by remember { mutableStateOf(true) }
    var introVideo by remember { mutableStateOf("JSTV Intro 1 - 10s") }
    var introDuration by remember { mutableStateOf("10s") }
    var introPlaying by remember { mutableStateOf(false) }
    var introCountdown by remember { mutableStateOf(10) }

    // CLOCK - Updates every 1 sec
    LaunchedEffect(Unit) {
        while (true) {
            val fmt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            currentTime = fmt.format(Date())
            delay(1000)
        }
    }
    // LIVE TIMER - Counts when LIVE
    LaunchedEffect(isLive) {
        while (isLive) {
            delay(1000)
            liveSeconds += 1
        }
    }
    // BLINK - For LIVE dot
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            blink = !blink
        }
    }
    // INTRO COUNTDOWN
    LaunchedEffect(introPlaying) {
        if (introPlaying) {
            var c = if (introDuration == "5s") 5 else if (introDuration == "15s") 15 else if (introDuration == "30s") 30 else 10
            introCountdown = c
            while (c > 0 && introPlaying) {
                delay(1000)
                c -= 1
                introCountdown = c
            }
            if (c <= 0) {
                introPlaying = false
            }
        }
    }

    fun formatLive(s: Int): String {
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return String.format("%02d:%02d:%02d", h, m, sec)
    }
    fun toast(m: String) { Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(msg: String) {
        val num = "254763052911"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + num + "?text=" + Uri.encode(msg)))
        try { ctx.startActivity(i) } catch (e: Exception) { toast("WhatsApp not found") }
    }
    fun shareApp() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, "Download JSTV - Jehovah Shalom TV - BROADCASTER App. WhatsApp 0763052911 for link.")
        try { ctx.startActivity(Intent.createChooser(i, "Share JSTV App")) } catch (e: Exception) { toast("Cannot share") }
    }
    fun tryGoLive() {
        if (isPremium) {
            if (streamKey.isBlank()) { toast("Enter Stream Key First - See STREAMING SETUP below"); return }
            if (introOn && !introPlaying) {
                introPlaying = true
                toast("Playing Intro First - $introVideo - Then LIVE")
                return
            }
            isLive = true
            liveSeconds = 0
            toast("You Are LIVE to $dest - $channel")
        } else { showPremium = true }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (screen == "CHOOSE") {
            Spacer(modifier = Modifier.height(80.dp))
            Text("JSTV", color = Color.Yellow, fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text("JEHOVAH SHALOM TV", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            // TIME ON CHOOSE SCREEN TOO
            Text("Time Now: $currentTime", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Who Are You? Choose Your Phone Role:", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { screen = "MAIN" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MAIN STUDIO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Goes LIVE to YouTube", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { screen = "REMOTE" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("REMOTE CONTROL", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Control Main From Far", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { screen = "CAMERA" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green), shape = MaterialTheme.shapes.extraLarge) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("WIRELESS CAMERA", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("2nd Phone As Camera", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Same App On All Phones - Just Choose Different Mode", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { shareApp() }, modifier = Modifier.fillMaxWidth(0.85f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B6BB0)), shape = MaterialTheme.shapes.extraLarge) { Text("SHARE JSTV APP TO 2ND PHONE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need help with BROADCASTER app.") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("WHATSAPP HELP - 0763052911", color = Color.White, fontSize = 12.sp) }
        }

        if (screen == "MAIN") {
            // ===== TOP - LIVE INDICATOR + TIME =====
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("JSTV", color = Color.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (isLive) {
                    Box(modifier = Modifier.background(Color.Red).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(if (blink) "● LIVE" else "○ LIVE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (introPlaying) {
                    Box(modifier = Modifier.background(Color.Yellow).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("▶ INTRO $introCountdown s", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                } else {
                    Box(modifier = Modifier.background(Color.DarkGray).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("● OFFLINE", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Text("MAIN STUDIO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Goes LIVE to YouTube", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            // TIME BAR
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("TIME NOW", color = Color.Gray, fontSize = 10.sp); Text(currentTime, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("LIVE TIME", color = Color.Gray, fontSize = 10.sp); Text(if (isLive) formatLive(liveSeconds) else "00:00:00", color = if (isLive) Color.Red else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("STATUS", color = Color.Gray, fontSize = 10.sp); Text(if (isLive) "ON AIR" else if (introPlaying) "INTRO" else "READY", color = if (isLive) Color.Red else Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // PREVIEW WITH LIVE INDICATOR + INTRO + TIME OVERLAY
            Box(modifier = Modifier.fillMaxWidth().height(270.dp).background(Color.DarkGray)) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (logoOn) logoText else "", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (isLive) { Text(if (blink) "● LIVE ${formatLive(liveSeconds)}" else "● LIVE ${formatLive(liveSeconds)}", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        else if (introPlaying) { Text("▶ INTRO PLAYING $introCountdown s", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        else { Text("PREVIEW", color = Color.White, fontSize = 13.sp) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(currentTime, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    if (introPlaying) {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("▶ PLAYING INTRO", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(introVideo, color = Color.White, fontSize = 13.sp); Text("$introCountdown seconds left...", color = Color.Gray, fontSize = 12.sp) }
                        }
                    } else {
                        Text(streamTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("Channel: $channel | Scene: $scene | $quality", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("Camera: ${if (cameraOn) "ON" else "OFF"} | Mic: ${if (micOn) "ON" else "OFF"} | Logo: $logoPos", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (lsOn) {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Yellow).padding(8.dp)) { Column { Text(lsName, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(lsTitle, color = Color.Black, fontSize = 11.sp) } }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (loopOn) { Text("LOOP PLAYING: $loopVideo - REPEAT", color = Color.Cyan, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    if (tickerOn) { Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(6.dp)) { Text("SCROLLING >>> $ticker >>> $ticker", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center) } }
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

            // ===== STREAMING SETUP =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("STREAMING SETUP - WHERE TO GO LIVE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Paste your Stream Key from YouTube / Facebook / TikTok", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { dest = "YouTube"; streamUrl = "rtmp://a.rtmp.youtube.com/live2" }, colors = ButtonDefaults.buttonColors(containerColor = if (dest == "YouTube") Color.Red else Color.DarkGray)) { Text("YouTube", color = Color.White) }
                Button(onClick = { dest = "Facebook"; streamUrl = "rtmps://live-api-s.facebook.com:443/rtmp/" }, colors = ButtonDefaults.buttonColors(containerColor = if (dest == "Facebook") Color(0xFF1877F2) else Color.DarkGray)) { Text("Facebook", color = Color.White) }
                Button(onClick = { dest = "TikTok"; streamUrl = "rtmp://push-rtmp-l1.tiktok.com/live/" }, colors = ButtonDefaults.buttonColors(containerColor = if (dest == "TikTok") Color.White else Color.DarkGray)) { Text("TikTok", color = if (dest == "TikTok") Color.Black else Color.White) }
                Button(onClick = { dest = "Custom RTMP"; streamUrl = "" }, colors = ButtonDefaults.buttonColors(containerColor = if (dest == "Custom RTMP") Color.Yellow else Color.DarkGray)) { Text("Custom", color = if (dest == "Custom RTMP") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected: $dest", color = Color.Yellow, fontSize = 12.sp)
            OutlinedTextField(value = streamUrl, onValueChange = { streamUrl = it }, label = { Text("Stream URL / RTMP URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = if (showKey) streamKey else "*".repeat(streamKey.length), onValueChange = { if (showKey) streamKey = it else streamKey = it }, label = { Text("STREAM KEY - Paste Here - Keep Secret") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { showKey = !showKey }) { Text(if (showKey) "HIDE KEY" else "SHOW KEY") }
                Button(onClick = { streamKey = ""; toast("Stream Key Cleared") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("CLEAR KEY") }
            }
            Text(if (streamKey.isBlank()) "No Stream Key Yet - Paste from YouTube Studio > Go Live > Stream Key" else "Stream Key Saved - Ready to Go Live to $dest", color = if (streamKey.isBlank()) Color.Red else Color.Green, fontSize = 11.sp)

            // ===== VIDEO INTRO =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("VIDEO INTRO - PLAYS BEFORE LIVE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Intro plays first, then you go LIVE automatically", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { introVideo = "JSTV Intro 1 - 10s"; introDuration = "10s" }, colors = ButtonDefaults.buttonColors(containerColor = if (introVideo == "JSTV Intro 1 - 10s") Color.Yellow else Color.DarkGray)) { Text("Intro 1 - 10s", color = if (introVideo == "JSTV Intro 1 - 10s") Color.Black else Color.White) }
                Button(onClick = { introVideo = "Worship Intro - 15s"; introDuration = "15s" }, colors = ButtonDefaults.buttonColors(containerColor = if (introVideo == "Worship Intro - 15s") Color.Yellow else Color.DarkGray)) { Text("Worship - 15s", color = if (introVideo == "Worship Intro - 15s") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { introVideo = "News Intro - 5s"; introDuration = "5s" }, colors = ButtonDefaults.buttonColors(containerColor = if (introVideo == "News Intro - 5s") Color.Yellow else Color.DarkGray)) { Text("News - 5s", color = if (introVideo == "News Intro - 5s") Color.Black else Color.White) }
                Button(onClick = { introVideo = "Church Intro - 30s"; introDuration = "30s" }, colors = ButtonDefaults.buttonColors(containerColor = if (introVideo == "Church Intro - 30s") Color.Yellow else Color.DarkGray)) { Text("Church - 30s", color = if (introVideo == "Church Intro - 30s") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected Intro: $introVideo - Duration: $introDuration", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { introOn = true; toast("Intro ON") }, colors = ButtonDefaults.buttonColors(containerColor = if (introOn) Color.Green else Color.DarkGray)) { Text("INTRO ON") }
                Button(onClick = { introOn = false; toast("Intro OFF - Go Live Direct") }, colors = ButtonDefaults.buttonColors(containerColor = if (!introOn) Color.Red else Color.DarkGray)) { Text("INTRO OFF") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = { if (!introPlaying) { introPlaying = true; toast("Preview Intro - $introVideo") } else { introPlaying = false } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text(if (introPlaying) "STOP INTRO PREVIEW - $introCountdown s" else "PREVIEW INTRO NOW", color = Color.Black) }

            // ===== LOGO =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("LOGO OVERLAY", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = logoText, onValueChange = { logoText = it }, label = { Text("Logo Text - e.g. JSTV") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (logoOn) Color.Green else Color.DarkGray)) { Text("LOGO ON") }
                Button(onClick = { logoOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!logoOn) Color.Red else Color.DarkGray)) { Text("LOGO OFF") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Logo Position", color = Color.White, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoPos = "Top Left" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Top Left") Color.Yellow else Color.DarkGray)) { Text("Top Left", color = if (logoPos == "Top Left") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Top Right" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Top Right") Color.Yellow else Color.DarkGray)) { Text("Top Right", color = if (logoPos == "Top Right") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Bottom" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Bottom") Color.Yellow else Color.DarkGray)) { Text("Bottom", color = if (logoPos == "Bottom") Color.Black else Color.White) }
            }

            // ===== TITLE LS =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("TITLE LS - LOWER THIRD", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = lsName, onValueChange = { lsName = it }, label = { Text("Name - e.g. Pastor Kevin") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = lsTitle, onValueChange = { lsTitle = it }, label = { Text("Title - e.g. Host - Jehovah Shalom TV") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { lsOn = true; toast("Lower Third SHOWING") }, colors = ButtonDefaults.buttonColors(containerColor = if (lsOn) Color.Yellow else Color.DarkGray)) { Text("SHOW LS", color = if (lsOn) Color.Black else Color.White) }
                Button(onClick = { lsOn = false; toast("Lower Third Hidden") }, colors = ButtonDefaults.buttonColors(containerColor = if (!lsOn) Color.Red else Color.DarkGray)) { Text("HIDE LS") }
            }

            // ===== TICKER =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("SCROLLING WORDS - TICKER", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Scrolling Words - e.g. Welcome to JSTV") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(8.dp)) { Text(">>> $ticker >>> $ticker", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center) }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Scroll Speed", color = Color.White, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tickerSpeed = "Slow" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Slow") Color.Yellow else Color.DarkGray)) { Text("Slow", color = if (tickerSpeed == "Slow") Color.Black else Color.White) }
                Button(onClick = { tickerSpeed = "Medium" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Medium") Color.Yellow else Color.DarkGray)) { Text("Medium", color = if (tickerSpeed == "Medium") Color.Black else Color.White) }
                Button(onClick = { tickerSpeed = "Fast" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Fast") Color.Yellow else Color.DarkGray)) { Text("Fast", color = if (tickerSpeed == "Fast") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tickerOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerOn) Color.Green else Color.DarkGray)) { Text("TICKER ON") }
                Button(onClick = { tickerOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!tickerOn) Color.Red else Color.DarkGray)) { Text("TICKER OFF") }
            }

            // ===== LOOPS =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("LOOPS - LOOP VIDEOS", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            loopList.forEach { v ->
                Button(onClick = { loopVideo = v; toast("Loop Selected: $v") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (loopVideo == v) Color.Cyan else Color.DarkGray)) { Text(v, color = if (loopVideo == v) Color.Black else Color.White) }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { loopOn = true; toast("Loop ON - $loopVideo Repeating") }, colors = ButtonDefaults.buttonColors(containerColor = if (loopOn) Color.Green else Color.DarkGray)) { Text("LOOP ON") }
                Button(onClick = { loopOn = false; toast("Loop OFF") }, colors = ButtonDefaults.buttonColors(containerColor = if (!loopOn) Color.Red else Color.DarkGray)) { Text("LOOP OFF") }
            }
            Text(if (loopOn) "Now Looping: $loopVideo - Will Repeat" else "Loop Off", color = if (loopOn) Color.Cyan else Color.Gray, fontSize = 12.sp)

            // ===== TIME SETTINGS =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("TIME - CLOCK & LIVE TIME", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Current Time: $currentTime", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Live Time: ${if (isLive) formatLive(liveSeconds) else "Not Live Yet - 00:00:00"}", color = if (isLive) Color.Red else Color.Gray, fontSize = 13.sp)
                    Text("Intro Time: $introDuration | Loop: ${if (loopOn) "ON" else "OFF"}", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { cameraOn = !cameraOn }, colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON" else "CAMERA OFF") }
                Button(onClick = { micOn = !micOn }, colors = ButtonDefaults.buttonColors(containerColor = if (micOn) Color.Green else Color.DarkGray)) { Text(if (micOn) "MIC ON" else "MIC OFF") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isLive) { isLive = false; introPlaying = false; toast("Broadcast Stopped - Live Time Was ${formatLive(liveSeconds)}") }
                else if (introPlaying) { introPlaying = false; isLive = true; liveSeconds = 0; toast("Intro Skipped - You Are LIVE!") }
                else { tryGoLive() }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else if (introPlaying) Color.Yellow else Color.Green)) {
                Text(if (isLive) "STOP LIVE - ${formatLive(liveSeconds)}" else if (introPlaying) "SKIP INTRO & GO LIVE" else "GO LIVE to $dest", color = if (introPlaying) Color.Black else Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Try all features free. Premium only when you press GO LIVE.", color = Color.Gray, fontSize = 11.sp)
            if (isPremium) { Text("Premium Active - Enjoy", color = Color.Green, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (screen == "REMOTE") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("REMOTE CONTROL", color = Color.Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Control Main From Far", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Time Now: $currentTime", color = Color.White, fontSize = 12.sp)
            Text(if (isLive) "● LIVE - ${formatLive(liveSeconds)}" else "● OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Main Studio Status", color = Color.White, fontSize = 14.sp)
                    Text(if (isLive) "MAIN IS LIVE" else "MAIN IS OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Live Time: ${formatLive(liveSeconds)} | Intro: ${if (introPlaying) "PLAYING $introCountdown s" else "OFF"}", color = Color.Yellow, fontSize = 12.sp)
                    Text("Last Command: $remoteCommand", color = Color.Yellow, fontSize = 12.sp)
                    Text("Ticker: $ticker", color = Color.White, fontSize = 11.sp)
                    Text("Loop: ${if (loopOn) loopVideo else "OFF"} | LS: ${if (lsOn) lsName else "OFF"}", color = Color.White, fontSize = 11.sp)
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
            Button(onClick = { remoteCommand = "SHOW LS sent"; lsOn = !lsOn; toast("LS toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (lsOn) "HIDE LOWER THIRD" else "SHOW LOWER THIRD") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "LOOP sent"; loopOn = !loopOn; toast("Loop toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (loopOn) "STOP LOOP" else "START LOOP - $loopVideo") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "INTRO sent"; introPlaying = !introPlaying; toast("Intro toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (introPlaying) "STOP INTRO" else "PLAY INTRO - $introVideo") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "TICKER UPDATE sent"; toast("Ticker sent") }, modifier = Modifier.fillMaxWidth()) { Text("UPDATE TICKER ON MAIN") }
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker to send to Main") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (screen == "CAMERA") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("WIRELESS CAMERA", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("2nd Phone As Camera", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Time Now: $currentTime", color = Color.White, fontSize = 12.sp)
            Text(if (isLive) "● MAIN IS LIVE - ${formatLive(liveSeconds)}" else "● MAIN OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) { Text("CAMERA PREVIEW\n$cameraStatus", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center) }
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
                        if (code.startsWith("JSTV-")) { isPremium = true; showPremium = false; isLive = true; liveSeconds = 0; toast("Premium Unlocked - You Are LIVE!") } else { toast("Wrong Code - WhatsApp Us") }
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
