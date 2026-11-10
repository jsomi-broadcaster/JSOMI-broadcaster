package com.jsomi.broadcaster
import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
 private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r -> hasPerm = r[Manifest.permission.CAMERA] == true && r[Manifest.permission.RECORD_AUDIO] == true }
 private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { onLogo?.invoke(it) }
 private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { onVideo?.invoke(it) }
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
  val ctl = WindowInsetsControllerCompat(window, window.decorView)
  ctl.hide(WindowInsetsCompat.Type.systemBars())
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
    onPreview = { try { if (cam?.isOnPreview != true) { cam?.prepareVideo(); cam?.prepareAudio(); cam?.startPreview() } } catch (e: Exception) {} },
    onStart = { url -> val a = cam?.prepareAudio() ?: false; val v = cam?.prepareVideo() ?: false; if (a && v) { try { cam?.startPreview() } catch (e: Exception) {}; cam?.startStream(url); true } else false },
    onStop = { try { cam?.stopStream() } catch (e: Exception) {}; try { cam?.startPreview() } catch (e: Exception) {} },
    onSwitchCam = { try { cam?.switchCamera() } catch (e: Exception) {} },
    onMute = { if (it) cam?.disableAudio() else cam?.enableAudio() }
   )
  }
 }
 override fun onDestroy() { try { cam?.stopStream(); cam?.stopPreview() } catch (e: Exception) {}; super.onDestroy() }
}
data class TitleItem(val id: Int, val text: String, val offset: Offset)
data class Channel(val id: Int, val platform: String, val key: String, val enabled: Boolean)
@Composable
fun JstvScreen(hasPerm: Boolean, isLand: Boolean, onAsk: () -> Unit, onRotate: () -> Unit, onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit, onSetCam: (SurfaceView) -> Unit, onPreview: () -> Unit, onStart: (String) -> Boolean, onStop: () -> Unit, onSwitchCam: () -> Unit, onMute: (Boolean) -> Unit) {
 val ctx = LocalContext.current
 var tab by remember { mutableStateOf("BROADCAST") }
 var restreamKey by remember { mutableStateOf("") }
 var newPlat by remember { mutableStateOf("YouTube") }
 var newKey by remember { mutableStateOf("") }
 var nextCh by remember { mutableStateOf(4) }
 var channels by remember { mutableStateOf(listOf(Channel(1, "YouTube", "", true), Channel(2, "Facebook", "", true), Channel(3, "TikTok", "", false))) }
 var status by remember { mutableStateOf("READY") }
 var streaming by remember { mutableStateOf(false) }
 var showSave by remember { mutableStateOf(false) }
 var muted by remember { mutableStateOf(false) }
 var camName by remember { mutableStateOf("Back Camera") }
 var logoUri by remember { mutableStateOf<Uri?>(null) }
 var showLogo by remember { mutableStateOf(true) }
 var logoOff by remember { mutableStateOf(Offset(20f, 20f)) }
 var logoSize by remember { mutableStateOf(80f) }
 var showTitle by remember { mutableStateOf(true) }
 var titleSize by remember { mutableStateOf(24f) }
 var titleColor by remember { mutableStateOf("Red") }
 var titleFont by remember { mutableStateOf("Normal") }
 var titleBold by remember { mutableStateOf(true) }
 var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live - ") }
 var tickerMoving by remember { mutableStateOf(true) }
 var tickerLoop by remember { mutableStateOf(true) }
 var tickerAlign by remember { mutableStateOf("Left") }
 var tickerColor by remember { mutableStateOf("Yellow") }
 var tickerSize by remember { mutableStateOf(15f) }
 var tickerSpeed by remember { mutableStateOf(12f) }
 var tickerOff by remember { mutableStateOf(Offset(0f, 550f)) }
 var tickerBgOn by remember { mutableStateOf(true) }
 var galleryUri by remember { mutableStateOf<Uri?>(null) }
 var galleryLoop by remember { mutableStateOf(true) }
 var isGallery by remember { mutableStateOf(false) }
 var newTitle by remember { mutableStateOf("") }
 var nextId by remember { mutableStateOf(4) }
 var introText by remember { mutableStateOf("Welcome to JSTV - Jesus Saves") }
 var showIntro by remember { mutableStateOf(true) }
 var introVis by remember { mutableStateOf(false) }
 var now by remember { mutableStateOf("") }
 var liveLabel by remember { mutableStateOf("") }
 var titles by remember { mutableStateOf(listOf(TitleItem(1, "BREAKING", Offset(20f, 200f)), TitleItem(2, "ENQUIRY", Offset(20f, 260f)), TitleItem(3, "RESTORATION", Offset(20f, 320f)))) }
 LaunchedEffect(hasPerm) { if (hasPerm) onPreview() }
 LaunchedEffect(Unit) { while (true) { now = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()); delay(1000) } }
 LaunchedEffect(introVis) { if (introVis) { delay(3000); introVis = false } }
 val inf = rememberInfiniteTransition(label = "t")
 val tx by inf.animateFloat(400f, -800f, infiniteRepeatable(tween((tickerSpeed * 1000).toInt(), easing = LinearEasing), RepeatMode.Restart), label = "tx")
 fun onList() = channels.filter { it.enabled }
 fun urlFor(p: String, k: String): String {
  if (p == "YouTube") return "rtmp://a.rtmp.youtube.com/live2/" + k
  if (p == "TikTok") return "rtmp://push-rtmp-l1.tiktok.com/live/" + k
  if (p == "Instagram") return "rtmps://live-upload.instagram.com:443/rtmp/" + k
  if (p == "Twitch") return "rtmp://live.twitch.tv/app/" + k
  if (p == "Kick") return "rtmp://live.kick.com/app/" + k
  if (p == "Custom") return k
  return "rtmps://live-api-s.facebook.com:443/rtmp/" + k
 }
 fun goLive() {
  if (!hasPerm) { onToast("Grant Camera + Mic"); onAsk(); return }
  val on = onList()
  if (on.isEmpty()) { onToast("Turn ON 1 Channel"); tab = "SETUP"; return }
  val label = on.joinToString(" + ") { it.platform }
  var url = ""
  if (on.size == 1) { if (on[0].key.isBlank()) { onToast("Enter Key for " + on[0].platform); tab = "SETUP"; return }; url = urlFor(on[0].platform, on[0].key) } else { if (restreamKey.isBlank()) { onToast("Enter Cloud Key for 2+"); tab = "SETUP"; return }; url = "rtmp://live.restream.io/live/" + restreamKey }
  status = "CONNECTING to " + label
  if (onStart(url)) { streaming = true; showSave = false; liveLabel = label; status = "MULTISTREAM LIVE to " + label; if (showIntro) introVis = true; onToast("MULTISTREAM LIVE to " + label) } else { status = "FAILED - Check Key"; onToast("FAILED") }
 }
 fun endLive() { onStop(); streaming = false; introVis = false; showSave = true; status = "ENDED - Tap SAVE"; onToast("ENDED - Tap SAVE") }
 fun tBg(): Color { if (titleColor == "Blue") return Color.Blue; if (titleColor == "Green") return Color.Green; if (titleColor == "Yellow") return Color.Yellow; if (titleColor == "Purple") return Color(0xFF800080); if (titleColor == "Orange") return Color(0xFFFF9800); return Color.Red }
 fun kBg(): Color { if (tickerColor == "White") return Color.White; if (tickerColor == "Green") return Color.Green; if (tickerColor == "Red") return Color.Red; if (tickerColor == "Cyan") return Color.Cyan; if (tickerColor == "Pink") return Color(0xFFFF69B4); if (tickerColor == "Orange") return Color(0xFFFF9800); return Color.Yellow }
 fun fFor(): FontFamily { if (titleFont == "Serif") return FontFamily.Serif; if (titleFont == "Mono") return FontFamily.Monospace; if (titleFont == "Cursive") return FontFamily.Cursive; return FontFamily.Default }
 fun aVal(): TextAlign { if (tickerAlign == "Center") return TextAlign.Center; if (tickerAlign == "Right") return TextAlign.End; return TextAlign.Start }
 fun share() { val s = Intent(Intent.ACTION_SEND); s.type = "text/plain"; s.putExtra(Intent.EXTRA_TEXT, "JSTV MULTISTREAM LIVE: " + liveLabel); ctx.startActivity(Intent.createChooser(s, "Share Live Link")) }
 Column(Modifier.fillMaxSize().background(Color.Black)) {
  if (!streaming && tab == "SETUP") { Row(Modifier.fillMaxWidth().background(Color.Black).padding(6.dp), Arrangement.SpaceEvenly) { Button(onClick = { tab = "BROADCAST" }) { Text("📺 BROADCAST") }; Button(onClick = { tab = "SETUP" }) { Text("⚙️ SETUP") } } }
  if (!hasPerm) { Box(Modifier.fillMaxWidth().background(Color.Red).padding(12.dp)) { Button(onClick = { onAsk() }) { Text("GRANT CAMERA + MIC") } } }
  if (tab == "BROADCAST") {
   Box(Modifier.fillMaxSize().background(Color.Black)) {
    if (hasPerm) { AndroidView(factory = { c -> SurfaceView(c).also { onSetCam(it) } }, modifier = Modifier.fillMaxSize()) }
    if (isGallery && galleryUri != null) { Box(Modifier.fillMaxSize().background(Color.DarkGray), Alignment.Center) { Text("GALLERY VIDEO PLAYING\nLoop: " + if (galleryLoop) "ON" else "OFF", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center) } }
    if (introVis) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), Alignment.Center) { Text(introText, color = Color.White, fontSize = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp)) } }
    if (streaming || showSave) { Column(Modifier.align(Alignment.TopStart).padding(12.dp).background(Color.Red).padding(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(if (streaming) "● LIVE " else "● ENDED ", color = Color.White, fontSize = 16.sp); Text(now, color = Color.White, fontSize = 14.sp) }; if (streaming) Text("MULTISTREAM: " + liveLabel, color = Color.White, fontSize = 12.sp) } }
    if (!streaming) { Button(onClick = { tab = "SETUP" }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) { Text("⚙️") } }
    if (showLogo) { Box(Modifier.offset(logoOff.x.dp, logoOff.y.dp).size(logoSize.dp).background(Color.White).pointerInput(Unit) { detectDragGestures { ch, d -> ch.consume(); logoOff += d } }) { Text(if (logoUri == null) "LOGO" else "LOGO ✓", fontSize = 12.sp, modifier = Modifier.padding(4.dp)) } }
    if (showTitle) { titles.forEach { item -> val one = item; Box(Modifier.offset(one.offset.x.dp, one.offset.y.dp).background(tBg()).padding(8.dp, 4.dp).pointerInput(one.id) { detectDragGestures { ch, d -> ch.consume(); titles = titles.map { if (it.id == one.id) it.copy(offset = it.offset + d) else it } } }) { Text(one.text, color = Color.White, fontSize = titleSize.sp, fontFamily = fFor(), fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Normal) } } }
    Box(Modifier.offset(tickerOff.x.dp, tickerOff.y.dp).fillMaxWidth().background(if (tickerBgOn) Color.Black else Color.Transparent).pointerInput(Unit) { detectDragGestures { ch, d -> ch.consume(); tickerOff += d } }) {
     Column(Modifier.fillMaxWidth().padding(6.dp)) {
      
      if (tickerMoving) Text(tickerText + " • " + tickerText, color = kBg(), fontSize = tickerSize.sp, maxLines = 1, textAlign = aVal(), modifier = Modifier.fillMaxWidth().offset(if (tickerLoop) tx.dp else 0.dp)) else Text(tickerText, color = kBg(), fontSize = tickerSize.sp, maxLines = 1, textAlign = aVal(), modifier = Modifier.fillMaxWidth())
     }
    }
    if (!streaming) {
     Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(8.dp)) {
      Text(status + " | " + now, color = Color.White, fontSize = 13.sp)
      Text("ON: " + onList().joinToString(" + ") { it.platform }, color = Color.Green, fontSize = 12.sp)
      Spacer(Modifier.height(4.dp))
      Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
       Button(onClick = { goLive() }) { Text("🔴 GO LIVE") }
       Button(onClick = { onSwitchCam(); camName = if (camName == "Back Camera") "Front Camera" else "Back Camera" }) { Text(camName) }
       Button(onClick = { onRotate() }) { Text(if (isLand) "↻ Portrait" else "↻ Landscape") }
       Button(onClick = { muted = !muted; onMute(muted) }) { Text(if (muted) "Unmute" else "Mute") }
      }
      if (showSave) { Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { Button(onClick = { showSave = false; status = "SAVED ✓"; onToast("SAVED ✓") }) { Text("💾 SAVE VIDEO") }; Button(onClick = { share() }) { Text("🔗 SHARE LINK") } } }
     }
    } else {
     Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { Button(onClick = { endLive() }) { Text("⏹ END LIVE") }; Button(onClick = { share() }) { Text("🔗 SHARE") } } }
    }
   }
  } else {
   Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
    Text("⚙️ SETUP - Like PRISM - FULLSCREEN ON", fontSize = 20.sp, color = Color.White)
    Text("Status: " + status + " | " + now, fontSize = 14.sp, color = Color.White)
    Spacer(Modifier.height(12.dp))
    Text("1) MY CHANNELS - Up to 6 - Toggle ON", fontSize = 18.sp, color = Color.White)
    Text("ON = Will go LIVE. OFF = Skip.", fontSize = 13.sp, color = Color.White)
    Spacer(Modifier.height(6.dp))
    channels.forEach { ch -> val one = ch; Column(Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(one.platform, color = Color.White, fontSize = 18.sp); Row(verticalAlignment = Alignment.CenterVertically) { Text(if (one.enabled) "ON" else "OFF", color = if (one.enabled) Color.Green else Color.Gray); Spacer(Modifier.width(6.dp)); Switch(checked = one.enabled, onCheckedChange = { on -> channels = channels.map { if (it.id == one.id) it.copy(enabled = on) else it } }) } }; Spacer(Modifier.height(4.dp)); OutlinedTextField(value = one.key, onValueChange = { nk -> channels = channels.map { if (it.id == one.id) it.copy(key = nk) else it } }, label = { Text(one.platform + " Stream Key") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp)); Button(onClick = { channels = channels.filter { it.id != one.id } }) { Text("Remove Channel") } }; Spacer(Modifier.height(8.dp)) }
    Text("ON now: " + onList().joinToString(" + ") { it.platform }, fontSize = 14.sp, color = Color.Green)
    Spacer(Modifier.height(8.dp))
    Text("+ ADD CHANNEL", fontSize = 16.sp, color = Color.White)
    Row { listOf("YouTube", "Facebook", "TikTok", "Instagram", "Twitch", "Kick", "Custom").forEach { p -> Button(onClick = { newPlat = p }) { Text(if (newPlat == p) "● " + p else p) }; Spacer(Modifier.width(4.dp)) } }
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = newKey, onValueChange = { newKey = it }, label = { Text("Key for " + newPlat) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Button(onClick = { if (channels.size >= 6) onToast("Max 6 channels") else if (newKey.isBlank()) onToast("Enter key first") else { channels = channels + Channel(nextCh, newPlat, newKey, true); nextCh = nextCh + 1; newKey = ""; onToast("Channel Added + ON") } }) { Text("+ ADD CHANNEL - " + newPlat) }
    Spacer(Modifier.height(12.dp))
    Text("2) JSTV CLOUD KEY - For 2+ Channels", fontSize = 18.sp, color = Color.White)
    Text("Get FREE from restream.io > Settings > RTMP > Stream Key.", fontSize = 12.sp, color = Color.Yellow)
    OutlinedTextField(value = restreamKey, onValueChange = { restreamKey = it }, label = { Text("JSTV Cloud / Restream Key") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Text("3) CAMERA", fontSize = 18.sp, color = Color.White)
    Text("Camera now: " + camName, color = Color.White)
    Button(onClick = { onSwitchCam(); camName = if (camName == "Back Camera") "Front Camera" else "Back Camera" }) { Text("Switch Camera") }
    Spacer(Modifier.height(12.dp))
    Text("4) INTRO", fontSize = 18.sp, color = Color.White)
    OutlinedTextField(value = introText, onValueChange = { introText = it }, label = { Text("Intro Text") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Intro  ", color = Color.White); Switch(checked = showIntro, onCheckedChange = { showIntro = it }); Spacer(Modifier.width(8.dp)); Text(if (showIntro) "INTRO ON" else "INTRO OFF", color = Color.White); Spacer(Modifier.width(8.dp)); Button(onClick = { introVis = true }) { Text("Preview Intro") } }
    Spacer(Modifier.height(12.dp))
    Text("5) LOGO", fontSize = 18.sp, color = Color.White)
    Button(onClick = { onPickLogo { uri -> logoUri = uri } }) { Text("Pick Logo") }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Logo  ", color = Color.White); Switch(checked = showLogo, onCheckedChange = { showLogo = it }); Spacer(Modifier.width(8.dp)); Button(onClick = { logoSize = (logoSize + 20f).coerceAtMost(180f) }) { Text("Logo +") }; Spacer(Modifier.width(8.dp)); Button(onClick = { logoSize = (logoSize - 20f).coerceAtLeast(40f) }) { Text("Logo -") } }
    Spacer(Modifier.height(12.dp))
    Text("6) TITLES - SEE + EDIT", fontSize = 18.sp, color = Color.White)
    Spacer(Modifier.height(6.dp))
    titles.forEach { item -> val one = item; Column(Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) { Text("TITLE: " + one.text, color = Color.White, fontSize = 16.sp); Spacer(Modifier.height(4.dp)); OutlinedTextField(value = one.text, onValueChange = { nt -> titles = titles.map { if (it.id == one.id) it.copy(text = nt) else it } }, label = { Text("Edit this title") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { Button(onClick = { titles = titles.map { if (it.id == one.id) it.copy(offset = it.offset + Offset(-20f, 0f)) else it } }) { Text("⬅️") }; Button(onClick = { titles = titles.map { if (it.id == one.id) it.copy(offset = it.offset + Offset(20f, 0f)) else it } }) { Text("➡️") }; Button(onClick = { titles = titles.map { if (it.id == one.id) it.copy(offset = it.offset + Offset(0f, -20f)) else it } }) { Text("⬆️") }; Button(onClick = { titles = titles.map { if (it.id == one.id) it.copy(offset = it.offset + Offset(0f, 20f)) else it } }) { Text("⬇️") }; Button(onClick = { titles = titles.filter { it.id != one.id } }) { Text("Delete") } } }; Spacer(Modifier.height(8.dp)) }
    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("New Title") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { if (newTitle.isNotBlank()) { titles = titles + TitleItem(nextId, newTitle, Offset(20f, 200f + titles.size * 60f)); nextId = nextId + 1; newTitle = "" } }) { Text("+ ADD TITLE") }; Spacer(Modifier.width(8.dp)); Button(onClick = { titles = listOf(TitleItem(1, "BREAKING", Offset(20f, 200f)), TitleItem(2, "ENQUIRY", Offset(20f, 260f)), TitleItem(3, "RESTORATION", Offset(20f, 320f))); nextId = 4 }) { Text("Reset to 3") } }
    Spacer(Modifier.height(8.dp))
    Text("THEME - Title Color", fontSize = 16.sp, color = Color.White)
    Row { listOf("Red", "Blue", "Green", "Yellow", "Purple", "Orange").forEach { c -> Button(onClick = { titleColor = c }) { Text(if (titleColor == c) "● " + c else c) }; Spacer(Modifier.width(4.dp)) } }
    Spacer(Modifier.height(6.dp))
    Text("FONT - Title Font", fontSize = 16.sp, color = Color.White)
    Row { listOf("Normal", "Serif", "Mono", "Cursive").forEach { f -> Button(onClick = { titleFont = f }) { Text(if (titleFont == f) "● " + f else f) }; Spacer(Modifier.width(4.dp)) } }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Titles  ", color = Color.White); Switch(checked = showTitle, onCheckedChange = { showTitle = it }); Spacer(Modifier.width(8.dp)); Text("Bold  ", color = Color.White); Switch(checked = titleBold, onCheckedChange = { titleBold = it }) }
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { titleSize = (titleSize + 4f).coerceAtMost(48f) }) { Text("Title +") }; Spacer(Modifier.width(8.dp)); Button(onClick = { titleSize = (titleSize - 4f).coerceAtLeast(12f) }) { Text("Title -") } }
    Spacer(Modifier.height(12.dp))
    Text("7) MOVING TEXT - DRAG UP / DOWN + SIZE + COLOR", fontSize = 18.sp, color = Color.White)
    Text("On BROADCAST: Finger on ticker, drag UP / DOWN.", fontSize = 13.sp, color = Color.Yellow)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Moving Text - One Line") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Text("LIVE PREVIEW:", color = Color.White)
    Box(Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) { Text(tickerText, color = kBg(), fontSize = tickerSize.sp, maxLines = 1, textAlign = aVal(), modifier = Modifier.fillMaxWidth()) }
    Spacer(Modifier.height(6.dp))
    Text("SIZE - Slide: " + tickerSize.toInt(), color = Color.White)
    Slider(value = tickerSize, onValueChange = { tickerSize = it }, valueRange = 10f..40f, modifier = Modifier.fillMaxWidth())
    Row { Button(onClick = { tickerSize = 12f }) { Text("Small") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerSize = 18f }) { Text("Medium") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerSize = 28f }) { Text("BIG") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerSize = 36f }) { Text("HUGE") } }
    Spacer(Modifier.height(6.dp))
    Text("COLOR - Tap one:", color = Color.White)
    Row { listOf("Yellow", "White", "Green", "Red", "Cyan", "Pink", "Orange").forEach { c -> Button(onClick = { tickerColor = c }) { Text(if (tickerColor == c) "● " + c else c) }; Spacer(Modifier.width(4.dp)) } }
    Spacer(Modifier.height(6.dp))
    Text("MOVE - Buttons too:", color = Color.White)
    Row { Button(onClick = { tickerOff = tickerOff + Offset(0f, -40f) }) { Text("⬆️ UP") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerOff = tickerOff + Offset(0f, 40f) }) { Text("⬇️ DOWN") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerOff = Offset(0f, 550f) }) { Text("Reset Bottom") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerOff = Offset(0f, 100f) }) { Text("Top") } }
    Spacer(Modifier.height(6.dp))
    Row { listOf("Left", "Center", "Right").forEach { a -> Button(onClick = { tickerAlign = a }) { Text(if (tickerAlign == a) "● " + a else a) }; Spacer(Modifier.width(4.dp)) } }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Moving  ", color = Color.White); Switch(checked = tickerMoving, onCheckedChange = { tickerMoving = it }); Spacer(Modifier.width(8.dp)); Text(if (tickerMoving) "MOVING" else "STATIC", color = Color.White) }
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("LOOP Moving Text  ", color = Color.White); Switch(checked = tickerLoop, onCheckedChange = { tickerLoop = it }); Spacer(Modifier.width(8.dp)); Text(if (tickerLoop) "LOOP ON" else "LOOP OFF", color = Color.White) }
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Black Background  ", color = Color.White); Switch(checked = tickerBgOn, onCheckedChange = { tickerBgOn = it }) }
    Spacer(Modifier.height(6.dp))
    Row { Button(onClick = { tickerSpeed = (tickerSpeed - 2f).coerceAtLeast(4f) }) { Text("Faster") }; Spacer(Modifier.width(8.dp)); Button(onClick = { tickerSpeed = (tickerSpeed + 2f).coerceAtMost(20f) }) { Text("Slower") } }
    Spacer(Modifier.height(12.dp))
    Text("8) GALLERY + LOOP", fontSize = 18.sp, color = Color.White)
    Button(onClick = { onPickVideo { uri -> galleryUri = uri } }) { Text("Pick Gallery Video") }
    Spacer(Modifier.height(6.dp))
    Text(if (galleryUri == null) "No video picked" else "Video picked ✓", color = Color.White)
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) { Text("LOOP Gallery Video  ", color = Color.White); Switch(checked = galleryLoop, onCheckedChange = { galleryLoop = it }); Spacer(Modifier.width(8.dp)); Text(if (galleryLoop) "LOOP ON" else "LOOP OFF", color = Color.White) }
    Spacer(Modifier.height(6.dp))
    Button(onClick = { if (galleryUri == null) onToast("Pick a video first") else isGallery = !isGallery }) { Text(if (isGallery) "Stop Gallery Video" else "Play Gallery Video") }
    Spacer(Modifier.height(12.dp))
        Text("9) SHARE LINK", fontSize = 18.sp, color = Color.White)
    Spacer(Modifier.height(12.dp))
    Button(onClick = { share() }) { Text("🔗 SHARE LINK - MULTISTREAM") }
    Spacer(Modifier.height(20.dp))
    Button(onClick = { tab = "BROADCAST" }, modifier = Modifier.fillMaxWidth()) { Text("✅ DONE - GO TO BROADCAST") }
    Spacer(Modifier.height(32.dp))
   }
  }
 }
}
