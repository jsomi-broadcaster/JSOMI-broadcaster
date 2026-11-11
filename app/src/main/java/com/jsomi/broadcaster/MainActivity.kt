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
data class PremiumCode(val code: String, val plan: String, val customer: String, val active: Boolean)

// ---- SHARED STATE - This fixes Method Too Large ----
@Stable
class AppState {
    var appMode by mutableStateOf("CHOOSE")
    var tab by mutableStateOf("BROADCAST")
    var restreamKey by mutableStateOf("")
    var newPlat by mutableStateOf("YouTube")
    var newKey by mutableStateOf("")
    var nextCh by mutableStateOf(4)
    var channels by mutableStateOf(listOf(Channel(1, "YouTube", "", true), Channel(2, "Facebook", "", false), Channel(3, "TikTok", "", false)))
    var status by mutableStateOf("READY")
    var streaming by mutableStateOf(false)
    var showSave by mutableStateOf(false)
    var muted by mutableStateOf(false)
    var camName by mutableStateOf("BACK")
    var logoUri by mutableStateOf<Uri?>(null)
    var showLogo by mutableStateOf(true)
    var logoOff by mutableStateOf(Offset(10f, 10f))
    var showTitle by mutableStateOf(true)
    var titleSize by mutableStateOf(22f)
    var titleColor by mutableStateOf("Yellow")
    var titleFont by mutableStateOf("Normal")
    var titleBold by mutableStateOf(true)
    var tickerText by mutableStateOf("JSTV - JEHOVAH SHALOM | Welcome to Jehovah Shalom TV | Paybill 516600 Acc 5600935001")
    var tickerMoving by mutableStateOf(true)
    var tickerAlign by mutableStateOf("Center")
    var tickerColor by mutableStateOf("White")
    var tickerSize by mutableStateOf(14f)
    var tickerSpeed by mutableStateOf(40f)
    var tickerBgOn by mutableStateOf(true)
    var tickerOff by mutableStateOf(0f)
    var galleryUri by mutableStateOf<Uri?>(null)
    var galleryLoop by mutableStateOf(true)
    var isGallery by mutableStateOf(false)
    var newTitle by mutableStateOf("")
    var nextId by mutableStateOf(4)
    var introText by mutableStateOf("Welcome to JSTV - Jehovah Shalom")
    var showIntro by mutableStateOf(false)
    var introVis by mutableStateOf(false)
    var now by mutableStateOf("")
    var titles by mutableStateOf(listOf(TitleItem(1, "Jehovah Shalom"), TitleItem(2, "Welcome"), TitleItem(3, "God Bless You")))
    var isPremium by mutableStateOf(false)
    var premiumCode by mutableStateOf("")
    var premiumMessage by mutableStateOf("")
    var selectedPlan by mutableStateOf("Daily 200")
    var adminPass by mutableStateOf("")
    var adminLogged by mutableStateOf(false)
    // DAILY CHANGED 50 -> 200
    var priceDaily by mutableStateOf("200")
    var priceMonthly by mutableStateOf("1000")
    var price3M by mutableStateOf("2500")
    var priceYear by mutableStateOf("8000")
    var paybill by mutableStateOf("516600")
    var payAcc by mutableStateOf("5600935001")
    var adminWhats by mutableStateOf("0763052911")
    var freeForAll by mutableStateOf(false)
    var newCustName by mutableStateOf("")
    var newCustPlan by mutableStateOf("Daily 200")
    var lastGenCode by mutableStateOf("")
    var premiumCodes by mutableStateOf(listOf(
        PremiumCode("JSTV-200-1A2B","Daily 200","Demo User 1",true),
        PremiumCode("JSTV-1K-3C4D","Monthly 1000","Demo User 2",true),
        PremiumCode("JSTV-3M-5E6F","3 Months 2500","Demo User 3",true),
        PremiumCode("JSTV-YR-7G8H","Year 8000","Demo User 4",true)
    ))
    var scene by mutableStateOf("Studio")
    var extCamOn by mutableStateOf(false)
    var extCamMode by mutableStateOf("WiFi Phone")
    var extCamIP by mutableStateOf("")
    var extCamStatus by mutableStateOf("Not Connected")
    var remoteOn by mutableStateOf(false)
    var remoteCode by mutableStateOf("JSTV-2026")
    var remoteMsg by mutableStateOf("")
    var remoteConnected by mutableStateOf(false)
    var remoteInputCode by mutableStateOf("")
    var hideUI by mutableStateOf(false)
    var resolution by mutableStateOf("720p")
    var bitrate by mutableStateOf(2500f)
    var fps by mutableStateOf("30")
    var micVol by mutableStateOf(80f)
    var musicVol by mutableStateOf(50f)
    var beautyOn by mutableStateOf(false)
    var brightness by mutableStateOf(50f)
    var chatOn by mutableStateOf(true)
    var countdownOn by mutableStateOf(true)
    var countdownNum by mutableStateOf(0)
    var showChatBox by mutableStateOf(false)
    var chatText by mutableStateOf("Welcome to JSTV! God Bless You 🙏")
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
    fun applyScene(s: String) {
        scene = s
        if (s == "Studio") { titleColor = "Yellow"; tickerText = "JSTV STUDIO - Jehovah Shalom | Welcome | Paybill $paybill Acc $payAcc"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Welcome"), TitleItem(3,"God Bless You")) }
        if (s == "Church") { titleColor = "Yellow"; tickerText = "JSTV CHURCH LIVE - Sunday Service | Jehovah Shalom | Paybill $paybill Acc $payAcc"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Sunday Service LIVE"), TitleItem(3,"God Bless You")) }
        if (s == "Outdoor") { titleColor = "Green"; tickerText = "JSTV OUTDOOR - Crusade LIVE | Jehovah Shalom | Paybill $paybill Acc $payAcc"; titles = listOf(TitleItem(1,"Jehovah Shalom"), TitleItem(2,"Outdoor Crusade"), TitleItem(3,"God Bless You")) }
    }
    fun genCode(plan: String, cust: String): String {
        val prefix = if (plan.contains("Daily")) "JSTV-200-" else if (plan.contains("Monthly")) "JSTV-1K-" else if (plan.contains("3 Months")) "JSTV-3M-" else "JSTV-YR-"
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var suffix = ""
        repeat(4) { suffix += chars.random() }
        val code = prefix + suffix
        premiumCodes = premiumCodes + PremiumCode(code, plan, cust.ifBlank { "Customer" }, true)
        lastGenCode = code
        return code
    }
    fun tBg(): Color { if (titleColor == "Blue") return Color.Blue; if (titleColor == "Green") return Color.Green; if (titleColor == "Red") return Color.Red; return Color.Yellow }
    fun kBg(): Color { if (tickerColor == "White") return Color.White; if (tickerColor == "Green") return Color.Green; if (tickerColor == "Red") return Color.Red; return Color.Yellow }
}

@Composable
fun JstvScreen(hasPerm: Boolean, isLand: Boolean, onAsk: () -> Unit, onRotate: () -> Unit, onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit, onSetCam: (SurfaceView) -> Unit, onPreview: () -> Unit, onStart: (String) -> Boolean, onStop: () -> Unit, onSwitchCam: () -> Unit, onMute: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    val s = remember { AppState() }
    LaunchedEffect(hasPerm) { if (hasPerm && s.appMode == "MAIN") onPreview() }
    LaunchedEffect(Unit) { while (true) { s.now = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()); delay(1000) } }
    LaunchedEffect(s.introVis) { if (s.introVis) { delay(5000); s.introVis = false } }
    val inf = rememberInfiniteTransition(label = "ticker")
    val tx by inf.animateFloat(400f, -800f, infiniteRepeatable(tween((s.tickerSpeed.toInt() * 1000).coerceIn(2000, 20000), easing = LinearEasing), RepeatMode.Restart), label = "tx")
    fun goLive() {
        if (s.freeForAll) { s.status = "FREE FOR ALL - LIVE"; s.streaming = true; s.showSave = true; onToast("Free For All - Creator allowed free"); return }
        if (!s.isPremium) { onToast("Premium Required - Go to SETUP > PREMIUM or Ask Creator"); return }
        if (!hasPerm) { onToast("Grant Camera + Mic first"); onAsk(); return }
        val on = s.onList()
        if (on.isEmpty()) { onToast("Turn ON 1 Channel"); return }
        val label = on.joinToString(" + ") { it.platform }
        var url = ""
        if (on.size == 1) { if (on[0].key.isBlank()) { onToast("Add Key for ${on[0].platform}"); return }; url = s.urlFor(on[0].platform, on[0].key) }
        else { if (s.restreamKey.isBlank()) { onToast("Add JSTV Cloud Key for 2+ Channels"); return }; url = "rtmp://live.restream.io/live/${s.restreamKey}" }
        s.status = "CONNECTING to $label"
        if (onStart(url)) { s.streaming = true; s.showSave = true; s.status = "LIVE on $label" } else s.status = "FAILED - Check Key"
    }
    fun endLive() { onStop(); s.streaming = false; s.showSave = false; s.status = "ENDED" }
    fun shareApp() {
        val txt = "Download JSTV App - Jehovah Shalom TV - Install same app, choose MAIN / REMOTE / CAMERA. Ask Kevin for APK via WhatsApp ${s.adminWhats}"
        val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, txt); ctx.startActivity(Intent.createChooser(i, "Share JSTV App"))
    }
    // SMALL ROUTER ONLY - like Prism
    when (s.appMode) {
        "CHOOSE" -> ChooseScreen(s, onPreview, ::shareApp)
        "ADMIN" -> AdminScreen(s, onToast)
        "REMOTE" -> RemoteScreen(s, onToast, onSwitchCam)
        "CAMERA" -> CameraScreen(s, onSetCam, onSwitchCam)
        else -> {
            if (s.tab == "BROADCAST") BroadcastScreen(s, tx, onSetCam, onSwitchCam, onMute, onRotate, ::goLive, ::endLive)
            else SetupScreen(s, onAsk, hasPerm, onPickLogo, onPickVideo, onToast)
        }
    }
}

