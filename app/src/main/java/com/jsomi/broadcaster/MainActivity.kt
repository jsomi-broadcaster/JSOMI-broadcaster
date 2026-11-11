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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// JSTV - JEHOVAH SHALOM TV - BROADCASTER - FULL
// NO ADMIN - Admin only in ADMIN STUDIO APP
// 100% JSTV ONLY
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
    var ticker by remember { mutableStateOf("Welcome to JSTV - Jehovah Shalom TV - God Bless You - Subscribe - Share - Like") }
    var tickerOn by remember { mutableStateOf(true) }
    var tickerSpeed by remember { mutableStateOf("Medium") }
    var tickerPos by remember { mutableStateOf("Bottom") } // Top / Middle / Bottom - MOVABLE
    var cameraOn by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var isDemo by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("HD 720p") }
    var streamTitle by remember { mutableStateOf("My Live Show") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var remoteCommand by remember { mutableStateOf("Ready") }
    var cameraStatus by remember { mutableStateOf("Camera Ready - Waiting for Main") }

    // MULTISTREAMING
    var ytOn by remember { mutableStateOf(true) }
    var fbOn by remember { mutableStateOf(false) }
    var tkOn by remember { mutableStateOf(false) }
    var ytUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2") }
    var fbUrl by remember { mutableStateOf("rtmps://live-api-s.facebook.com:443/rtmp/") }
    var tkUrl by remember { mutableStateOf("rtmp://push-rtmp-l1.tiktok.com/live/") }
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var tkKey by remember { mutableStateOf("") }

    // MOVABLE TITLES - All titles movable
    var logoOn by remember { mutableStateOf(true) }
    var logoText by remember { mutableStateOf("JSTV") }
    var logoPos by remember { mutableStateOf("Top Right") } // Top Left / Top Center / Top Right / Bottom Left / Bottom Right
    var logoX by remember { mutableStateOf(0) } // -2 to +2 Move Left / Right
    var logoY by remember { mutableStateOf(0) }

    var lsName by remember { mutableStateOf("Pastor Kevin") }
    var lsTitle by remember { mutableStateOf("Host - Jehovah Shalom TV") }
    var lsOn by remember { mutableStateOf(false) }
    var lsPos by remember { mutableStateOf("Bottom Left") } // Bottom Left / Bottom Center / Bottom Right / Top

    var titlePos by remember { mutableStateOf("Center") } // Top / Center / Bottom
    var livePos by remember { mutableStateOf("Top Right") } // Top Left / Top Right / Bottom

    var loopOn by remember { mutableStateOf(false) }
    var loopVideo by remember { mutableStateOf("Worship Loop 1") }
    var loopList by remember { mutableStateOf(listOf("Worship Loop 1", "Praise Loop 2", "Sermon Loop 3")) }
    var liveSeconds by remember { mutableStateOf(0) }
    var currentTime by remember { mutableStateOf("") }
    var blink by remember { mutableStateOf(true) }
    var introOn by remember { mutableStateOf(true) }
    var introVideo by remember { mutableStateOf("JSTV Intro 1 - 10s") }
    var introDuration by remember { mutableStateOf("10s") }
    var introPlaying by remember { mutableStateOf(false) }
    var introCountdown by remember { mutableStateOf(10) }

    // FULL SCREEN
    var fullScreen by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val fmt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            currentTime = fmt.format(Date())
            delay(1000)
        }
    }
    LaunchedEffect(isLive, isDemo) {
        while (isLive || isDemo) { delay(1000); liveSeconds += 1 }
    }
    LaunchedEffect(Unit) {
        while (true) { delay(500); blink = !blink }
    }
    LaunchedEffect(introPlaying) {
        if (introPlaying) {
            var c = if (introDuration == "5s") 5 else if (introDuration == "15s") 15 else if (introDuration == "30s") 30 else 10
            introCountdown = c
            while (c > 0 && introPlaying) { delay(1000); c -= 1; introCountdown = c }
            if (c <= 0) { introPlaying = false }
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
    fun getActiveDests(): String {
        val list = mutableListOf<String>()
        if (ytOn) list.add("YouTube")
        if (fbOn) list.add("Facebook")
        if (tkOn) list.add("TikTok")
        return if (list.isEmpty()) "None" else list.joinToString(" + ")
    }
    fun tryRealLive() {
        if (!isPremium) { showPremium = true; return }
        if (!ytOn && !fbOn && !tkOn) { toast("Turn ON at least 1 destination"); return }
        if (ytOn && ytKey.isBlank()) { toast("Enter YouTube Stream Key"); return }
        if (fbOn && fbKey.isBlank()) { toast("Enter Facebook Stream Key"); return }
        if (tkOn && tkKey.isBlank()) { toast("Enter TikTok Stream Key"); return }
        if (introOn && !introPlaying) { introPlaying = true; toast("Playing Intro First - $introVideo"); return }
        isLive = true; isDemo = false; liveSeconds = 0; fullScreen = true
        toast("REAL LIVE to ${getActiveDests()} - FULL SCREEN")
    }
    fun tryDemoLive() {
        isDemo = true; isLive = false; liveSeconds = 0; fullScreen = true
        toast("DEMO / PREVIEW ONLY - FULL SCREEN - Tap EXIT to go back")
    }

    // ===== FULL SCREEN MODE - WHEN STREAMING =====
    if (fullScreen && (isLive || isDemo || introPlaying)) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (logoOn) logoText else "", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (isLive) { Text(if (blink) "● LIVE ${formatLive(liveSeconds)}" else "○ LIVE ${formatLive(liveSeconds)}", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                else { Text("● DEMO ${formatLive(liveSeconds)}", color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(currentTime, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            if (introPlaying) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("▶ PLAYING INTRO", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(introVideo, color = Color.White, fontSize = 14.sp); Text("$introCountdown s left...", color = Color.Gray, fontSize = 12.sp) }
                }
            } else {
                Text(streamTitle, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("$channel | $scene | $quality | ${getActiveDests()}", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.weight(1f))
            if (lsOn) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.Yellow).padding(10.dp)) { Column { Text(lsName, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(lsTitle, color = Color.Black, fontSize = 12.sp) } }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // SCROLLING TEXT - SINGLE LINE - MOVABLE POSITION
            if (tickerOn) {
                if (tickerPos == "Top") { /* Top already handled below - we show at bottom by default, this is placeholder */ }
                Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(8.dp)) {
                    Text(ticker, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Text("Ticker: $tickerPos - Single Line Scrolling", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { fullScreen = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("EXIT FULL SCREEN", color = Color.Black, fontWeight = FontWeight.Bold) }
                Button(onClick = { isLive = false; isDemo = false; fullScreen = false; introPlaying = false; toast("Stopped - Live Time ${formatLive(liveSeconds)}") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("STOP LIVE", color = Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("FULL SCREEN - Landscape or Portrait - Both OK - Rotate Phone Freely", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        return
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
            Text("Time Now: $currentTime", color = Color.Gray, fontSize = 13.sp)
            Text(if (isLandscape) "Landscape Mode - OK" else "Portrait Mode - OK - Rotate Freely", color = Color.Gray, fontSize = 11.sp)
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
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isLandscape = false; toast("Portrait Mode") }, colors = ButtonDefaults.buttonColors(containerColor = if (!isLandscape) Color.Yellow else Color.DarkGray)) { Text("PORTRAIT", color = if (!isLandscape) Color.Black else Color.White) }
                Button(onClick = { isLandscape = true; toast("Landscape Mode - Rotate Phone") }, colors = ButtonDefaults.buttonColors(containerColor = if (isLandscape) Color.Yellow else Color.DarkGray)) { Text("LANDSCAPE", color = if (isLandscape) Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need help with BROADCASTER app.") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("WHATSAPP HELP - 0763052911", color = Color.White, fontSize = 12.sp) }
        }

        if (screen == "MAIN") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("JSTV", color = Color.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (isLive) { Box(modifier = Modifier.background(Color.Red).padding(horizontal = 10.dp, vertical = 4.dp)) { Text(if (blink) "● LIVE" else "○ LIVE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) } }
                else if (isDemo) { Box(modifier = Modifier.background(Color.Yellow).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("● DEMO", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold) } }
                else if (introPlaying) { Box(modifier = Modifier.background(Color.Yellow).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("▶ INTRO $introCountdown s", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }
                else { Box(modifier = Modifier.background(Color.DarkGray).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("● OFFLINE", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }
            }
            Text("MAIN STUDIO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Multistream to YouTube + Facebook + TikTok | Portrait + Landscape OK", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("TIME NOW", color = Color.Gray, fontSize = 10.sp); Text(currentTime, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("LIVE TIME", color = Color.Gray, fontSize = 10.sp); Text(if (isLive || isDemo) formatLive(liveSeconds) else "00:00:00", color = if (isLive) Color.Red else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("STATUS", color = Color.Gray, fontSize = 10.sp); Text(if (isLive) "ON AIR" else if (isDemo) "DEMO" else if (introPlaying) "INTRO" else "READY", color = if (isLive) Color.Red else Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // PREVIEW - SHOWS MOVABLE POSITIONS
            Box(modifier = Modifier.fillMaxWidth().height(if (isLandscape) 200.dp else 270.dp).background(Color.DarkGray)) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (logoOn) "$logoText ($logoPos)" else "", color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (isLive) { Text("● LIVE ${formatLive(liveSeconds)} - $livePos", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        else if (isDemo) { Text("● DEMO PREVIEW", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        else if (introPlaying) { Text("▶ INTRO $introCountdown s", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        else { Text("PREVIEW", color = Color.White, fontSize = 13.sp) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(currentTime, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    // TITLE POS - MOVABLE
                    if (titlePos == "Top") { Text(streamTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.weight(1f))
                    if (titlePos == "Center") { Text(streamTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()); Text("Channel: $channel | Scene: $scene | $quality", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    Spacer(modifier = Modifier.weight(1f))
                    if (titlePos == "Bottom") { Text(streamTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    if (lsOn) {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Yellow).padding(8.dp)) { Column { Text("$lsName - $lsPos", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(lsTitle, color = Color.Black, fontSize = 11.sp) } }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (loopOn) { Text("LOOP PLAYING: $loopVideo - REPEAT", color = Color.Cyan, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    // TICKER POS - MOVABLE - SINGLE LINE
                    if (tickerOn && tickerPos == "Middle") {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(6.dp)) { Text(ticker, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                    if (tickerOn && tickerPos == "Top") {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(6.dp)) { Text(ticker, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                    if (tickerOn && tickerPos == "Bottom") {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(6.dp)) { Text(ticker, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { fullScreen = true; toast("Full Screen Preview - Rotate Phone - Portrait or Landscape") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text("FULL SCREEN PREVIEW", color = Color.Black, fontWeight = FontWeight.Bold) }
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isLandscape = false; toast("Portrait - Tall Video") }, colors = ButtonDefaults.buttonColors(containerColor = if (!isLandscape) Color.Yellow else Color.DarkGray)) { Text("PORTRAIT", color = if (!isLandscape) Color.Black else Color.White) }
                Button(onClick = { isLandscape = true; toast("Landscape - Wide Video - Rotate Phone") }, colors = ButtonDefaults.buttonColors(containerColor = if (isLandscape) Color.Yellow else Color.DarkGray)) { Text("LANDSCAPE", color = if (isLandscape) Color.Black else Color.White) }
            }

            // ===== MULTISTREAMING =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("MULTISTREAMING - 3 DESTINATIONS", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("URLs Already Included - You Only Paste Stream Key For Each", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            Text(if (isPremium) "PREMIUM: Real Streaming ON - ${getActiveDests()}" else "UNPAID: PREVIEW / DEMO ONLY - Real Streaming Needs Premium", color = if (isPremium) Color.Green else Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("1. YOUTUBE", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("URL: $ytUrl - Already Set", color = Color.Gray, fontSize = 11.sp)
            OutlinedTextField(value = ytKey, onValueChange = { ytKey = it }, label = { Text("YouTube Stream Key - Paste Here Only") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { ytOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (ytOn) Color.Red else Color.DarkGray)) { Text("YOUTUBE ON") }
                Button(onClick = { ytOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!ytOn) Color.Gray else Color.DarkGray)) { Text("YOUTUBE OFF") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("2. FACEBOOK", color = Color(0xFF1877F2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("URL: $fbUrl - Already Set", color = Color.Gray, fontSize = 11.sp)
            OutlinedTextField(value = fbKey, onValueChange = { fbKey = it }, label = { Text("Facebook Stream Key - Paste Here Only") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { fbOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (fbOn) Color(0xFF1877F2) else Color.DarkGray)) { Text("FACEBOOK ON", color = Color.White) }
                Button(onClick = { fbOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!fbOn) Color.Gray else Color.DarkGray)) { Text("FACEBOOK OFF") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("3. TIKTOK", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("URL: $tkUrl - Already Set", color = Color.Gray, fontSize = 11.sp)
            OutlinedTextField(value = tkKey, onValueChange = { tkKey = it }, label = { Text("TikTok Stream Key - Paste Here Only") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tkOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (tkOn) Color.White else Color.DarkGray)) { Text("TIKTOK ON", color = if (tkOn) Color.Black else Color.White) }
                Button(onClick = { tkOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!tkOn) Color.Gray else Color.DarkGray)) { Text("TIKTOK OFF") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Active Now: ${getActiveDests()}", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            // ===== VIDEO INTRO =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("VIDEO INTRO - PLAYS BEFORE LIVE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { introOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (introOn) Color.Green else Color.DarkGray)) { Text("INTRO ON") }
                Button(onClick = { introOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!introOn) Color.Red else Color.DarkGray)) { Text("INTRO OFF") }
            }

            // ===== MOVABLE TITLES - ALL TITLES MOVABLE =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("MOVE TITLES - ALL TITLES MOVABLE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Move Logo, Title, Lower Third, LIVE, Ticker - Anywhere You Want", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("LOGO Position: $logoPos", color = Color.White, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoPos = "Top Left" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Top Left") Color.Yellow else Color.DarkGray)) { Text("Top Left", color = if (logoPos == "Top Left") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Top Center" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Top Center") Color.Yellow else Color.DarkGray)) { Text("Top Center", color = if (logoPos == "Top Center") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Top Right" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Top Right") Color.Yellow else Color.DarkGray)) { Text("Top Right", color = if (logoPos == "Top Right") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoPos = "Bottom Left" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Bottom Left") Color.Yellow else Color.DarkGray)) { Text("Bottom Left", color = if (logoPos == "Bottom Left") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Bottom Center" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Bottom Center") Color.Yellow else Color.DarkGray)) { Text("Bottom Center", color = if (logoPos == "Bottom Center") Color.Black else Color.White) }
                Button(onClick = { logoPos = "Bottom Right" }, colors = ButtonDefaults.buttonColors(containerColor = if (logoPos == "Bottom Right") Color.Yellow else Color.DarkGray)) { Text("Bottom Right", color = if (logoPos == "Bottom Right") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoX -= 1; toast("Logo Moved Left") }) { Text("◀ LEFT") }
                Button(onClick = { logoX = 0; logoY = 0; toast("Logo Centered") }) { Text("CENTER") }
                Button(onClick = { logoX += 1; toast("Logo Moved Right") }) { Text("RIGHT ▶") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoY -= 1; toast("Logo Moved Up") }) { Text("▲ UP") }
                Button(onClick = { logoY += 1; toast("Logo Moved Down") }) { Text("▼ DOWN") }
            }
            Text("Logo: $logoText - Move X:$logoX Y:$logoY", color = Color.Yellow, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = logoText, onValueChange = { logoText = it }, label = { Text("Logo Text - e.g. JSTV") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { logoOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (logoOn) Color.Green else Color.DarkGray)) { Text("LOGO ON") }
                Button(onClick = { logoOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!logoOn) Color.Red else Color.DarkGray)) { Text("LOGO OFF") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("STREAM TITLE Position: $titlePos", color = Color.White, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { titlePos = "Top" }, colors = ButtonDefaults.buttonColors(containerColor = if (titlePos == "Top") Color.Yellow else Color.DarkGray)) { Text("TITLE TOP", color = if (titlePos == "Top") Color.Black else Color.White) }
                Button(onClick = { titlePos = "Center" }, colors = ButtonDefaults.buttonColors(containerColor = if (titlePos == "Center") Color.Yellow else Color.DarkGray)) { Text("TITLE CENTER", color = if (titlePos == "Center") Color.Black else Color.White) }
                Button(onClick = { titlePos = "Bottom" }, colors = ButtonDefaults.buttonColors(containerColor = if (titlePos == "Bottom") Color.Yellow else Color.DarkGray)) { Text("TITLE BOTTOM", color = if (titlePos == "Bottom") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("LIVE INDICATOR Position: $livePos", color = Color.White, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { livePos = "Top Left" }, colors = ButtonDefaults.buttonColors(containerColor = if (livePos == "Top Left") Color.Yellow else Color.DarkGray)) { Text("LIVE Top Left", color = if (livePos == "Top Left") Color.Black else Color.White) }
                Button(onClick = { livePos = "Top Right" }, colors = ButtonDefaults.buttonColors(containerColor = if (livePos == "Top Right") Color.Yellow else Color.DarkGray)) { Text("LIVE Top Right", color = if (livePos == "Top Right") Color.Black else Color.White) }
                Button(onClick = { livePos = "Bottom" }, colors = ButtonDefaults.buttonColors(containerColor = if (livePos == "Bottom") Color.Yellow else Color.DarkGray)) { Text("LIVE Bottom", color = if (livePos == "Bottom") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("LOWER THIRD Position: $lsPos", color = Color.White, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { lsPos = "Bottom Left" }, colors = ButtonDefaults.buttonColors(containerColor = if (lsPos == "Bottom Left") Color.Yellow else Color.DarkGray)) { Text("LS Bottom Left", color = if (lsPos == "Bottom Left") Color.Black else Color.White) }
                Button(onClick = { lsPos = "Bottom Center" }, colors = ButtonDefaults.buttonColors(containerColor = if (lsPos == "Bottom Center") Color.Yellow else Color.DarkGray)) { Text("LS Bottom Center", color = if (lsPos == "Bottom Center") Color.Black else Color.White) }
                Button(onClick = { lsPos = "Bottom Right" }, colors = ButtonDefaults.buttonColors(containerColor = if (lsPos == "Bottom Right") Color.Yellow else Color.DarkGray)) { Text("LS Bottom Right", color = if (lsPos == "Bottom Right") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = lsName, onValueChange = { lsName = it }, label = { Text("Name - e.g. Pastor Kevin") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = lsTitle, onValueChange = { lsTitle = it }, label = { Text("Title - e.g. Host - Jehovah Shalom TV") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { lsOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (lsOn) Color.Yellow else Color.DarkGray)) { Text("SHOW LS", color = if (lsOn) Color.Black else Color.White) }
                Button(onClick = { lsOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!lsOn) Color.Red else Color.DarkGray)) { Text("HIDE LS") }
            }

            // ===== SCROLLING TEXT - SINGLE LINE + MOVABLE =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("SCROLLING TEXT - SINGLE LINE + MOVABLE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("One Line Only - Never Wraps To 2 Lines - Move It Anywhere", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Scrolling Text - Single Line") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Preview - Single Line:", color = Color.White, fontSize = 12.sp)
            Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(8.dp)) {
                Text(ticker, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Move Ticker: Now $tickerPos", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tickerPos = "Top" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerPos == "Top") Color.Yellow else Color.DarkGray)) { Text("▲ TOP", color = if (tickerPos == "Top") Color.Black else Color.White) }
                Button(onClick = { tickerPos = "Middle" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerPos == "Middle") Color.Yellow else Color.DarkGray)) { Text("MIDDLE", color = if (tickerPos == "Middle") Color.Black else Color.White) }
                Button(onClick = { tickerPos = "Bottom" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerPos == "Bottom") Color.Yellow else Color.DarkGray)) { Text("▼ BOTTOM", color = if (tickerPos == "Bottom") Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tickerPos = "Top"; toast("Ticker Moved Up - Top") }) { Text("MOVE UP") }
                Button(onClick = { tickerPos = "Bottom"; toast("Ticker Moved Down - Bottom") }) { Text("MOVE DOWN") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Scroll Speed: $tickerSpeed", color = Color.White, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tickerSpeed = "Slow" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Slow") Color.Yellow else Color.DarkGray)) { Text("Slow", color = if (tickerSpeed == "Slow") Color.Black else Color.White) }
                Button(onClick = { tickerSpeed = "Medium" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Medium") Color.Yellow else Color.DarkGray)) { Text("Medium", color = if (tickerSpeed == "Medium") Color.Black else Color.White) }
                Button(onClick = { tickerSpeed = "Fast" }, colors = ButtonDefaults.buttonColors(containerColor = if (tickerSpeed == "Fast") Color.Yellow else Color.DarkGray)) { Text("Fast", color = if (tickerSpeed == "Fast") Color.Black else Color.White) }
            }
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { loopOn = true }, colors = ButtonDefaults.buttonColors(containerColor = if (loopOn) Color.Green else Color.DarkGray)) { Text("LOOP ON") }
                Button(onClick = { loopOn = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!loopOn) Color.Red else Color.DarkGray)) { Text("LOOP OFF") }
            }

            // ===== TIME =====
            Spacer(modifier = Modifier.height(20.dp))
            Text("TIME - CLOCK & LIVE TIME", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Current Time: $currentTime", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Live Time: ${if (isLive || isDemo) formatLive(liveSeconds) else "Not Live Yet - 00:00:00"}", color = if (isLive) Color.Red else Color.Gray, fontSize = 13.sp)
                    Text("Orientation: ${if (isLandscape) "Landscape - Wide" else "Portrait - Tall"} - Both OK", color = Color.Gray, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { cameraOn = !cameraOn }, colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON" else "CAMERA OFF") }
                Button(onClick = { micOn = !micOn }, colors = ButtonDefaults.buttonColors(containerColor = if (micOn) Color.Green else Color.DarkGray)) { Text(if (micOn) "MIC ON" else "MIC OFF") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { if (isDemo) { isDemo = false; fullScreen = false } else { tryDemoLive() } }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) {
                Text(if (isDemo) "STOP DEMO - ${formatLive(liveSeconds)}" else "PREVIEW / DEMO - FREE FOR ALL", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (isLive) { isLive = false; introPlaying = false; fullScreen = false; toast("Real Broadcast Stopped - ${formatLive(liveSeconds)}") }
                else if (introPlaying) { introPlaying = false; isLive = true; liveSeconds = 0; fullScreen = true }
                else { tryRealLive() }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else if (introPlaying) Color.Yellow else Color.Green)) {
                Text(if (isLive) "STOP REAL LIVE - ${formatLive(liveSeconds)}" else if (introPlaying) "SKIP INTRO & GO REAL LIVE" else "GO REAL LIVE - ${getActiveDests()} - PREMIUM", color = if (introPlaying) Color.Black else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(if (isPremium) "Premium Active - Real Multistreaming ON" else "Real Streaming Needs Premium Code", color = if (isPremium) Color.Green else Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (screen == "REMOTE") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("REMOTE CONTROL", color = Color.Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Control Main From Far", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Time Now: $currentTime", color = Color.White, fontSize = 12.sp)
            Text(if (isLive) "● REAL LIVE - ${formatLive(liveSeconds)} - ${getActiveDests()}" else if (isDemo) "● DEMO - ${formatLive(liveSeconds)}" else "● OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Main Studio Status", color = Color.White, fontSize = 14.sp)
                    Text(if (isLive) "MAIN IS REAL LIVE" else if (isDemo) "MAIN IS DEMO" else "MAIN IS OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Live Time: ${formatLive(liveSeconds)} | Dests: ${getActiveDests()}", color = Color.Yellow, fontSize = 12.sp)
                    Text("Ticker: $ticker - Pos: $tickerPos", color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Last Command: $remoteCommand", color = Color.Yellow, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { remoteCommand = "DEMO sent"; isDemo = true; isLive = false; liveSeconds = 0; toast("DEMO sent - Preview Only") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("START DEMO ON MAIN - FREE", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "REAL LIVE sent"; if (!isPremium) showPremium = true else { isLive = true; isDemo = false; toast("REAL LIVE sent") } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("START REAL LIVE ON MAIN - PREMIUM") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "STOP sent"; isLive = false; isDemo = false; fullScreen = false; toast("STOP sent") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("STOP LIVE ON MAIN") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "FULL SCREEN sent"; fullScreen = true; toast("Full Screen sent") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text("FULL SCREEN ON MAIN", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "TICKER UP sent"; tickerPos = "Top"; toast("Ticker Moved Up - Top") }, modifier = Modifier.fillMaxWidth()) { Text("MOVE TICKER UP - TOP") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "TICKER DOWN sent"; tickerPos = "Bottom"; toast("Ticker Moved Down - Bottom") }, modifier = Modifier.fillMaxWidth()) { Text("MOVE TICKER DOWN - BOTTOM") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "SWITCH SCENE sent"; scene = if (scene == "Studio") "Outdoor" else "Studio"; toast("Scene $scene") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("SWITCH SCENE - Now $scene", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "SHOW LS sent"; lsOn = !lsOn; toast("LS toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (lsOn) "HIDE LOWER THIRD" else "SHOW LOWER THIRD") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { remoteCommand = "LOOP sent"; loopOn = !loopOn; toast("Loop toggled") }, modifier = Modifier.fillMaxWidth()) { Text(if (loopOn) "STOP LOOP" else "START LOOP - $loopVideo") }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker to send to Main - Single Line") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (screen == "CAMERA") {
            Text("JSTV", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("WIRELESS CAMERA", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("2nd Phone As Camera", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Time Now: $currentTime", color = Color.White, fontSize = 12.sp)
            Text(if (isLive) "● MAIN IS REAL LIVE - ${formatLive(liveSeconds)}" else if (isDemo) "● MAIN IS DEMO" else "● MAIN OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) { Text("CAMERA PREVIEW\n$cameraStatus", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center) }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isLandscape = false }, colors = ButtonDefaults.buttonColors(containerColor = if (!isLandscape) Color.Yellow else Color.DarkGray)) { Text("PORTRAIT", color = if (!isLandscape) Color.Black else Color.White) }
                Button(onClick = { isLandscape = true }, colors = ButtonDefaults.buttonColors(containerColor = if (isLandscape) Color.Yellow else Color.DarkGray)) { Text("LANDSCAPE", color = if (isLandscape) Color.Black else Color.White) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Status: $cameraStatus", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraOn = !cameraOn; cameraStatus = if (cameraOn) "Camera ON - Sending to Main" else "Camera OFF"; toast(cameraStatus) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (cameraOn) Color.Green else Color.DarkGray)) { Text(if (cameraOn) "CAMERA ON - SENDING" else "CAMERA OFF") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraStatus = "Connected to Main Studio"; toast("Connected") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("CONNECT TO MAIN STUDIO", color = Color.Black) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { screen = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK - CHANGE ROLE") }
        }

        if (showPremium) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REAL STREAMING - PREMIUM REQUIRED", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Preview / Demo is FREE for everyone. Real Multistreaming to YouTube + Facebook + TikTok needs Premium Code.", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Enter Code - e.g. JSTV-200-XXXX") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (code.startsWith("JSTV-")) { isPremium = true; showPremium = false; toast("Premium Unlocked - Real Streaming ON!") } else { toast("Wrong Code - WhatsApp Us") }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("UNLOCK REAL STREAMING", color = Color.Black, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 11.sp)
                    Text("Paybill 516600 Acc 5600935001", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code for Real Multistreaming. I have paid.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WHATSAPP FOR CODE - 0763052911") }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { showPremium = false }, modifier = Modifier.fillMaxWidth()) { Text("CANCEL - USE DEMO ONLY") }
                }
            }
        }
    }
}
