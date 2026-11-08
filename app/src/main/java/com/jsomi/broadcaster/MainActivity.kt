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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.Image

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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

                var showSettings by remember { mutableStateOf(false) }
                var settingsTab by remember { mutableStateOf(0) } // 0 Video, 1 Audio, 2 Scene, 3 Dest, 4 Chat, 5 Record

                // 1. VIDEO & ENCODER
                var resolution by remember { mutableStateOf("1080p") }
                var fps by remember { mutableStateOf("30fps") }
                var bitrate by remember { mutableStateOf("6000 Kbps") }
                var codec by remember { mutableStateOf("H.264") }
                var encoderType by remember { mutableStateOf("Hardware") } // Hardware = phone GPU, Software = CPU x264
                var aspect by remember { mutableStateOf("16:9 Horizontal") } // 16:9, 9:16, 1:1
                var layoutMode by remember { mutableStateOf("Full Camera") }

                // 2. AUDIO
                var micOn by remember { mutableStateOf(true) }
                var micSource by remember { mutableStateOf("Phone Mic") }
                var sampleRate by remember { mutableStateOf("48kHz") }
                var channels by remember { mutableStateOf("Stereo") }
                var systemAudioOn by remember { mutableStateOf(false) }
                var noiseSuppression by remember { mutableStateOf(true) }
                var micGain by remember { mutableStateOf(70f) }
                var compressorOn by remember { mutableStateOf(false) }
                var smartMute by remember { mutableStateOf(false) }

                // 3. SCENE / OVERLAY
                var showLogoOnVideo by remember { mutableStateOf(true) }
                var brandColor by remember { mutableStateOf("Gold") }
                var phoneNumber by remember { mutableStateOf("+254 700 000 000") }
                var topic by remember { mutableStateOf("Sunday Service - Jesus Is Lord") }
                var theme by remember { mutableStateOf("JEHOVAH SHALOM") }
                var tickerOn by remember { mutableStateOf(true) }
                var tickerText by remember { mutableStateOf("WELCOME TO JSTV - JEHOVAH SHALOM OUTREACH MINISTRIES") }
                var lowerThird by remember { mutableStateOf("JEHOVAH SHALOM OUTREACH MINISTRIES") }
                var showLowerThird by remember { mutableStateOf(true) }
                var chromaKeyOn by remember { mutableStateOf(false) }
                var virtualBgOn by remember { mutableStateOf(false) }
                var alertsOn by remember { mutableStateOf(true) }
                var chatBoxOn by remember { mutableStateOf(true) }
                var scoreboardOn by remember { mutableStateOf(false) }
                var scoreText by remember { mutableStateOf("JSTV 0 - 0 GUEST") }
                var logoOffset by remember { mutableStateOf(Offset(0f, 0f)) }
                var topicOffset by remember { mutableStateOf(Offset(0f, 0f)) }
                var phoneOffset by remember { mutableStateOf(Offset(0f, 0f)) }
                var lowerOffset by remember { mutableStateOf(Offset(0f, 0f)) }
                var loopOn by remember { mutableStateOf(false) }
                var loopVideoUri by remember { mutableStateOf<Uri?>(null) }
                var loopVideoName by remember { mutableStateOf("No video selected") }
                val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> if (uri != null) { loopVideoUri = uri; loopVideoName = uri.lastPathSegment ?: "Video"; loopOn = true } }

                // 4. DESTINATIONS
                var youtubeOn by remember { mutableStateOf(true) }
                var facebookOn by remember { mutableStateOf(true) }
                var websiteOn by remember { mutableStateOf(false) }
                var twitchOn by remember { mutableStateOf(false) }
                var tiktokOn by remember { mutableStateOf(false) }
                var linkedinOn by remember { mutableStateOf(false) }
                var useRestream by remember { mutableStateOf(false) }
                var youtubeUrl by remember { mutableStateOf("") }
                var facebookUrl by remember { mutableStateOf("") }
                var websiteUrl by remember { mutableStateOf("") }
                var twitchUrl by remember { mutableStateOf("") }
                var customRtmp by remember { mutableStateOf("") }

                // 5. CHAT / GUESTS / MODERATION
                var unifiedChatOn by remember { mutableStateOf(true) }
                var chatMessages by remember { mutableStateOf(listOf("Welcome to JSTV! 🙏", "Amen!", "God bless you")) }
                var chatInput by remember { mutableStateOf("") }
                var guestLink by remember { mutableStateOf("https://jstv.live/guest/${Random.nextInt(1000,9999)}") }
                var guestAudioOn by remember { mutableStateOf(true) }
                var guestVideoOn by remember { mutableStateOf(true) }
                var wordFilterOn by remember { mutableStateOf(true) }
                var slowModeOn by remember { mutableStateOf(false) }
                var subOnlyChat by remember { mutableStateOf(false) }
                var bannedWords by remember { mutableStateOf("bad, hate, spam") }

                // 6. RECORDING
                var saveAfterLive by remember { mutableStateOf(true) }
                var recordFormat by remember { mutableStateOf("MP4") }
                var multiTrackOn by remember { mutableStateOf(false) }
                var replayBufferOn by remember { mutableStateOf(false) }
                var replaySeconds by remember { mutableStateOf("30s") }
                var savedList by remember { mutableStateOf(listOf<String>()) }

                var myCode by remember { mutableStateOf(Random.nextInt(100000, 999999).toString()) }
                var remoteCodeInput by remember { mutableStateOf("") }
                var remoteStatus by remember { mutableStateOf("Not connected") }
                var remoteCam2Connected by remember { mutableStateOf(false) }
                var remoteCam3Connected by remember { mutableStateOf(false) }

                LaunchedEffect(isLive) { if (isLive) { while (true) { delay(1000); liveSeconds++ } } }
                fun fmt(s: Int) = "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
                val blink by rememberInfiniteTransition(label = "b").animateFloat(1f, 0.2f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "b")
                val tickerOffset by rememberInfiniteTransition(label = "t").animateFloat(900f, -1800f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart), label = "t")
                fun stopLive() { if (isLive && saveAfterLive) savedList = savedList + "Saved: $topic - ${fmt(liveSeconds)} - $recordFormat"; isLive = false; liveSeconds = 0 }
                fun startLive(): Boolean { if (!youtubeOn && !facebookOn && !websiteOn && !twitchOn) { error = "Turn ON at least 1 destination"; return false }; if (youtubeOn && youtubeUrl.isBlank() && !useRestream) { error = "Paste YouTube RTMP URL"; return false }; if (facebookOn && facebookUrl.isBlank() && !useRestream) { error = "Paste Facebook RTMP URL"; return false }; error = ""; return true }
                fun toggleOrientation() { isLandscape = !isLandscape; activity?.requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }

                if (!loggedIn) {
                    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(90.dp).clip(CircleShape)); Spacer(Modifier.height(8.dp))
                        Text("JEHOVAH SHALOM\nOUTREACH MINISTRIES", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text("JSTV STUDIO - LOGIN", fontSize = 13.sp, color = Color.Gray); Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = loginEmail, onValueChange = { loginEmail = it }, label = { Text("Email / Username") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = loginPass, onValueChange = { loginPass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp))
                        if (loginError.isNotEmpty()) Text(loginError, color = Color.Red)
                        Button(onClick = { if (loginEmail.isBlank() || loginPass.isBlank()) loginError = "Enter email and password" else loggedIn = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A3A))) { Text("LOGIN →") }
                    }; return@MaterialTheme
                }

                // SETTINGS - 6 TABS
                if (showSettings) {
                    AlertDialog(onDismissRequest = { showSettings = false }, title = { Text("⚙️ JSTV PRO SETTINGS") }, text = {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Video","Audio","Scene").forEachIndexed { i, t -> FilterChip(selected = settingsTab == i, onClick = { settingsTab = i }, label = { Text(t, fontSize = 11.sp) }) }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Dest","Chat","Record").forEachIndexed { i, t -> FilterChip(selected = settingsTab == i + 3, onClick = { settingsTab = i + 3 }, label = { Text(t, fontSize = 11.sp) }) }
                            }
                            Column(Modifier.verticalScroll(rememberScrollState()).height(420.dp)) {
                                when (settingsTab) {
                                    0 -> { Text("1. VIDEO & ENCODER", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Text("Resolution"); Row { listOf("720p","1080p").forEach { r -> FilterChip(selected = resolution == r, onClick = { resolution = r }, label = { Text(r) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Frame Rate"); Row { listOf("30fps","60fps").forEach { r -> FilterChip(selected = fps == r, onClick = { fps = r }, label = { Text(r) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Bitrate"); Row { listOf("4000 Kbps","6000 Kbps","10000 Kbps","14000 Kbps").forEach { b -> FilterChip(selected = bitrate == b, onClick = { bitrate = b }, label = { Text(b, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Codec"); Row { listOf("H.264","H.265/HEVC","AV1").forEach { c -> FilterChip(selected = codec == c, onClick = { codec = c }, label = { Text(c, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Encoder Type"); Row { listOf("Hardware","Software x264").forEach { e -> FilterChip(selected = encoderType == e, onClick = { encoderType = e }, label = { Text(e, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Hardware = Phone GPU (saves battery) | Software = CPU (better quality)", fontSize = 11.sp, color = Color.Gray)
                                        Text("Aspect / Layout"); Row { listOf("16:9 Horizontal","9:16 Vertical","1:1 Square").forEach { a -> FilterChip(selected = aspect == a, onClick = { aspect = a }, label = { Text(a, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Layout"); Row { listOf("Full Camera","Split","Picture-in-Picture").forEach { l -> FilterChip(selected = layoutMode == l, onClick = { layoutMode = l }, label = { Text(l, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                    }
                                    1 -> { Text("2. AUDIO & INPUT", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = micOn, onCheckedChange = { micOn = it }); Text("  Mic ON") }
                                        Text("Mic Source"); Row { listOf("Phone Mic","Bluetooth","USB Mic").forEach { m -> FilterChip(selected = micSource == m, onClick = { micSource = m }, label = { Text(m, fontSize = 11.sp) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Sample Rate"); Row { listOf("44.1kHz","48kHz").forEach { s -> FilterChip(selected = sampleRate == s, onClick = { sampleRate = s }, label = { Text(s) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Channels"); Row { listOf("Mono","Stereo").forEach { c -> FilterChip(selected = channels == c, onClick = { channels = c }, label = { Text(c) }); Spacer(Modifier.width(4.dp)) } }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = systemAudioOn, onCheckedChange = { systemAudioOn = it }); Text("  Desktop / System Audio") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = noiseSuppression, onCheckedChange = { noiseSuppression = it }); Text("  Noise Suppression") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = compressorOn, onCheckedChange = { compressorOn = it }); Text("  Compressor") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = smartMute, onCheckedChange = { smartMute = it }); Text("  Smart Mute / AI Enhance") }
                                        Text("Mic Gain: ${micGain.toInt()}%"); Slider(value = micGain, onValueChange = { micGain = it }, valueRange = 0f..100f)
                                    }
                                    2 -> { Text("3. SCENE, SOURCE & OVERLAY", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Text("Topic"); OutlinedTextField(value = topic, onValueChange = { topic = it }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        Text("Theme"); OutlinedTextField(value = theme, onValueChange = { theme = it }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        Text("Phone on Video"); OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        Text("Brand Color"); Row { listOf("Gold","Red","Blue","Green").forEach { bc -> FilterChip(selected = brandColor == bc, onClick = { brandColor = bc }, label = { Text(bc) }); Spacer(Modifier.width(4.dp)) } }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = showLogoOnVideo, onCheckedChange = { showLogoOnVideo = it }); Text("  Logo ON") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = chromaKeyOn, onCheckedChange = { chromaKeyOn = it }); Text("  Green Screen / Chroma Key") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = virtualBgOn, onCheckedChange = { virtualBgOn = it }); Text("  Virtual Background") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = showLowerThird, onCheckedChange = { showLowerThird = it }); Text("  Lower Third ON") }
                                        OutlinedTextField(value = lowerThird, onValueChange = { lowerThird = it }, label = { Text("Lower Third Text") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = tickerOn, onCheckedChange = { tickerOn = it }); Text("  Moving Text ON") }
                                        OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Your Moving Text") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = alertsOn, onCheckedChange = { alertsOn = it }); Text("  Alerts / Follower / Sub / Tip") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = chatBoxOn, onCheckedChange = { chatBoxOn = it }); Text("  Chat Box Overlay") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = scoreboardOn, onCheckedChange = { scoreboardOn = it }); Text("  Scoreboard") }
                                        if (scoreboardOn) OutlinedTextField(value = scoreText, onValueChange = { scoreText = it }, label = { Text("Score") }, modifier = Modifier.fillMaxWidth())
                                        Spacer(Modifier.height(6.dp)); Text("LOOP VIDEO"); Text(loopVideoName, fontSize = 12.sp); Button(onClick = { pickVideo.launch("video/*") }) { Text("📁 ADD VIDEO TO LOOP") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = loopOn, onCheckedChange = { loopOn = it }); Text("  Loop ON") }
                                        Button(onClick = { logoOffset = Offset(0f,0f); topicOffset = Offset(0f,0f); phoneOffset = Offset(0f,0f); lowerOffset = Offset(0f,0f) }) { Text("RESET LAYOUT - Move items back") }
                                        Text("Tip: Drag Logo / Topic / Phone / Lower Third on video to move them.", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    3 -> { Text("4. DESTINATIONS & MULTISTREAM", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = useRestream, onCheckedChange = { useRestream = it }); Text("  Use Restream Cloud (1 RTMP to all)") }
                                        Text("Toggle platforms, paste RTMP + Key in each:", fontSize = 12.sp)
                                        listOf(Triple("YouTube", youtubeOn, { v: Boolean -> youtubeOn = v }), Triple("Facebook", facebookOn, { v: Boolean -> facebookOn = v }), Triple("Website", websiteOn, { v: Boolean -> websiteOn = v }), Triple("Twitch", twitchOn, { v: Boolean -> twitchOn = v }), Triple("TikTok / Shorts", tiktokOn, { v: Boolean -> tiktokOn = v }), Triple("LinkedIn", linkedinOn, { v: Boolean -> linkedinOn = v })).forEach { (name, on, set) -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = on, onCheckedChange = set); Text(name) } }
                                        OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("YouTube RTMP + Key") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("Facebook RTMP + Key") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Website RTMP + Key") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(value = twitchUrl, onValueChange = { twitchUrl = it }, label = { Text("Twitch / Custom RTMP") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(value = customRtmp, onValueChange = { customRtmp = it }, label = { Text("Custom RTMP Endpoint") }, modifier = Modifier.fillMaxWidth())
                                    }
                                    4 -> { Text("5. CHAT, GUESTS & MODERATION", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = unifiedChatOn, onCheckedChange = { unifiedChatOn = it }); Text("  Unified Chat (all platforms)") }
                                        Text("Guest Link:"); Text(guestLink, fontSize = 12.sp, color = Color.Blue); Button(onClick = { guestLink = "https://jstv.live/guest/${Random.nextInt(1000,9999)}" }) { Text("Generate New Guest Link") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = guestAudioOn, onCheckedChange = { guestAudioOn = it }); Text("  Guest Audio") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = guestVideoOn, onCheckedChange = { guestVideoOn = it }); Text("  Guest Video") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = wordFilterOn, onCheckedChange = { wordFilterOn = it }); Text("  Word Filter") }
                                        if (wordFilterOn) OutlinedTextField(value = bannedWords, onValueChange = { bannedWords = it }, label = { Text("Banned Words, comma separated") }, modifier = Modifier.fillMaxWidth())
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = slowModeOn, onCheckedChange = { slowModeOn = it }); Text("  Slow Mode") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = subOnlyChat, onCheckedChange = { subOnlyChat = it }); Text("  Subscriber-Only Chat") }
                                    }
                                    5 -> { Text("6. RECORDING & OUTPUT", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = saveAfterLive, onCheckedChange = { saveAfterLive = it }); Text("  Save After Streaming") }
                                        Text("Format"); Row { listOf("MP4","WAV").forEach { f -> FilterChip(selected = recordFormat == f, onClick = { recordFormat = f }, label = { Text(f) }); Spacer(Modifier.width(4.dp)) } }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = multiTrackOn, onCheckedChange = { multiTrackOn = it }); Text("  Multi-Track Isolated (per person)") }
                                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = replayBufferOn, onCheckedChange = { replayBufferOn = it }); Text("  Replay Buffer / Instant Replay") }
                                        if (replayBufferOn) Row { listOf("15s","30s","60s").forEach { rs -> FilterChip(selected = replaySeconds == rs, onClick = { replaySeconds = rs }, label = { Text(rs) }); Spacer(Modifier.width(4.dp)) } }
                                        Text("Saved Recordings: ${savedList.size}"); savedList.forEach { Text(it, fontSize = 11.sp) }
                                        if (replayBufferOn) Button(onClick = {}) { Text("▶ SAVE REPLAY CLIP ($replaySeconds)") }
                                    }
                                }
                            }
                        }
                    }, confirmButton = { Button(onClick = { showSettings = false }) { Text("SAVE & CLOSE") } })
                }

                Column(Modifier.fillMaxSize().background(Color(0xFF111111))) {
                    Row(Modifier.fillMaxWidth().background(Color.Black).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(32.dp).clip(CircleShape)); Spacer(Modifier.width(6.dp))
                        Text("JSTV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) { Text(topic, color = Color.Gray, fontSize = 11.sp, maxLines = 1); Text("$resolution • $fps • $bitrate • $codec • $encoderType • $aspect", color = Color.Gray, fontSize = 9.sp, maxLines = 1) }
                        if (isLive) { Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 6.dp, vertical = 3.dp)) { Text("● LIVE ${fmt(liveSeconds)}", color = Color.White, fontSize = 11.sp, maxLines = 1) }; Spacer(Modifier.width(4.dp)) }
                        Button(onClick = { toggleOrientation() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text(if (isLandscape) "↕️" else "↔️") }
                        Spacer(Modifier.width(4.dp)); Button(onClick = { showSettings = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("⚙️") }
                        Spacer(Modifier.width(4.dp)); Button(onClick = { loggedIn = false }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("OUT") }
                    }
                    when (screen) {
                        "studio" -> Column(Modifier.fillMaxSize().padding(10.dp)) {
                            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
                                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(activeCam, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(if (loopOn && loopVideoUri != null) "▶ LOOPING: $loopVideoName" else "$camera Camera - $layoutMode", color = Color.Gray, fontSize = 13.sp)
                                    if (replayBufferOn) Text("⏪ REPLAY BUFFER $replaySeconds", color = Color.Yellow, fontSize = 11.sp)
                                }
                                if (showLogoOnVideo) Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "Logo", modifier = Modifier.align(Alignment.TopCenter).offset { IntOffset(logoOffset.x.toInt(), logoOffset.y.toInt()) }.size(48.dp).clip(CircleShape).pointerInput(Unit) { detectDragGestures { _, d -> logoOffset += d } })
                                Text(topic, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).offset { IntOffset(topicOffset.x.toInt(), topicOffset.y.toInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> topicOffset += d } })
                                Text("📞 $phoneNumber", color = Color.Yellow, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center).offset { IntOffset(phoneOffset.x.toInt(), (topicOffset.y + 30f + phoneOffset.y).toInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> phoneOffset += d } })
                                if (scoreboardOn) Box(Modifier.align(Alignment.TopCenter).padding(top = 60.dp).background(Color.Black.copy(alpha = 0.7f)).padding(6.dp)) { Text(scoreText, color = Color.White, fontWeight = FontWeight.Bold) }
                                if (isLive) Box(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Red.copy(alpha = blink)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("● LIVE", color = Color.White, fontWeight = FontWeight.Bold) }
                                if (showLowerThird) Box(Modifier.align(Alignment.BottomStart).padding(bottom = if (tickerOn) 34.dp else 10.dp, start = 10.dp).offset { IntOffset(lowerOffset.x.toInt(), lowerOffset.y.toInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> lowerOffset += d } }.clip(RoundedCornerShape(4.dp)).background(Color(0xFFB8860B)).padding(horizontal = 10.dp, vertical = 6.dp)) { Text(lowerThird, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                if (tickerOn) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(28.dp).background(Color.Red)) { Box(Modifier.offset(x = tickerOffset.dp)) { Text(tickerText + "   •   " + tickerText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) } }
                            }
                            Spacer(Modifier.height(8.dp)); if (error.isNotEmpty()) Text(error, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            if (unifiedChatOn && chatBoxOn) { Row(Modifier.fillMaxWidth().background(Color.DarkGray, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Text("💬 ${chatMessages.lastOrNull() ?: ""}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1); Button(onClick = { screen = "chat" }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("CHAT") } }; Spacer(Modifier.height(6.dp)) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { activeCam = "CAM 1" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam == "CAM 1") Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 1") }
                                Button(onClick = { activeCam = "CAM 2" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam.contains("CAM 2")) Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 2 ${if (remoteCam2Connected) "✓" else ""}") }
                                Button(onClick = { activeCam = "CAM 3" }, colors = ButtonDefaults.buttonColors(containerColor = if (activeCam.contains("CAM 3")) Color(0xFFB8860B) else Color.DarkGray)) { Text("CAM 3 ${if (remoteCam3Connected) "✓" else ""}") }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { camera = if (camera == "FRONT") "REAR" else "FRONT" }) { Text("SWITCH: $camera") }
                                Button(onClick = { pickVideo.launch("video/*") }) { Text("📁 LOOP") }
                                Button(onClick = { loopOn = !loopOn }, colors = ButtonDefaults.buttonColors(containerColor = if (loopOn) Color.Yellow else Color.DarkGray)) { Text(if (loopOn) "🔁 ON" else "LOOP OFF") }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth()) { Button(onClick = { screen = "remote" }, modifier = Modifier.weight(1f)) { Text("REMOTE") }; Spacer(Modifier.width(6.dp)); Button(onClick = { showSettings = true; settingsTab = 3 }, modifier = Modifier.weight(1f)) { Text("STREAM URLs") }; Spacer(Modifier.width(6.dp)); Button(onClick = { if (!isLive) { if (startLive()) isLive = true } else stopLive() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A))) { Text(if (isLive) "■ STOP" else "● GO LIVE") } }
                        }
                        "remote" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REMOTE CONTROL", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
                            Text("YOUR CODE:", color = Color.Yellow, fontSize = 13.sp); Text(myCode, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            Text("Guest Link: $guestLink", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = remoteCodeInput, onValueChange = { remoteCodeInput = it }, label = { Text("Enter Code from other phone") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)); Spacer(Modifier.height(8.dp))
                            Row { Button(onClick = { if (remoteCodeInput.length == 6) { remoteCam2Connected = true; remoteStatus = "CAM 2 CONNECTED ✓" } else remoteStatus = "Enter 6-digit code" }) { Text("CONNECT CAM 2") }; Spacer(Modifier.width(8.dp)); Button(onClick = { if (remoteCodeInput.length == 6) { remoteCam3Connected = true; remoteStatus = "CAM 3 CONNECTED ✓" } }) { Text("CONNECT CAM 3") } }
                            Spacer(Modifier.height(10.dp)); Text(remoteStatus, color = Color.White); Spacer(Modifier.height(16.dp)); Button(onClick = { screen = "studio" }) { Text("← BACK TO STUDIO") }
                        }
                        "chat" -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                            Text("💬 UNIFIED CHAT - All Platforms", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp))
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { chatMessages.forEach { Text(it, color = Color.White, modifier = Modifier.padding(vertical = 4.dp)) } }
                            Row { OutlinedTextField(value = chatInput, onValueChange = { chatInput = it }, label = { Text("Type message...") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)); Spacer(Modifier.width(6.dp)); Button(onClick = { if (chatInput.isNotBlank()) { chatMessages = chatMessages + "You: $chatInput"; chatInput = "" } }) { Text("SEND") } }
                            Spacer(Modifier.height(10.dp)); Text("Moderation: Filter ${if (wordFilterOn) "ON" else "OFF"} | Slow ${if (slowModeOn) "ON" else "OFF"} | Sub-Only ${if (subOnlyChat) "ON" else "OFF"}", color = Color.Gray, fontSize = 12.sp)
                            Button(onClick = { screen = "studio" }) { Text("← BACK") }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color.Black).padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { screen = "studio" }) { Text("STUDIO") }; Button(onClick = { screen = "remote" }) { Text("REMOTE") }; Button(onClick = { screen = "chat" }) { Text("CHAT") }; Button(onClick = { showSettings = true }) { Text("⚙️ SETTINGS") }
                    }
                }
            }
        }
    }
}
