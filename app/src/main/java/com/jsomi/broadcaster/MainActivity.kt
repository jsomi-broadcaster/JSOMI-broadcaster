package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var cam: RtmpCamera1? = null
    private var hasPerm by mutableStateOf(false)
    private var isLand by mutableStateOf(false)
    private var onLogo: ((Uri?) -> Unit)? = null
    private var onVideo: ((Uri?) -> Unit)? = null
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r -> hasPerm = r.values.all { it } }
    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onLogo?.invoke(uri) }
    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onVideo?.invoke(uri) }
    private fun ask() {
        val c = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val m = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (c && m) hasPerm = true else permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }
    private fun makeCam(v: SurfaceView): RtmpCamera1 {
        val chk = object : ConnectChecker {
            override fun onConnectionStarted(url: String) {}
            override fun onConnectionSuccess() {}
            override fun onConnectionFailed(reason: String) {}
            override fun onNewBitrate(bitrate: Long) {}
            override fun onDisconnect() {}
            override fun onAuthError() {}
            override fun onAuthSuccess() {}
        }
        return RtmpCamera1(v, chk)
    }
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        // TRUE FULLSCREEN - PRISM STYLE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val ctl = WindowInsetsControllerCompat(window, window.decorView)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.hide(WindowInsetsCompat.Type.statusBars())
        ctl.hide(WindowInsetsCompat.Type.navigationBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ask()
        setContent {
            JstvScreen(hasPerm, isLand,
                onAsk = { ask() },
                onRotate = { isLand = !isLand; requestedOrientation = if (isLand) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
                onPickLogo = { cb -> onLogo = cb; logoPicker.launch("image/*") },
                onPickVideo = { cb -> onVideo = cb; videoPicker.launch("video/*") },
                onToast = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() },
                onSetCam = { cam = makeCam(it) },
                onPreview = { try { if (cam?.isOnPreview == false) cam?.startPreview() } catch (e: Exception) {} },
                onStart = { url -> val a = cam?.prepareAudio() == true && cam?.prepareVideo() == true; if (a) { cam?.startStream(url); true } else false },
                onStop = { try { cam?.stopStream() } catch (e: Exception) {} },
                onSwitchCam = { try { cam?.switchCamera() } catch (e: Exception) {} },
                onMute = { if (it) cam?.disableAudio() else cam?.enableAudio() }
            )
        }
    }
    override fun onDestroy() { try { cam?.stopStream() } catch (e: Exception) {}; super.onDestroy() }
    override fun onWindowFocusChanged(f: Boolean) {
        super.onWindowFocusChanged(f)
        if (f) {
            val ctl = WindowInsetsControllerCompat(window, window.decorView)
            ctl.hide(WindowInsetsCompat.Type.systemBars())
            ctl.hide(WindowInsetsCompat.Type.statusBars())
            ctl.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

data class TitleItem(val id: Int, val text: String)
data class Channel(val id: Int, val platform: String, val key: String, val enabled: Boolean)

@Composable
fun JstvScreen(hasPerm: Boolean, isLand: Boolean, onAsk: () -> Unit, onRotate: () -> Unit, onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit, onSetCam: (SurfaceView) -> Unit, onPreview: () -> Unit, onStart: (String) -> Boolean, onStop: () -> Unit, onSwitchCam: () -> Unit, onMute: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf("BROADCAST") }
    var restreamKey by remember { mutableStateOf("") }
    var newPlat by remember { mutableStateOf("YouTube") }
    var newKey by remember { mutableStateOf("") }
    var nextCh by remember { mutableStateOf(4) }
    var channels by remember { mutableStateOf(listOf(Channel(1, "YouTube", "", true), Channel(2, "Facebook", "", false), Channel(3, "TikTok", "", false))) }
    var status by remember { mutableStateOf("READY") }
    var streaming by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var camName by remember { mutableStateOf("BACK") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOff by remember { mutableStateOf(Offset(10f, 10f)) }
    var logoSize by remember { mutableStateOf(80f) }
    var showTitle by remember { mutableStateOf(true) }
    var titleSize by remember { mutableStateOf(22f) }
    var titleColor by remember { mutableStateOf("Yellow") }
    var titleFont by remember { mutableStateOf("Normal") }
    var titleBold by remember { mutableStateOf(true) }
    var tickerText by remember { mutableStateOf("JSTV - JEHOVAH SHALOM | Welcome to Jehovah Shalom TV | Paybill 516600 Acc 5600935001") }
    var tickerMoving by remember { mutableStateOf(true) }
    var tickerLoop by remember { mutableStateOf(true) }
    var tickerAlign by remember { mutableStateOf("Center") }
    var tickerColor by remember { mutableStateOf("White") }
    var tickerSize by remember { mutableStateOf(14f) }
    var tickerSpeed by remember { mutableStateOf(40f) }
    var tickerBgOn by remember { mutableStateOf(true) }
    var tickerOff by remember { mutableStateOf(0f) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    var galleryLoop by remember { mutableStateOf(true) }
    var isGallery by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(4) }
    var introText by remember { mutableStateOf("Welcome to JSTV - Jehovah Shalom") }
    var showIntro by remember { mutableStateOf(false) }
    var introVis by remember { mutableStateOf(false) }
    var now by remember { mutableStateOf("") }
    var titles by remember { mutableStateOf(listOf(TitleItem(1, "Jehovah Shalom"), TitleItem(2, "Welcome"), TitleItem(3, "God Bless You"))) }
    var isPremium by remember { mutableStateOf(false) }
    var premiumCode by remember { mutableStateOf("") }
    var premiumMessage by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Daily 50") }
    val validCodes = listOf("JSTV-50-1A2B","JSTV-50-2B3C","JSTV-50-3C4D","JSTV-50-4D5E","JSTV-50-5E6F","JSTV-1K-3C4D","JSTV-1K-4D5E","JSTV-1K-5E6F","JSTV-1K-6F7G","JSTV-1K-7G8H","JSTV-3M-5E6F","JSTV-3M-6F7G","JSTV-3M-7G8H","JSTV-3M-8H9I","JSTV-3M-9I0J","JSTV-YR-7G8H","JSTV-YR-8H9I","JSTV-YR-9I0J","JSTV-YR-1J2K","JSTV-YR-2K3L")
    var scene by remember { mutableStateOf("Studio") }
    var extCamOn by remember { mutableStateOf(false) }
    var extCamName by remember { mutableStateOf("USB Camera") }
    var remoteOn by remember { mutableStateOf(false) }
    var remoteCode by remember { mutableStateOf("JSTV-2026") }
    var remoteMsg by remember { mutableStateOf("") }
    // PRISM LIVE STUDIO SETTINGS
    var hideUI by remember { mutableStateOf(false) }
    var resolution by remember { mutableStateOf("720p") }
    var bitrate by remember { mutableStateOf(2500f) }
    var fps by remember { mutableStateOf("30") }
    var micVol by remember { mutableStateOf(80f) }
    var musicVol by remember { mutableStateOf(50f) }
    var beautyOn by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(50f) }
    var chatOn by remember { mutableStateOf(true) }
    var countdownOn by remember { mutableStateOf(true) }
    var countdownNum by remember { mutableStateOf(0) }
    var showChatBox by remember { mutableStateOf(false) }
    var chatText by remember { mutableStateOf("Welcome to JSTV! God Bless You 🙏") }

    LaunchedEffect(hasPerm) { if (hasPerm) onPreview() }
    LaunchedEffect(Unit) { while (true) { now = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()); delay(1000) } }
    LaunchedEffect(introVis) { if (introVis) { delay(5000); introVis = false } }
    val inf = rememberInfiniteTransition(label = "ticker")
    val tx by inf.animateFloat(400f, -800f, infiniteRepeatable(tween((tickerSpeed.toInt() * 1000).coerceIn(2000, 20000), easing = LinearEasing), RepeatMode.Restart), label = "tx")
    fun onList() = channels.filter { it.enabled }
    fun urlFor(p: String, k: String): String {
        if (p == "YouTube") return "rtmp://a.rtmp.youtube.com/live2/$k"
        if (p == "TikTok") return "rtmp://push-rtmp-l1.tiktok.com/live/$k"
        if (p == "Instagram") return "rtmps://live-upload.instagram.com:443/rtmp/$k"
        if (p == "Twitch") return "rtmp://live.twitch.tv/app/$k"
        if (p == "Kick") return "rtmp://live.kick.com/app/$k"
        if (p == "Custom") return k
        return "rtmps://live-api-s.facebook.com:443/rtmp/$k"
    }
    fun goLive() {
        if (!hasPerm) { onToast("Grant Camera + Mic first"); onAsk(); return }
        val on = onList()
        if (on.isEmpty()) { onToast("Turn ON 1 Channel"); return }
        val label = on.joinToString(" + ") { it.platform }
        var url = ""
        if (on.size == 1) { if (on[0].key.isBlank()) { onToast("Add Key for ${on[0].platform}"); return }; url = urlFor(on[0].platform, on[0].key) }
        else { if (restreamKey.isBlank()) { onToast("Add JSTV Cloud Key for 2+ Channels"); return }; url = "rtmp://live.restream.io/live/$restreamKey" }
        status = "CONNECTING to $label"
        if (onStart(url)) { streaming = true; showSave = true; status = "LIVE on $label" } else status = "FAILED - Check Key"
    }
    fun endLive() { onStop(); streaming = false; showSave = false; status = "ENDED" }
    fun applyScene(s: String) {
        scene = s
        if (s == "Studio") { titleColor = "Yellow"; tickerText = "JSTV STUDIO - Jehovah Shalom | Welcome | Paybill 516600 Acc 5600935001"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Welcome"), TitleItem(3,"God Bless You")) }
        if (s == "Church") { titleColor = "Yellow"; tickerText = "JSTV CHURCH LIVE - Sunday Service | Jehovah Shalom | Paybill 516600 Acc 5600935001"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Sunday Service LIVE"), TitleItem(3,"God Bless You")) }
        if (s == "Outdoor") { titleColor = "Green"; tickerText = "JSTV OUTDOOR - Crusade LIVE | Jehovah Shalom | Paybill 516600 Acc 5600935001"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Outdoor Crusade"), TitleItem(3,"God Bless You")) }
        onToast("Scene: $s")
    }
    fun tBg(): Color { if (titleColor == "Blue") return Color.Blue; if (titleColor == "Green") return Color.Green; if (titleColor == "Red") return Color.Red; return Color.Yellow }
    fun kBg(): Color { if (tickerColor == "White") return Color.White; if (tickerColor == "Green") return Color.Green; if (tickerColor == "Red") return Color.Red; return Color.Yellow }
    fun share() {
        val txt = "Watch JSTV - Jehovah Shalom LIVE! ${onList().joinToString(", ") { it.platform }} - ${tickerText}"
        val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, txt); ctx.startActivity(Intent.createChooser(i, "Share JSTV"))
    }
    fun shareRemote() {
        val txt = "Control JSTV Remote - Code: $remoteCode - Open JSTV on 2nd phone, enter code, control GO LIVE / MUTE / SCENE from far"
        val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, txt); ctx.startActivity(Intent.createChooser(i, "Share Remote Code"))
    }

    if (tab == "BROADCAST") {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { if (hideUI) hideUI = false }) {
            AndroidView(factory = { v -> SurfaceView(v).also { onSetCam(it) } }, modifier = Modifier.fillMaxSize())
            if (showLogo && !hideUI) {
                Box(Modifier.offset(x = logoOff.x.dp, y = logoOff.y.dp).pointerInput(Unit) {
                    detectDragGestures { _, d -> logoOff = Offset((logoOff.x + d.x).coerceIn(0f, 300f), (logoOff.y + d.y).coerceIn(0f, 500f)) }
                }.background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)) {
                    Text("JSTV", color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (hideUI) {
                // CLEAN FULLSCREEN - Only titles + ticker, no buttons - Tap screen to bring back
                Column(Modifier.align(Alignment.TopCenter).padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    titles.forEach { Text(it.text, color = tBg(), fontSize = titleSize.sp, fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Normal) }
                }
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp).background(Color.Black.copy(alpha = 0.7f)).padding(6.dp)) { Text(tickerText, color = kBg(), fontSize = tickerSize.sp) }
                Text("Tap to Show Buttons", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
            } else {
                if (showTitle) {
                    Column(Modifier.align(Alignment.TopCenter).padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        titles.forEach { Text(it.text, color = tBg(), fontSize = titleSize.sp, fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Normal, fontFamily = if (titleFont == "Serif") FontFamily.Serif else if (titleFont == "Mono") FontFamily.Monospace else FontFamily.Default) }
                    }
                }
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 270.dp + tickerOff.dp)) {
                    if (tickerBgOn) {
                        Box(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.7f)).padding(6.dp)) {
                            if (tickerMoving) Text(tickerText, color = kBg(), fontSize = tickerSize.sp, modifier = Modifier.offset(x = tx.dp))
                            else Text(tickerText, color = kBg(), fontSize = tickerSize.sp, textAlign = if (tickerAlign == "Left") TextAlign.Start else if (tickerAlign == "Right") TextAlign.End else TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        if (tickerMoving) Text(tickerText, color = kBg(), fontSize = tickerSize.sp, modifier = Modifier.offset(x = tx.dp))
                        else Text(tickerText, color = kBg(), fontSize = tickerSize.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (showIntro && introVis) {
                    Box(Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.8f)).padding(16.dp)) { Text(introText, color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                }
                if (countdownNum > 0) {
                    Box(Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.8f)).padding(24.dp)) { Text("$countdownNum", color = Color.Yellow, fontSize = 60.sp, fontWeight = FontWeight.Bold) }
                }
                if (extCamOn) { Text("📷 EXT CAM: $extCamName ON", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp).background(Color.Black.copy(alpha = 0.6f)).padding(4.dp)) }
                if (remoteOn) { Text("📱 REMOTE ON - Code: $remoteCode", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp).background(Color.Black.copy(alpha = 0.6f)).padding(4.dp)) }
                if (chatOn && showChatBox) {
                    Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp).background(Color.Black.copy(alpha = 0.7f)).padding(8.dp)) { Text("💬 LIVE CHAT\n$chatText", color = Color.White, fontSize = 12.sp) }
                }
                Column(Modifier.align(Alignment.BottomCenter).padding(8.dp).background(Color.Black.copy(alpha = 0.6f))) {
                    Text(status + " | " + now + " | Scene: $scene | $resolution $fps fps ${bitrate.toInt()}kbps", color = Color.White, fontSize = 11.sp)
                    Text("ON: " + onList().joinToString(", ") { it.platform } + if (extCamOn) " | EXT CAM ON" else "" + if (beautyOn) " | BEAUTY ON" else "", color = Color.Yellow, fontSize = 11.sp)
                    if (isPremium) Text("⭐ PREMIUM ACTIVE - $selectedPlan", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { goLive() }, modifier = Modifier.weight(1f)) { Text(if (streaming) "LIVE..." else "GO LIVE") }
                        Button(onClick = { onSwitchCam(); camName = if (camName == "BACK") "FRONT" else "BACK" }, modifier = Modifier.weight(1f)) { Text("CAM: $camName") }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onRotate() }, modifier = Modifier.weight(1f)) { Text(if (isLand) "PORTRAIT" else "LANDSCAPE") }
                        Button(onClick = { muted = !muted; onMute(muted) }, modifier = Modifier.weight(1f)) { Text(if (muted) "UNMUTE" else "MUTE") }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { applyScene(if (scene == "Studio") "Church" else if (scene == "Church") "Outdoor" else "Studio") }, modifier = Modifier.weight(1f)) { Text("SCENE: $scene") }
                        Button(onClick = { extCamOn = !extCamOn; onToast(if (extCamOn) "Ext Cam ON - $extCamName" else "Ext Cam OFF") }, modifier = Modifier.weight(1f)) { Text(if (extCamOn) "EXT ON" else "EXT CAM") }
                        Button(onClick = { remoteOn = !remoteOn }, modifier = Modifier.weight(1f)) { Text(if (remoteOn) "REMOTE ON" else "REMOTE") }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { hideUI = true; onToast("Fullscreen Clean - Tap screen to show buttons") }, modifier = Modifier.weight(1f)) { Text("⛶ FULLSCREEN") }
                        Button(onClick = { showChatBox = !showChatBox }, modifier = Modifier.weight(1f)) { Text(if (showChatBox) "HIDE CHAT" else "CHAT") }
                        Button(onClick = { beautyOn = !beautyOn }, modifier = Modifier.weight(1f)) { Text(if (beautyOn) "BEAUTY ON" else "BEAUTY") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { tab = "SETUP" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("⚙️ SETUP - ALL SETTINGS / LIVE STUDIO", color = Color.Black, fontWeight = FontWeight.Bold) }
                    if (showSave) { Spacer(Modifier.height(6.dp)); Button(onClick = { endLive() }, modifier = Modifier.fillMaxWidth()) { Text("END LIVE") } }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(14.dp)) {
            Text("⚙️ SETUP - PRISM LIVE STUDIO - FULLSCREEN", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Status: " + status + " | " + now + " | Scene: $scene | $resolution $fps fps", color = Color.White)
            if (!hasPerm) { Spacer(Modifier.height(8.dp)); Button(onClick = { onAsk() }) { Text("ALLOW CAMERA + MIC") } }
            Spacer(Modifier.height(12.dp))
            Text("1) MY CHANNELS - Up to 6", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            channels.forEach { ch -> val one = ch; Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = one.enabled, onCheckedChange = { isChecked -> channels = channels.map { ch2 -> if (ch2.id == one.id) ch2.copy(enabled = isChecked) else ch2 } })
                Text("${one.platform}: ${if (one.key.isBlank()) "NO KEY" else "KEY SET"}", color = Color.White, modifier = Modifier.weight(1f))
                Button(onClick = { channels = channels.filter { it.id != one.id } }) { Text("DEL") }
            } }
            Text("ON now: " + onList().joinToString(", ") { it.platform }, color = Color.Yellow)
            Spacer(Modifier.height(8.dp))
            Text("+ ADD CHANNEL", fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Row { listOf("YouTube", "Facebook", "TikTok", "Instagram", "Twitch", "Kick", "Custom").forEach { p -> Button(onClick = { newPlat = p }, modifier = Modifier.padding(end = 4.dp)) { Text(p) } } }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = newKey, onValueChange = { newKey = it }, label = { Text("Key for $newPlat") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Button(onClick = { if (channels.size >= 6) { onToast("Max 6 channels") } else if (newKey.isBlank()) { onToast("Enter Key") } else { channels = channels + Channel(nextCh, newPlat, newKey, true); nextCh++; newKey = ""; onToast("Added $newPlat") } }) { Text("ADD $newPlat") }
            Spacer(Modifier.height(12.dp))
            Text("2) JSTV CLOUD KEY - For 2+ Channels", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Get FREE from restream.io > Settings > RTMP > Copy Key", color = Color.White.copy(alpha = 0.7f))
            OutlinedTextField(value = restreamKey, onValueChange = { restreamKey = it }, label = { Text("Restream Key") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text("3) CAMERA", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Camera now: " + camName, color = Color.Yellow)
            Button(onClick = { onSwitchCam(); camName = if (camName == "BACK") "FRONT" else "BACK" }) { Text("SWITCH CAMERA") }
            Spacer(Modifier.height(12.dp))
            Text("4) INTRO", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = introText, onValueChange = { introText = it }, label = { Text("Intro Text") }, modifier = Modifier.fillMaxWidth())
            Row { Button(onClick = { showIntro = !showIntro }) { Text(if (showIntro) "INTRO ON" else "INTRO OFF") }; Spacer(Modifier.width(6.dp)); Button(onClick = { introVis = true }) { Text("SHOW INTRO NOW") } }
            Spacer(Modifier.height(12.dp))
            Text("5) LOGO", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row { Button(onClick = { onPickLogo { logoUri = it } }) { Text("PICK LOGO") }; Spacer(Modifier.width(6.dp)); Button(onClick = { showLogo = !showLogo }) { Text(if (showLogo) "HIDE LOGO" else "SHOW LOGO") } }
            Text(if (logoUri == null) "No logo picked - using JSTV text" else "Logo picked", color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Text("6) TITLES", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("New Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { if (newTitle.isNotBlank()) { titles = titles + TitleItem(nextId, newTitle); nextId++; newTitle = "" } }) { Text("ADD TITLE") } }
            Spacer(Modifier.height(6.dp))
            titles.forEach { t -> Row(verticalAlignment = Alignment.CenterVertically) { Text(t.text, color = Color.White, modifier = Modifier.weight(1f)); Button(onClick = { titles = titles.filter { it.id != t.id } }) { Text("DEL") } } }
            Spacer(Modifier.height(8.dp))
            Text("THEME - Title Color", fontSize = 16.sp, color = Color.White)
            Row { listOf("Red", "Blue", "Green", "Yellow").forEach { c -> Button(onClick = { titleColor = c }, modifier = Modifier.padding(end = 4.dp)) { Text(c) } } }
            Spacer(Modifier.height(6.dp))
            Text("FONT - Title Font", fontSize = 16.sp, color = Color.White)
            Row { listOf("Normal", "Serif", "Mono").forEach { f -> Button(onClick = { titleFont = f }, modifier = Modifier.padding(end = 4.dp)) { Text(f) } } }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("SIZE: ${titleSize.toInt()}", color = Color.White); Slider(value = titleSize, onValueChange = { titleSize = it }, valueRange = 12f..40f, modifier = Modifier.weight(1f)) }
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { titleSize = (titleSize - 2).coerceAtLeast(12f) }) { Text("-") }; Button(onClick = { titleSize = (titleSize + 2).coerceAtMost(40f) }) { Text("+") }; Button(onClick = { titleBold = !titleBold }) { Text(if (titleBold) "BOLD ON" else "BOLD OFF") } }
            Spacer(Modifier.height(12.dp))
            Text("7) MOVING TEXT", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Ticker Text") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(6.dp))
            Text("LIVE PREVIEW:", color = Color.White)
            Box(Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) { Text(tickerText, color = kBg(), fontSize = tickerSize.sp) }
            Spacer(Modifier.height(6.dp))
            Text("SIZE: " + tickerSize.toInt(), color = Color.White)
            Slider(value = tickerSize, onValueChange = { tickerSize = it }, valueRange = 10f..30f)
            Row { Button(onClick = { tickerSize = 12f }) { Text("SMALL") }; Button(onClick = { tickerSize = 16f }) { Text("MED") }; Button(onClick = { tickerSize = 22f }) { Text("BIG") } }
            Spacer(Modifier.height(6.dp))
            Text("COLOR:", color = Color.White)
            Row { listOf("Yellow", "White", "Green", "Red").forEach { c -> Button(onClick = { tickerColor = c }, modifier = Modifier.padding(end = 4.dp)) { Text(c) } } }
            Spacer(Modifier.height(6.dp))
            Text("MOVE:", color = Color.White)
            Row { Button(onClick = { tickerOff = tickerOff - 20f }) { Text("⬆ UP") }; Button(onClick = { tickerOff = tickerOff + 20f }) { Text("⬇ DOWN") }; Button(onClick = { tickerOff = 0f }) { Text("RESET") } }
            Spacer(Modifier.height(6.dp))
            Row { listOf("Left", "Center", "Right").forEach { a -> Button(onClick = { tickerAlign = a }, modifier = Modifier.padding(end = 4.dp)) { Text(a) } } }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("BG:", color = Color.White); Checkbox(checked = tickerBgOn, onCheckedChange = { tickerBgOn = it }); Text("Background", color = Color.White) }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("MOVING:", color = Color.White); Checkbox(checked = tickerMoving, onCheckedChange = { tickerMoving = it }); Text("Scroll", color = Color.White) }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("LOOP:", color = Color.White); Checkbox(checked = tickerLoop, onCheckedChange = { tickerLoop = it }); Text("Loop", color = Color.White) }
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { tickerSpeed = (tickerSpeed - 10f).coerceAtLeast(10f) }) { Text("SLOWER") }; Button(onClick = { tickerSpeed = (tickerSpeed + 10f).coerceAtMost(100f) }) { Text("FASTER") } }
            Spacer(Modifier.height(12.dp))
            Text("8) GALLERY + LOOP", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Button(onClick = { onPickVideo { uri -> galleryUri = uri } }) { Text("PICK VIDEO") }
            Spacer(Modifier.height(6.dp))
            Text(if (galleryUri == null) "No video picked" else "Video picked", color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("LOOP:", color = Color.White); Checkbox(checked = galleryLoop, onCheckedChange = { galleryLoop = it }); Text("Loop Video", color = Color.White) }
            Spacer(Modifier.height(6.dp))
            Button(onClick = { if (galleryUri == null) onToast("Pick video first") else { isGallery = !isGallery; onToast(if (isGallery) "Gallery ON" else "Gallery OFF") } }) { Text(if (isGallery) "STOP GALLERY" else "PLAY GALLERY") }
            Spacer(Modifier.height(12.dp))
            Text("9) SHARE LINK", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { share() }) { Text("🔗 SHARE LINK") }
            Spacer(Modifier.height(20.dp))
            Text("10) JSTV PREMIUM", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White)
            Text("Paybill 516600 Acc 5600935001 | WhatsApp 0763052911", color = Color.White, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { selectedPlan = "Daily 50" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Daily 50") Color.Yellow else Color.DarkGray)) { Text("Daily 50", color = if (selectedPlan == "Daily 50") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { selectedPlan = "Monthly 1000" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Monthly 1000") Color.Yellow else Color.DarkGray)) { Text("Monthly 1000", color = if (selectedPlan == "Monthly 1000") Color.Black else Color.White) } }
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { selectedPlan = "3 Months 2500" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "3 Months 2500") Color.Yellow else Color.DarkGray)) { Text("3 Months 2500", color = if (selectedPlan == "3 Months 2500") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { selectedPlan = "Year 8000" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Year 8000") Color.Yellow else Color.DarkGray)) { Text("Year 8000", color = if (selectedPlan == "Year 8000") Color.Black else Color.White) } }
            Spacer(Modifier.height(6.dp))
            Text("Selected: $selectedPlan", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("1. M-PESA > Paybill 516600 | 2. Acc 5600935001 | 3. WhatsApp receipt 0763052911 | 4. Enter Code", color = Color.White, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = premiumCode, onValueChange = { premiumCode = it }, label = { Text("Enter Premium Code") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Button(onClick = {
                if (validCodes.contains(premiumCode.trim().uppercase())) { isPremium = true; premiumMessage = "✅ Welcome to Premium! $selectedPlan Active" }
                else { premiumMessage = "❌ Wrong Code. Pay via Paybill 516600 then WhatsApp 0763052911" }
            }, modifier = Modifier.fillMaxWidth()) { Text("UNLOCK PREMIUM - $selectedPlan") }
            if (premiumMessage.isNotEmpty()) Text(premiumMessage, color = if (isPremium) Color.Green else Color.Red)
            if (isPremium) { Spacer(Modifier.height(6.dp)); Text("⭐ PREMIUM ACTIVE - $selectedPlan", color = Color.Green, fontWeight = FontWeight.Bold); Button(onClick = { isPremium = false; premiumMessage = "Logged out" }) { Text("LOGOUT PREMIUM") } }
            Spacer(Modifier.height(20.dp))
            Text("11) SCENES - 1 Tap", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Now: $scene", color = Color.White)
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { applyScene("Studio") }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Studio") Color.Yellow else Color.DarkGray)) { Text("STUDIO", color = if (scene == "Studio") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { applyScene("Church") }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Church") Color.Yellow else Color.DarkGray)) { Text("CHURCH", color = if (scene == "Church") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { applyScene("Outdoor") }, colors = ButtonDefaults.buttonColors(containerColor = if (scene == "Outdoor") Color.Yellow else Color.DarkGray)) { Text("OUTDOOR", color = if (scene == "Outdoor") Color.Black else Color.White) } }
            Spacer(Modifier.height(20.dp))
            Text("12) EXTERNAL CAMERA", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("USB Camera / 2nd Phone as Camera. Plug USB camera with OTG cable, then turn ON.", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = extCamName, onValueChange = { extCamName = it }, label = { Text("External Camera Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("EXT CAM:", color = Color.White); Checkbox(checked = extCamOn, onCheckedChange = { extCamOn = it }); Text(if (extCamOn) "ON - $extCamName" else "OFF - Phone Cam", color = Color.White) }
            Spacer(Modifier.height(20.dp))
            Text("13) REMOTE CONTROL", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Control from 2nd phone while you preach.", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = remoteCode, onValueChange = { remoteCode = it }, label = { Text("Remote Code") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("REMOTE:", color = Color.White); Checkbox(checked = remoteOn, onCheckedChange = { remoteOn = it }); Text(if (remoteOn) "ON - Code: $remoteCode" else "OFF", color = Color.White) }
            if (remoteMsg.isNotEmpty()) Text(remoteMsg, color = Color.Cyan)
            Spacer(Modifier.height(6.dp))
            Row { Button(onClick = { remoteOn = true; remoteMsg = "Remote ON - Code $remoteCode" }) { Text("TURN REMOTE ON") }; Spacer(Modifier.width(6.dp)); Button(onClick = { shareRemote() }) { Text("SHARE CODE") } }
            Spacer(Modifier.height(20.dp))
            Text("14) LIVE STUDIO - LIKE PRISM", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Quality, Audio Mixer, Filters, Chat, Countdown - All Here", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("QUALITY - Resolution", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row { listOf("720p", "1080p").forEach { r -> Button(onClick = { resolution = r }, colors = ButtonDefaults.buttonColors(containerColor = if (resolution == r) Color.Yellow else Color.DarkGray), modifier = Modifier.padding(end = 4.dp)) { Text(r, color = if (resolution == r) Color.Black else Color.White) } } }
            Spacer(Modifier.height(6.dp))
            Text("FPS: $fps", color = Color.White)
            Row { listOf("30", "60").forEach { f -> Button(onClick = { fps = f }, colors = ButtonDefaults.buttonColors(containerColor = if (fps == f) Color.Yellow else Color.DarkGray), modifier = Modifier.padding(end = 4.dp)) { Text("$f FPS", color = if (fps == f) Color.Black else Color.White) } } }
            Spacer(Modifier.height(6.dp))
            Text("BITRATE: ${bitrate.toInt()} kbps - Higher = Clearer, needs good internet", color = Color.White)
            Slider(value = bitrate, onValueChange = { bitrate = it }, valueRange = 1000f..6000f)
            Row { Button(onClick = { bitrate = 1500f }) { Text("LOW - Slow Net") }; Button(onClick = { bitrate = 2500f }) { Text("MED") }; Button(onClick = { bitrate = 4500f }) { Text("HIGH - Clear") } }
            Spacer(Modifier.height(12.dp))
            Text("🎚️ AUDIO MIXER - Like PRISM", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("🎤 MIC Volume: ${micVol.toInt()}%", color = Color.White)
            Slider(value = micVol, onValueChange = { micVol = it }, valueRange = 0f..100f)
            Text("🎵 MUSIC Volume: ${musicVol.toInt()}%", color = Color.White)
            Slider(value = musicVol, onValueChange = { musicVol = it }, valueRange = 0f..100f)
            Row { Button(onClick = { muted = !muted; onMute(muted) }) { Text(if (muted) "UNMUTE MIC" else "MUTE MIC") }; Spacer(Modifier.width(6.dp)); Button(onClick = { musicVol = 0f }) { Text("MUTE MUSIC") } }
            Spacer(Modifier.height(12.dp))
            Text("✨ FILTERS", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("BEAUTY:", color = Color.White); Checkbox(checked = beautyOn, onCheckedChange = { beautyOn = it }); Text(if (beautyOn) "ON - Smooth Face" else "OFF", color = Color.White) }
            Text("BRIGHTNESS: ${brightness.toInt()}%", color = Color.White)
            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = 0f..100f)
            Row { Button(onClick = { brightness = 30f }) { Text("DARK") }; Button(onClick = { brightness = 50f }) { Text("NORMAL") }; Button(onClick = { brightness = 80f }) { Text("BRIGHT") } }
            Spacer(Modifier.height(12.dp))
            Text("💬 LIVE CHAT", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("SHOW CHAT:", color = Color.White); Checkbox(checked = chatOn, onCheckedChange = { chatOn = it }); Text(if (chatOn) "ON" else "OFF", color = Color.White) }
            OutlinedTextField(value = chatText, onValueChange = { chatText = it }, label = { Text("Chat Preview Text") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Button(onClick = { showChatBox = !showChatBox }) { Text(if (showChatBox) "HIDE CHAT ON SCREEN" else "SHOW CHAT ON SCREEN") }
            Spacer(Modifier.height(12.dp))
            Text("⏱️ COUNTDOWN BEFORE LIVE", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("COUNTDOWN:", color = Color.White); Checkbox(checked = countdownOn, onCheckedChange = { countdownOn = it }); Text(if (countdownOn) "ON - 3-2-1" else "OFF", color = Color.White) }
            Spacer(Modifier.height(6.dp))
            Text("⛶ FULLSCREEN", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hide all buttons for clean preaching view. Tap screen to bring back.", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Button(onClick = { hideUI = true; tab = "BROADCAST" }) { Text("GO FULLSCREEN CLEAN NOW") }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { tab = "BROADCAST" }, modifier = Modifier.fillMaxWidth()) { Text("⬅ BACK TO BROADCAST") }
            Spacer(Modifier.height(32.dp))
        }
    }
}