@Composable
fun ChooseScreen(s: AppState, onPreview: () -> Unit, shareApp: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("JSTV", color = Color.Yellow, fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("JEHOVAH SHALOM TV", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Who Are You? Choose Your Phone Role:", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { s.appMode = "MAIN"; onPreview() }, modifier = Modifier.fillMaxWidth().height(70.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("🎬 MAIN STUDIO\nGoes LIVE to YouTube", color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { s.appMode = "REMOTE" }, modifier = Modifier.fillMaxWidth().height(70.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text("📱 REMOTE CONTROL\nControl Main From Far", color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { s.appMode = "CAMERA" }, modifier = Modifier.fillMaxWidth().height(70.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("📷 WIRELESS CAMERA\n2nd Phone As Camera", color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { s.appMode = "ADMIN" }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("🔐 CREATOR / ADMIN", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(12.dp))
        Text("Same App On All Phones - Just Choose Different Mode", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { shareApp() }) { Text("SHARE JSTV APP TO 2ND PHONE") }
    }
}

@Composable
fun AdminScreen(s: AppState, onToast: (String) -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🔐 CREATOR / ADMIN PANEL", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Only Kevin - Do NOT Share Password", color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        if (!s.adminLogged) {
            Text("Enter Admin Password:", color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.adminPass, onValueChange = { s.adminPass = it }, label = { Text("Admin Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { if (s.adminPass == "516600") { s.adminLogged = true; onToast("Welcome Creator Kevin!") } else onToast("Wrong Password") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("LOGIN AS CREATOR", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { s.appMode = "CHOOSE" }) { Text("⬅ BACK") }
        } else {
            Text("Welcome Creator Kevin! 🙏", color = Color.Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Total Codes: ${s.premiumCodes.size} | Active: ${s.premiumCodes.count { it.active }}", color = Color.Yellow)
            Spacer(Modifier.height(12.dp))
            Text("💰 1) CHANGE PRICES", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.priceDaily, onValueChange = { s.priceDaily = it }, label = { Text("Daily Price - KES - Now 200") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.priceMonthly, onValueChange = { s.priceMonthly = it }, label = { Text("Monthly Price - KES") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.price3M, onValueChange = { s.price3M = it }, label = { Text("3 Months Price - KES") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.priceYear, onValueChange = { s.priceYear = it }, label = { Text("Year Price - KES") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Text("Now: Daily ${s.priceDaily} | Monthly ${s.priceMonthly} | 3M ${s.price3M} | Year ${s.priceYear}", color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text("🏦 2) PAYMENT", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.paybill, onValueChange = { s.paybill = it }, label = { Text("Paybill") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.payAcc, onValueChange = { s.payAcc = it }, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.adminWhats, onValueChange = { s.adminWhats = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text("🎁 3) FREE FOR ALL", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { s.freeForAll = !s.freeForAll }, colors = ButtonDefaults.buttonColors(containerColor = if (s.freeForAll) Color.Green else Color.DarkGray)) { Text(if (s.freeForAll) "TURN FREE OFF" else "TURN FREE ON - Sunday Free") }
            Spacer(Modifier.height(12.dp))
            Text("🔑 4) GENERATE CODE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.newCustName, onValueChange = { s.newCustName = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row { listOf("Daily 200","Monthly 1000","3 Months 2500","Year 8000").forEach { p -> Button(onClick = { s.newCustPlan = p }, modifier = Modifier.padding(end = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = if (s.newCustPlan == p) Color.Yellow else Color.DarkGray)) { Text(if (p == "Daily 200") "Daily" else if (p == "Monthly 1000") "Monthly" else if (p == "3 Months 2500") "3M" else "Year", color = if (s.newCustPlan == p) Color.Black else Color.White) } } }
            Spacer(Modifier.height(6.dp))
            Button(onClick = { val c = s.genCode(s.newCustPlan, s.newCustName); onToast("Generated: $c") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("GENERATE CODE FOR ${s.newCustPlan}") }
            if (s.lastGenCode.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().background(Color.DarkGray).padding(10.dp)) { Column { Text("Last Code:", color = Color.White); Text(s.lastGenCode, color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold) } }
                Spacer(Modifier.height(6.dp))
                Button(onClick = { val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, "Your JSTV Premium Code: ${s.lastGenCode} | Plan: ${s.newCustPlan} | Open JSTV > SETUP > PREMIUM > Enter Code. God Bless! - Kevin"); ctx.startActivity(Intent.createChooser(i, "WhatsApp Code")) }) { Text("WHATSAPP CODE TO CUSTOMER") }
            }
            Spacer(Modifier.height(12.dp))
            Text("👥 5) ALL CODES", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            s.premiumCodes.forEach { pc -> Column(Modifier.fillMaxWidth().background(if (pc.active) Color.DarkGray else Color.Red.copy(alpha = 0.3f)).padding(8.dp)) {
                Text("${pc.code} - ${pc.plan}", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Text("Customer: ${pc.customer} | ${if (pc.active) "✅ ACTIVE" else "❌ BLOCKED"}", color = Color.White, fontSize = 12.sp)
                Row { Button(onClick = { s.premiumCodes = s.premiumCodes.map { if (it.code == pc.code) it.copy(active = !it.active) else it } }) { Text(if (pc.active) "BLOCK" else "UNBLOCK") }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.premiumCodes = s.premiumCodes.filter { it.code != pc.code } }) { Text("DELETE") } }
            }; Spacer(Modifier.height(6.dp)) }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { s.adminLogged = false; s.adminPass = ""; s.appMode = "CHOOSE" }, modifier = Modifier.fillMaxWidth()) { Text("LOGOUT ADMIN") }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun RemoteScreen(s: AppState, onToast: (String) -> Unit, onSwitchCam: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("📱 JSTV REMOTE", color = Color.Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        if (!s.remoteConnected) {
            Text("Enter Remote Code From Main Phone", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = s.remoteInputCode, onValueChange = { s.remoteInputCode = it }, label = { Text("Enter Code - e.g. JSTV-2026") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { if (s.remoteInputCode.trim().isNotEmpty()) { s.remoteConnected = true; s.remoteOn = true; onToast("Remote Connected!") } else onToast("Enter Code") }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text("CONNECT AS REMOTE", color = Color.Black, fontWeight = FontWeight.Bold) }
        } else {
            Text("✅ CONNECTED TO MAIN", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Status: ${s.status} | Scene: ${s.scene}", color = Color.Yellow, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { s.streaming = !s.streaming; s.status = if (s.streaming) "LIVE - Started by Remote" else "ENDED - By Remote" }, modifier = Modifier.fillMaxWidth().height(70.dp), colors = ButtonDefaults.buttonColors(containerColor = if (s.streaming) Color.Red else Color.Green)) { Text(if (s.streaming) "⏹ END LIVE (Remote)" else "▶ GO LIVE (Remote)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Button(onClick = { s.muted = !s.muted }, modifier = Modifier.weight(1f).height(60.dp)) { Text(if (s.muted) "UNMUTE" else "MUTE", fontWeight = FontWeight.Bold) }
                Button(onClick = { onSwitchCam(); s.camName = if (s.camName == "BACK") "FRONT" else "BACK" }, modifier = Modifier.weight(1f).height(60.dp)) { Text("CAM: ${s.camName}", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Button(onClick = { s.applyScene("Studio") }, modifier = Modifier.weight(1f)) { Text("STUDIO") }
                Button(onClick = { s.applyScene("Church") }, modifier = Modifier.weight(1f)) { Text("CHURCH") }
                Button(onClick = { s.applyScene("Outdoor") }, modifier = Modifier.weight(1f)) { Text("OUTDOOR") }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { s.remoteConnected = false; s.remoteOn = false }) { Text("DISCONNECT REMOTE") }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { s.appMode = "CHOOSE" }) { Text("⬅ CHANGE MODE") }
    }
}

@Composable
fun CameraScreen(s: AppState, onSetCam: (SurfaceView) -> Unit, onSwitchCam: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("📷 JSTV WIRELESS CAMERA", color = Color.Green, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("This Phone Is Now A Camera Only", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        AndroidView(factory = { v -> SurfaceView(v).also { onSetCam(it) } }, modifier = Modifier.fillMaxWidth().height(300.dp))
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onSwitchCam(); s.camName = if (s.camName == "BACK") "FRONT" else "BACK" }) { Text("SWITCH CAMERA: ${s.camName}") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { s.appMode = "CHOOSE" }) { Text("⬅ CHANGE MODE") }
        Spacer(Modifier.height(8.dp))
        Text("Leave this phone on tripod. Keep WiFi ON.", color = Color.Green, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun BroadcastScreen(s: AppState, tx: Float, onSetCam: (SurfaceView) -> Unit, onSwitchCam: () -> Unit, onMute: (Boolean) -> Unit, onRotate: () -> Unit, goLive: () -> Unit, endLive: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black).clickable { if (s.hideUI) s.hideUI = false }) {
        AndroidView(factory = { v -> SurfaceView(v).also { onSetCam(it) } }, modifier = Modifier.fillMaxSize())
        if (s.hideUI) {
            Column(Modifier.align(Alignment.TopCenter).padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) { s.titles.forEach { Text(it.text, color = s.tBg(), fontSize = s.titleSize.sp, fontWeight = if (s.titleBold) FontWeight.Bold else FontWeight.Normal) } }
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp).background(Color.Black.copy(alpha = 0.7f)).padding(6.dp)) { Text(s.tickerText, color = s.kBg(), fontSize = s.tickerSize.sp) }
        } else {
            if (s.showTitle) { Column(Modifier.align(Alignment.TopCenter).padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) { s.titles.forEach { Text(it.text, color = s.tBg(), fontSize = s.titleSize.sp, fontWeight = if (s.titleBold) FontWeight.Bold else FontWeight.Normal) } } }
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 270.dp + s.tickerOff.dp)) {
                if (s.tickerBgOn) { Box(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.7f)).padding(6.dp)) { if (s.tickerMoving) Text(s.tickerText, color = s.kBg(), fontSize = s.tickerSize.sp, modifier = Modifier.offset(x = tx.dp)) else Text(s.tickerText, color = s.kBg(), fontSize = s.tickerSize.sp, modifier = Modifier.fillMaxWidth()) } }
                else { Text(s.tickerText, color = s.kBg(), fontSize = s.tickerSize.sp, modifier = Modifier.fillMaxWidth()) }
            }
            if (s.showIntro && s.introVis) { Box(Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.8f)).padding(16.dp)) { Text(s.introText, color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) } }
            if (s.extCamOn) { Text("📷 WIRELESS EXT CAM ON - ${s.extCamMode} - ${s.extCamIP}", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp).background(Color.Black.copy(alpha = 0.6f)).padding(4.dp)) }
            if (s.remoteOn) { Text("📱 REMOTE ON - Code: ${s.remoteCode}", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp).background(Color.Black.copy(alpha = 0.6f)).padding(4.dp)) }
            if (s.chatOn && s.showChatBox) { Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp).background(Color.Black.copy(alpha = 0.7f)).padding(8.dp)) { Text("💬 LIVE CHAT\n${s.chatText}", color = Color.White, fontSize = 12.sp) } }
            Column(Modifier.align(Alignment.BottomCenter).padding(8.dp).background(Color.Black.copy(alpha = 0.6f))) {
                Text("MAIN STUDIO | ${s.status} | ${s.now} | Scene: ${s.scene} | ${s.resolution} ${s.fps} fps ${s.bitrate.toInt()}kbps", color = Color.White, fontSize = 11.sp)
                Text("ON: " + s.onList().joinToString(", ") { it.platform }, color = Color.Yellow, fontSize = 11.sp)
                if (s.isPremium) Text("⭐ PREMIUM ACTIVE - ${s.selectedPlan}", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (s.freeForAll) Text("🎁 FREE FOR ALL", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { goLive() }, modifier = Modifier.weight(1f)) { Text(if (s.streaming) "LIVE..." else "GO LIVE") }
                    Button(onClick = { onSwitchCam(); s.camName = if (s.camName == "BACK") "FRONT" else "BACK" }, modifier = Modifier.weight(1f)) { Text("CAM: ${s.camName}") }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onRotate() }, modifier = Modifier.weight(1f)) { Text(if (s.hideUI) "PORTRAIT" else "LANDSCAPE") }
                    Button(onClick = { s.muted = !s.muted; onMute(s.muted) }, modifier = Modifier.weight(1f)) { Text(if (s.muted) "UNMUTE" else "MUTE") }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { s.applyScene(if (s.scene == "Studio") "Church" else if (s.scene == "Church") "Outdoor" else "Studio") }, modifier = Modifier.weight(1f)) { Text("SCENE: ${s.scene}") }
                    Button(onClick = { if (s.extCamOn) { s.extCamOn = false; s.extCamStatus = "Not Connected" } else { if (s.extCamIP.isBlank()) { s.tab = "SETUP" } else { s.extCamOn = true; s.extCamStatus = "Connected - ${s.extCamIP}" } } }, modifier = Modifier.weight(1f)) { Text(if (s.extCamOn) "EXT ON" else "EXT CAM") }
                    Button(onClick = { s.remoteOn = !s.remoteOn }, modifier = Modifier.weight(1f)) { Text(if (s.remoteOn) "REMOTE ON" else "REMOTE") }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { s.hideUI = true }, modifier = Modifier.weight(1f)) { Text("⛶ FULLSCREEN") }
                    Button(onClick = { s.showChatBox = !s.showChatBox }, modifier = Modifier.weight(1f)) { Text(if (s.showChatBox) "HIDE CHAT" else "CHAT") }
                    Button(onClick = { s.beautyOn = !s.beautyOn }, modifier = Modifier.weight(1f)) { Text(if (s.beautyOn) "BEAUTY ON" else "BEAUTY") }
                }
                Spacer(Modifier.height(6.dp))
                Button(onClick = { s.tab = "SETUP" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("⚙️ SETUP - ALL SETTINGS / LIVE STUDIO", color = Color.Black, fontWeight = FontWeight.Bold) }
                if (s.showSave) { Spacer(Modifier.height(6.dp)); Button(onClick = { endLive() }, modifier = Modifier.fillMaxWidth()) { Text("END LIVE") } }
            }
        }
    }
}

@Composable
fun SetupScreen(s: AppState, onAsk: () -> Unit, hasPerm: Boolean, onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(14.dp)) {
        Text("⚙️ SETUP - PRISM LIVE STUDIO", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Status: ${s.status} | ${s.now} | Scene: ${s.scene}", color = Color.White)
        Spacer(Modifier.height(6.dp))
        Button(onClick = { s.appMode = "CHOOSE" }) { Text("⬅ CHANGE MODE") }
        if (!hasPerm) { Spacer(Modifier.height(8.dp)); Button(onClick = { onAsk() }) { Text("ALLOW CAMERA + MIC") } }
        Spacer(Modifier.height(12.dp))
        SetupChannels(s, onToast)
        SetupTitlesAndTicker(s, onPickLogo, onPickVideo, onToast)
        SetupPremium(s, onToast)
        SetupStudio(s, onToast)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { s.tab = "BROADCAST" }, modifier = Modifier.fillMaxWidth()) { Text("⬅ BACK TO BROADCAST") }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun SetupChannels(s: AppState, onToast: (String) -> Unit) {
    Text("1) MY CHANNELS - Up to 6", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    s.channels.forEach { ch -> Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = ch.enabled, onCheckedChange = { v -> s.channels = s.channels.map { if (it.id == ch.id) it.copy(enabled = v) else it } })
        Text("${ch.platform}: ${if (ch.key.isBlank()) "NO KEY" else "KEY SET"}", color = Color.White, modifier = Modifier.weight(1f))
        Button(onClick = { s.channels = s.channels.filter { it.id != ch.id } }) { Text("DEL") }
    } }
    Text("ON now: " + s.onList().joinToString(", ") { it.platform }, color = Color.Yellow)
    Spacer(Modifier.height(8.dp))
    Text("+ ADD CHANNEL", fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
    Row { listOf("YouTube", "Facebook", "TikTok", "Instagram", "Twitch", "Kick", "Custom").forEach { p -> Button(onClick = { s.newPlat = p }, modifier = Modifier.padding(end = 4.dp)) { Text(p) } } }
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = s.newKey, onValueChange = { s.newKey = it }, label = { Text("Key for ${s.newPlat}") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Button(onClick = { if (s.channels.size >= 6) onToast("Max 6 channels") else if (s.newKey.isBlank()) onToast("Enter Key") else { s.channels = s.channels + Channel(s.nextCh, s.newPlat, s.newKey, true); s.nextCh++; s.newKey = ""; onToast("Added ${s.newPlat}") } }) { Text("ADD ${s.newPlat}") }
    Spacer(Modifier.height(12.dp))
    Text("2) JSTV CLOUD KEY - For 2+ Channels", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    OutlinedTextField(value = s.restreamKey, onValueChange = { s.restreamKey = it }, label = { Text("Restream Key") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Text("3) INTRO", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    OutlinedTextField(value = s.introText, onValueChange = { s.introText = it }, label = { Text("Intro Text") }, modifier = Modifier.fillMaxWidth())
    Row { Button(onClick = { s.showIntro = !s.showIntro }) { Text(if (s.showIntro) "INTRO ON" else "INTRO OFF") }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.introVis = true }) { Text("SHOW INTRO NOW") } }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun SetupTitlesAndTicker(s: AppState, onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit) {
    Text("5) LOGO", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Row { Button(onClick = { onPickLogo { s.logoUri = it } }) { Text("PICK LOGO") }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.showLogo = !s.showLogo }) { Text(if (s.showLogo) "HIDE LOGO" else "SHOW LOGO") } }
    Spacer(Modifier.height(12.dp))
    Text("6) TITLES", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    OutlinedTextField(value = s.newTitle, onValueChange = { s.newTitle = it }, label = { Text("New Title") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Button(onClick = { if (s.newTitle.isNotBlank()) { s.titles = s.titles + TitleItem(s.nextId, s.newTitle); s.nextId++; s.newTitle = "" } }) { Text("ADD TITLE") }
    Spacer(Modifier.height(6.dp))
    s.titles.forEach { t -> Row(verticalAlignment = Alignment.CenterVertically) { Text(t.text, color = Color.White, modifier = Modifier.weight(1f)); Button(onClick = { s.titles = s.titles.filter { it.id != t.id } }) { Text("DEL") } } }
    Spacer(Modifier.height(8.dp))
    Text("THEME - Title Color", fontSize = 16.sp, color = Color.White)
    Row { listOf("Red", "Blue", "Green", "Yellow").forEach { c -> Button(onClick = { s.titleColor = c }, modifier = Modifier.padding(end = 4.dp)) { Text(c) } } }
    Spacer(Modifier.height(6.dp))
    Text("FONT", fontSize = 16.sp, color = Color.White)
    Row { listOf("Normal", "Serif", "Mono").forEach { f -> Button(onClick = { s.titleFont = f }, modifier = Modifier.padding(end = 4.dp)) { Text(f) } } }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("SIZE: ${s.titleSize.toInt()}", color = Color.White); Slider(value = s.titleSize, onValueChange = { s.titleSize = it }, valueRange = 12f..40f, modifier = Modifier.weight(1f)) }
    Row { Button(onClick = { s.titleBold = !s.titleBold }) { Text(if (s.titleBold) "BOLD ON" else "BOLD OFF") } }
    Spacer(Modifier.height(12.dp))
    Text("7) MOVING TEXT", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
    OutlinedTextField(value = s.tickerText, onValueChange = { s.tickerText = it }, label = { Text("Ticker Text") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    Spacer(Modifier.height(6.dp))
    Box(Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) { Text(s.tickerText, color = s.kBg(), fontSize = s.tickerSize.sp) }
    Spacer(Modifier.height(6.dp))
    Text("SIZE: " + s.tickerSize.toInt(), color = Color.White)
    Slider(value = s.tickerSize, onValueChange = { s.tickerSize = it }, valueRange = 10f..30f)
    Row { Button(onClick = { s.tickerSize = 12f }) { Text("SMALL") }; Button(onClick = { s.tickerSize = 16f }) { Text("MED") }; Button(onClick = { s.tickerSize = 22f }) { Text("BIG") } }
    Spacer(Modifier.height(6.dp))
    Text("COLOR:", color = Color.White)
    Row { listOf("Yellow", "White", "Green", "Red").forEach { c -> Button(onClick = { s.tickerColor = c }, modifier = Modifier.padding(end = 4.dp)) { Text(c) } } }
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { s.tickerOff = s.tickerOff - 20f }) { Text("⬆ UP") }; Button(onClick = { s.tickerOff = s.tickerOff + 20f }) { Text("⬇ DOWN") }; Button(onClick = { s.tickerOff = 0f }) { Text("RESET") } }
    Spacer(Modifier.height(6.dp))
    Row { listOf("Left", "Center", "Right").forEach { a -> Button(onClick = { s.tickerAlign = a }, modifier = Modifier.padding(end = 4.dp)) { Text(a) } } }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("BG:", color = Color.White); Checkbox(checked = s.tickerBgOn, onCheckedChange = { s.tickerBgOn = it }); Text("Background", color = Color.White) }
    Row(verticalAlignment = Alignment.CenterVertically) { Text("MOVING:", color = Color.White); Checkbox(checked = s.tickerMoving, onCheckedChange = { s.tickerMoving = it }); Text("Scroll", color = Color.White) }
    Row { Button(onClick = { s.tickerSpeed = (s.tickerSpeed - 10f).coerceAtLeast(10f) }) { Text("SLOWER") }; Button(onClick = { s.tickerSpeed = (s.tickerSpeed + 10f).coerceAtMost(100f) }) { Text("FASTER") } }
    Spacer(Modifier.height(12.dp))
    Text("8) GALLERY + LOOP", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    Button(onClick = { onPickVideo { uri -> s.galleryUri = uri } }) { Text("PICK VIDEO") }
    Text(if (s.galleryUri == null) "No video picked" else "Video picked", color = Color.White.copy(alpha = 0.7f))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("LOOP:", color = Color.White); Checkbox(checked = s.galleryLoop, onCheckedChange = { s.galleryLoop = it }); Text("Loop Video", color = Color.White) }
    Button(onClick = { if (s.galleryUri == null) onToast("Pick video first") else { s.isGallery = !s.isGallery } }) { Text(if (s.isGallery) "STOP GALLERY" else "PLAY GALLERY") }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun SetupPremium(s: AppState, onToast: (String) -> Unit) {
    Text("10) JSTV PREMIUM - USER SIDE", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
    Text("Daily ${s.priceDaily} | Monthly ${s.priceMonthly} | 3 Months ${s.price3M} | Year ${s.priceYear}", color = Color.White)
    Text("Paybill ${s.paybill} Acc ${s.payAcc} | WhatsApp ${s.adminWhats}", color = Color.White, fontSize = 11.sp)
    if (s.freeForAll) Text("🎁 FREE FOR ALL IS ON!", color = Color.Green, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { s.selectedPlan = "Daily ${s.priceDaily}" }, colors = ButtonDefaults.buttonColors(containerColor = if (s.selectedPlan.contains("Daily")) Color.Yellow else Color.DarkGray)) { Text("Daily ${s.priceDaily}", color = if (s.selectedPlan.contains("Daily")) Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.selectedPlan = "Monthly ${s.priceMonthly}" }, colors = ButtonDefaults.buttonColors(containerColor = if (s.selectedPlan.contains("Monthly")) Color.Yellow else Color.DarkGray)) { Text("Monthly ${s.priceMonthly}", color = if (s.selectedPlan.contains("Monthly")) Color.Black else Color.White) } }
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { s.selectedPlan = "3 Months ${s.price3M}" }, colors = ButtonDefaults.buttonColors(containerColor = if (s.selectedPlan.contains("3 Months")) Color.Yellow else Color.DarkGray)) { Text("3M ${s.price3M}", color = if (s.selectedPlan.contains("3 Months")) Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.selectedPlan = "Year ${s.priceYear}" }, colors = ButtonDefaults.buttonColors(containerColor = if (s.selectedPlan.contains("Year")) Color.Yellow else Color.DarkGray)) { Text("Year ${s.priceYear}", color = if (s.selectedPlan.contains("Year")) Color.Black else Color.White) } }
    Spacer(Modifier.height(6.dp))
    Text("Selected: ${s.selectedPlan}", color = Color.Yellow, fontWeight = FontWeight.Bold)
    Text("1. M-PESA > Paybill ${s.paybill} | 2. Acc ${s.payAcc} | 3. WhatsApp receipt ${s.adminWhats} | 4. Enter Code", color = Color.White, fontSize = 11.sp)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = s.premiumCode, onValueChange = { s.premiumCode = it }, label = { Text("Enter Premium Code") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Button(onClick = {
        if (s.freeForAll) { s.isPremium = true; s.premiumMessage = "✅ Free For All!" }
        else if (s.premiumCodes.any { it.code == s.premiumCode.trim().uppercase() && it.active }) { s.isPremium = true; s.premiumMessage = "✅ Welcome to Premium! ${s.selectedPlan} Active" }
        else { s.premiumMessage = "❌ Wrong Code or Blocked. Pay via Paybill ${s.paybill} then WhatsApp ${s.adminWhats}" }
    }, modifier = Modifier.fillMaxWidth()) { Text("UNLOCK PREMIUM - ${s.selectedPlan}") }
    if (s.premiumMessage.isNotEmpty()) Text(s.premiumMessage, color = if (s.isPremium) Color.Green else Color.Red)
    if (s.isPremium) { Spacer(Modifier.height(6.dp)); Button(onClick = { s.isPremium = false; s.premiumMessage = "Logged out" }) { Text("LOGOUT PREMIUM") } }
    Spacer(Modifier.height(6.dp))
    Button(onClick = { s.appMode = "ADMIN" }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("🔐 OPEN CREATOR / ADMIN PANEL", color = Color.White) }
    Spacer(Modifier.height(20.dp))
}

@Composable
fun SetupStudio(s: AppState, onToast: (String) -> Unit) {
    val ctx = LocalContext.current
    Text("11) SCENES - 1 Tap", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
    Text("Now: ${s.scene}", color = Color.White)
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { s.applyScene("Studio"); onToast("Scene: Studio") }, colors = ButtonDefaults.buttonColors(containerColor = if (s.scene == "Studio") Color.Yellow else Color.DarkGray)) { Text("STUDIO", color = if (s.scene == "Studio") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.applyScene("Church"); onToast("Scene: Church") }, colors = ButtonDefaults.buttonColors(containerColor = if (s.scene == "Church") Color.Yellow else Color.DarkGray)) { Text("CHURCH", color = if (s.scene == "Church") Color.Black else Color.White) }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.applyScene("Outdoor"); onToast("Scene: Outdoor") }, colors = ButtonDefaults.buttonColors(containerColor = if (s.scene == "Outdoor") Color.Yellow else Color.DarkGray)) { Text("OUTDOOR", color = if (s.scene == "Outdoor") Color.Black else Color.White) } }
    Spacer(Modifier.height(20.dp))
    Text("12) EXTERNAL CAMERA - WIRELESS", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
    Text("Use 2nd Phone as Camera via WiFi. Both phones on SAME WiFi.", color = Color.White, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
    Text("TYPE:", color = Color.White, fontWeight = FontWeight.Bold)
    Row { listOf("WiFi Phone", "IP Camera").forEach { m -> Button(onClick = { s.extCamMode = m }, colors = ButtonDefaults.buttonColors(containerColor = if (s.extCamMode == m) Color.Yellow else Color.DarkGray), modifier = Modifier.padding(end = 4.dp)) { Text(m, color = if (s.extCamMode == m) Color.Black else Color.White) } } }
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = s.extCamIP, onValueChange = { s.extCamIP = it }, label = { Text("2nd Phone IP - e.g. 192.168.1.10:8080") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Text("Status: ${s.extCamStatus}", color = if (s.extCamOn) Color.Green else Color.White, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { if (s.extCamIP.isBlank()) onToast("Enter 2nd Phone IP first") else { s.extCamOn = true; s.extCamStatus = "Connected - ${s.extCamIP}"; onToast("Wireless Ext Cam Connected") } }) { Text("CONNECT WIRELESS CAM") }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.extCamOn = false; s.extCamStatus = "Not Connected" }) { Text("DISCONNECT") } }
    Spacer(Modifier.height(6.dp))
    Button(onClick = { val txt = "Join as JSTV Wireless Camera - Open browser on 2nd phone, go to: ${s.extCamIP}"; val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, txt); ctx.startActivity(Intent.createChooser(i, "Share Ext Cam Link")) }) { Text("SHARE EXT CAM LINK") }
Spacer(Modifier.height(6.dp))
Row(verticalAlignment = Alignment.CenterVertically) { Text("EXT CAM:", color = Color.White); Checkbox(checked = s.extCamOn, onCheckedChange = { if (it) { if (s.extCamIP.isBlank()) onToast("Enter IP first") else { s.extCamOn = true; s.extCamStatus = "Connected" } } else { s.extCamOn = false; s.extCamStatus = "Not Connected" } }); Text(if (s.extCamOn) "ON - Wireless" else "OFF - Phone Cam", color = Color.White) }
Spacer(Modifier.height(20.dp))
Text("13) REMOTE CONTROL", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
OutlinedTextField(value = s.remoteCode, onValueChange = { s.remoteCode = it }, label = { Text("Remote Code") }, modifier = Modifier.fillMaxWidth())
Spacer(Modifier.height(6.dp))
Row(verticalAlignment = Alignment.CenterVertically) { Text("REMOTE:", color = Color.White); Checkbox(checked = s.remoteOn, onCheckedChange = { s.remoteOn = it }); Text(if (s.remoteOn) "ON - Code: ${s.remoteCode}" else "OFF", color = Color.White) }
Spacer(Modifier.height(6.dp))
Row { Button(onClick = { s.remoteOn = true; s.remoteMsg = "Remote ON - Code ${s.remoteCode}" }) { Text("TURN REMOTE ON") }; Spacer(Modifier.width(6.dp)); Button(onClick = { val txt = "Join JSTV as REMOTE - Enter Code: ${s.remoteCode}"; val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, txt); ctx.startActivity(Intent.createChooser(i, "Share Remote Code")) }) { Text("SHARE CODE") } }
Spacer(Modifier.height(20.dp))
Text("14) LIVE STUDIO - LIKE PRISM", fontSize = 18.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
Spacer(Modifier.height(8.dp))
Text("QUALITY - Resolution", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
Row { listOf("720p", "1080p").forEach { r -> Button(onClick = { s.resolution = r }, colors = ButtonDefaults.buttonColors(containerColor = if (s.resolution == r) Color.Yellow else Color.DarkGray), modifier = Modifier.padding(end = 4.dp)) { Text(r, color = if (s.resolution == r) Color.Black else Color.White) } } }
Spacer(Modifier.height(6.dp))
Text("FPS: ${s.fps}", color = Color.White)
Row { listOf("30", "60").forEach { f -> Button(onClick = { s.fps = f }, colors = ButtonDefaults.buttonColors(containerColor = if (s.fps == f) Color.Yellow else Color.DarkGray), modifier = Modifier.padding(end = 4.dp)) { Text("$f FPS", color = if (s.fps == f) Color.Black else Color.White) } } }
Spacer(Modifier.height(6.dp))
Text("BITRATE: ${s.bitrate.toInt()} kbps", color = Color.White)
Slider(value = s.bitrate, onValueChange = { s.bitrate = it }, valueRange = 1000f..6000f)
Row { Button(onClick = { s.bitrate = 1500f }) { Text("LOW - Slow Net") }; Button(onClick = { s.bitrate = 2500f }) { Text("MED") }; Button(onClick = { s.bitrate = 4500f }) { Text("HIGH - Clear") } }
Spacer(Modifier.height(12.dp))
Text("AUDIO MIXER", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
Text("MIC Volume: ${s.micVol.toInt()}%", color = Color.White)
Slider(value = s.micVol, onValueChange = { s.micVol = it }, valueRange = 0f..100f)
Text("MUSIC Volume: ${s.musicVol.toInt()}%", color = Color.White)
Slider(value = s.musicVol, onValueChange = { s.musicVol = it }, valueRange = 0f..100f)
Row { Button(onClick = { s.muted = !s.muted }) { Text(if (s.muted) "UNMUTE MIC" else "MUTE MIC") }; Spacer(Modifier.width(6.dp)); Button(onClick = { s.musicVol = 0f }) { Text("MUTE MUSIC") } }
Spacer(Modifier.height(12.dp))
Text("FILTERS", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
Row(verticalAlignment = Alignment.CenterVertically) { Text("BEAUTY:", color = Color.White); Checkbox(checked = s.beautyOn, onCheckedChange = { s.beautyOn = it }); Text(if (s.beautyOn) "ON" else "OFF", color = Color.White) }
Text("BRIGHTNESS: ${s.brightness.toInt()}%", color = Color.White)
Slider(value = s.brightness, onValueChange = { s.brightness = it }, valueRange = 0f..100f)
Row { Button(onClick = { s.brightness = 30f }) { Text("DARK") }; Button(onClick = { s.brightness = 50f }) { Text("NORMAL") }; Button(onClick = { s.brightness = 80f }) { Text("BRIGHT") } }
Spacer(Modifier.height(12.dp))
Text("LIVE CHAT", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
Row(verticalAlignment = Alignment.CenterVertically) { Text("SHOW CHAT:", color = Color.White); Checkbox(checked = s.chatOn, onCheckedChange = { s.chatOn = it }); Text(if (s.chatOn) "ON" else "OFF", color = Color.White) }
OutlinedTextField(value = s.chatText, onValueChange = { s.chatText = it }, label = { Text("Chat Preview Text") }, modifier = Modifier.fillMaxWidth())
Spacer(Modifier.height(6.dp))
Button(onClick = { s.showChatBox = !s.showChatBox }) { Text(if (s.showChatBox) "HIDE CHAT ON SCREEN" else "SHOW CHAT ON SCREEN") }
Spacer(Modifier.height(12.dp))
Text("COUNTDOWN", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
Row(verticalAlignment = Alignment.CenterVertically) { Text("COUNTDOWN:", color = Color.White); Checkbox(checked = s.countdownOn, onCheckedChange = { s.countdownOn = it }); Text(if (s.countdownOn) "ON - 3-2-1" else "OFF", color = Color.White) }
Spacer(Modifier.height(6.dp))
Button(onClick = { s.hideUI = true; s.tab = "BROADCAST" }) { Text("GO FULLSCREEN CLEAN NOW") }
}
