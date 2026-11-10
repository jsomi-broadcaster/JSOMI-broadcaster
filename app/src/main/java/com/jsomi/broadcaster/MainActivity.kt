package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.library.rtmp.RtmpCamera1
import java.lang.reflect.Proxy

class MainActivity : ComponentActivity() {
    private var rtmpCamera1: RtmpCamera1? = null
    private var hasPermissions by mutableStateOf(false)
    private var isLandscape by mutableStateOf(false)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val cameraOk = results[Manifest.permission.CAMERA] == true
        val micOk = results[Manifest.permission.RECORD_AUDIO] == true
        hasPermissions = cameraOk && micOk
        if (!hasPermissions) { Toast.makeText(this, "Camera + Mic needed to go LIVE.", Toast.LENGTH_LONG).show() }
    }
    private fun checkAndRequestPermissions() {
        val cameraOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (cameraOk && micOk) { hasPermissions = true } else { hasPermissions = false; permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }
    }
    private fun makeRtmpCamera(view: SurfaceView): RtmpCamera1 {
        val ctor = RtmpCamera1::class.java.constructors.first { it.parameterCount == 2 && it.parameterTypes[0] == SurfaceView::class.java }
        val checkerInterface = ctor.parameterTypes[1]
        val dummyChecker = Proxy.newProxyInstance(checkerInterface.classLoader, arrayOf(checkerInterface)) { _, _, _ -> null }
        return ctor.newInstance(view, dummyChecker) as RtmpCamera1
    }
    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onLogoPicked?.invoke(uri) }
    private var onVideoPicked: ((Uri?) -> Unit)? = null
    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onVideoPicked?.invoke(uri) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            JstvProScreen(
                hasPermissions = hasPermissions,
                isLandscape = isLandscape,
                onRequestPermissions = { checkAndRequestPermissions() },
                onToggleRotate = {
                    isLandscape = !isLandscape
                    requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                },
                onPickLogo = { callback -> onLogoPicked = callback; logoPicker.launch("image/*") },
                onPickVideo = { callback -> onVideoPicked = callback; videoPicker.launch("video/*") },
                onToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() },
                onSetCamera = { view -> rtmpCamera1 = makeRtmpCamera(view) },
                onStartPreview = { try { if (rtmpCamera1?.isOnPreview != true) { rtmpCamera1?.prepareVideo(); rtmpCamera1?.prepareAudio(); rtmpCamera1?.startPreview() } } catch (e: Exception) { } },
                onStartStream = { fullUrl ->
                    val okAudio = rtmpCamera1?.prepareAudio() ?: false
                    val okVideo = rtmpCamera1?.prepareVideo() ?: false
                    if (okAudio && okVideo) { try { rtmpCamera1?.startPreview() } catch (e: Exception) { }; rtmpCamera1?.startStream(fullUrl); true } else { false }
                },
                onSwitchUrl = { fullUrl -> try { rtmpCamera1?.stopStream(); rtmpCamera1?.startStream(fullUrl); true } catch (e: Exception) { false } },
                onStopStream = { try { rtmpCamera1?.stopStream() } catch (e: Exception) { }; try { rtmpCamera1?.startPreview() } catch (e: Exception) { } },
                onSwitchCamera = { try { rtmpCamera1?.switchCamera() } catch (e: Exception) { } },
                onMute = { mute -> if (mute) rtmpCamera1?.disableAudio() else rtmpCamera1?.enableAudio() }
            )
        }
    }
    override fun onDestroy() { try { rtmpCamera1?.stopStream(); rtmpCamera1?.stopPreview() } catch (e: Exception) { }; super.onDestroy() }
}

data class TitleItem(val id: Int, val text: String, val offset: Offset)

@Composable
fun JstvProScreen(
    hasPermissions: Boolean, isLandscape: Boolean, onRequestPermissions: () -> Unit, onToggleRotate: () -> Unit,
    onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit,
    onSetCamera: (SurfaceView) -> Unit, onStartPreview: () -> Unit, onStartStream: (String) -> Boolean,
    onSwitchUrl: (String) -> Boolean, onStopStream: () -> Unit, onSwitchCamera: () -> Unit, onMute: (Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf("BROADCAST") }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live - Breaking News - ") }
    var appMode by remember { mutableStateOf("Presenter") }
    var camIndex by remember { mutableStateOf(1) }
    var remoteCommand by remember { mutableStateOf("Waiting for Director...") }
    var directorJoined by remember { mutableStateOf(false) }
    var liveTarget by remember { mutableStateOf("Facebook") }
    var useYoutube by remember { mutableStateOf(true) }
    var useFacebook by remember { mutableStateOf(true) }
    var useJsomi by remember { mutableStateOf(false) }
    var useCustom by remember { mutableStateOf(false) }
    var statusYoutube by remember { mutableStateOf("OFF") }
    var statusFacebook by remember { mutableStateOf("OFF") }
    var statusJsomi by remember { mutableStateOf("OFF") }
    var statusCustom by remember { mutableStateOf("OFF") }
    var liveStatus by remember { mutableStateOf("READY") }
    var isStreaming by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var currentDestIndex by remember { mutableStateOf(-1) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var logoSize by remember { mutableStateOf(80f) }
    var titlesList by remember { mutableStateOf(listOf(TitleItem(1, "BREAKING", Offset(20f, 200f)), TitleItem(2, "ENQUIRY", Offset(20f, 260f)), TitleItem(3, "RESTORATION", Offset(20f, 320f)))) }
    var nextTitleId by remember { mutableStateOf(4) }
    var newTitleText by remember { mutableStateOf("") }
    var showTitle by remember { mutableStateOf(true) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }
    var tickerMoving by remember { mutableStateOf(true) }
    var tickerLoop by remember { mutableStateOf(true) }
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isPlayingGallery by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermissions) { if (hasPermissions) { onStartPreview() } }

    val infinite = rememberInfiniteTransition(label = "ticker")
    val tickerX by infinite.animateFloat(initialValue = 400f, targetValue = -800f, animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "tickerX")

    fun destFor(name: String): Triple<String, String, String> {
        return when (name) {
            "YouTube" -> Triple("YouTube", "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey, youtubeKey)
            "Facebook" -> Triple("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey, facebookKey)
            "JSOMI" -> Triple("JSOMI", "rtmp://jsomi.tv/live/" + jsomiKey, jsomiKey)
            else -> Triple("Custom", if (customUrl.isBlank()) "" else customUrl + "/" + customKey, customKey)
        }
    }
    fun selectedDests(): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        if (useYoutube) { list.add(destFor("YouTube")) }
        if (useFacebook) { list.add(destFor("Facebook")) }
        if (useJsomi) { list.add(destFor("JSOMI")) }
        if (useCustom) { list.add(destFor("Custom")) }
        return list
    }
    fun setStatus(name: String, status: String) {
        if (name == "YouTube") { statusYoutube = status }
        if (name == "Facebook") { statusFacebook = status }
        if (name == "JSOMI") { statusJsomi = status }
        if (name == "Custom") { statusCustom = status }
    }
    fun statusColor(s: String): Color {
        if (s.startsWith("LIVE")) { return Color.Green }
        if (s.startsWith("FAILED")) { return Color.Red }
        return Color.Black
    }
    fun startLive() {
        if (!hasPermissions) { onToast("Grant Camera + Mic first"); onRequestPermissions() } else {
            val dest = destFor(liveTarget)
            if (dest.third.isBlank()) {
                setStatus(dest.first, "FAILED - Enter Key"); liveStatus = "FAILED - Enter Key for " + dest.first; onToast("Enter " + dest.first + " key in SETUP first"); currentTab = "SETUP"
            } else {
                liveStatus = "CONNECTING to " + dest.first + "..."
                val started = onStartStream(dest.second)
                if (started) {
                    val dests = selectedDests()
                    currentDestIndex = dests.indexOfFirst { it.first == dest.first }.coerceAtLeast(0)
                    isStreaming = true; setStatus(dest.first, "LIVE ✅"); liveStatus = "LIVE to " + dest.first; onToast("LIVE to " + dest.first + " - Check " + dest.first + " app")
                } else { setStatus(dest.first, "FAILED - Check Key"); liveStatus = "FAILED - Check Key for " + dest.first; onToast("FAILED - Check key for " + dest.first) }
            }
        }
    }
    fun endLive() {
        val endedTo = liveTarget
        onStopStream(); isStreaming = false; currentDestIndex = -1; liveStatus = "ENDED - Saved to " + endedTo + " ✅"; statusYoutube = "OFF"; statusFacebook = "OFF"; statusJsomi = "OFF"; statusCustom = "OFF"
        onToast("ENDED - Saved to " + endedTo)
    }
    // FIXED LINE 200: simple if/else, explicit String type, clear body
    fun directorAction(action: String) {
        remoteCommand = action
        directorJoined = true
        if (action == "CAM 1") { camIndex = 1 }
        if (action == "CAM 2") { camIndex = 2 }
        if (action == "CAM 3") { camIndex = 3 }
        if (action == "MUTE") { isMuted = true; onMute(true) }
        if (action == "UNMUTE") { isMuted = false; onMute(false) }
        if (action == "SHOW LOGO") { showLogo = true }
        if (action == "HIDE LOGO") { showLogo = false }
        if (action == "SHOW TITLE") { showTitle = true }
        if (action == "HIDE TITLE") { showTitle = false }
        if (action == "PLAY GALLERY") { isPlayingGallery = true }
        if (action == "STOP GALLERY") { isPlayingGallery = false }
        onToast("Director: " + action + " - Room " + roomCode)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { currentTab = "BROADCAST" }, content = { Text(text = if (currentTab == "BROADCAST") "● 📺 BROADCAST" else "📺 BROADCAST") })
            Button(onClick = { currentTab = "SETUP" }, content = { Text(text = if (currentTab == "SETUP") "● ⚙️ SETUP" else "⚙️ SETUP") })
        }
        if (!hasPermissions) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(12.dp)) {
                Column { Text(text = "⚠️ Camera + Mic Needed", color = Color.White, fontSize = 16.sp); Button(onClick = { onRequestPermissions() }, content = { Text(text = "GRANT CAMERA + MIC") }) }
            }
        }
        if (currentTab == "BROADCAST") {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (hasPermissions) { AndroidView(factory = { ctx -> SurfaceView(ctx).also { view -> onSetCamera(view) } }, modifier = Modifier.fillMaxSize()) } else { Text(text = "Camera blocked - Grant permission above", color = Color.White, modifier = Modifier.align(androidx.compose.ui.Alignment.Center).padding(16.dp)) }
                if (isStreaming) { Box(modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart).padding(12.dp).background(Color.Red).padding(horizontal = 10.dp, vertical = 4.dp)) { Text(text = "● LIVE to $liveTarget", color = Color.White, fontSize = 16.sp) } }
                if (showLogo) {
                    Box(modifier = Modifier.offset(logoOffset.x.dp, logoOffset.y.dp).size(logoSize.dp).background(Color.White).pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); logoOffset += dragAmount } }) { Text(text = if (logoUri == null) "LOGO" else "LOGO ✓", fontSize = 12.sp, modifier = Modifier.padding(4.dp)) }
                }
                if (showTitle) {
                    titlesList.forEach { t ->
                        val myTitle = t
                        Box(modifier = Modifier.offset(myTitle.offset.x.dp, myTitle.offset.y.dp).background(when (titleColor) { "Red" -> Color.Red; "Blue" -> Color.Blue; "Green" -> Color.Green; else -> Color.Black }).padding(horizontal = 8.dp, vertical = 4.dp).pointerInput(myTitle.id) { detectDragGestures { change, dragAmount -> change.consume(); titlesList = titlesList.map { if (it.id == myTitle.id) it.copy(offset = it.offset + dragAmount) else it } } }) { Text(text = myTitle.text, color = Color.White, fontSize = titleSize.sp) }
                    }
                }
                Box(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).fillMaxWidth().padding(bottom = 105.dp).background(Color.Black)) {
                    if (tickerMoving) { Text(text = tickerText + "  •  " + tickerText, color = Color.Yellow, fontSize = 15.sp, modifier = Modifier.offset(if (tickerLoop) tickerX.dp else 0.dp).padding(6.dp)) } else { Text(text = tickerText, color = Color.Yellow, fontSize = 15.sp, modifier = Modifier.padding(6.dp)) }
                }
                if (isPlayingGallery) { Text(text = "▶ Gallery Video Playing - Loop: " + if (loopOn) "ON" else "OFF", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(12.dp).background(Color.Blue).padding(4.dp)) }
                Column(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(8.dp)) {
                    Text(text = if (isStreaming) "LIVE to $liveTarget" else if (liveStatus.startsWith("ENDED")) liveStatus else "READY - Target: $liveTarget | $roomCode", color = if (isStreaming) Color.Green else Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { if (isStreaming) endLive() else startLive() }, content = { Text(text = if (isStreaming) "⏹ END LIVE" else "🔴 GO LIVE") })
                        Button(onClick = { onSwitchCamera() }, content = { Text(text = "Switch Cam") })
                        Button(onClick = { onToggleRotate() }, content = { Text(text = if (isLandscape) "↻ Portrait" else "↻ Landscape") })
                        Button(onClick = { isMuted = !isMuted; onMute(isMuted) }, content = { Text(text = if (isMuted) "Unmute" else "Mute") })
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = "⚙️ SETUP - All settings here", fontSize = 20.sp)
                Text(text = "Live Status: $liveStatus", fontSize = 14.sp, color = if (liveStatus.startsWith("LIVE")) Color.Green else if (liveStatus.startsWith("FAILED")) Color.Red else Color.Black)
                Text(text = "Director says: $remoteCommand", fontSize = 14.sp, color = Color.Blue)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "1) WHERE TO STREAM? Pick ONE LIVE TARGET", fontSize = 18.sp)
                Text(text = "Phone streams to 1 place at a time. Pick Facebook, GO LIVE, then END, pick YouTube, GO LIVE again. Or use SWITCH below while live.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { liveTarget = "YouTube" }, content = { Text(text = if (liveTarget == "YouTube") "● YouTube" else "YouTube") })
                    Button(onClick = { liveTarget = "Facebook" }, content = { Text(text = if (liveTarget == "Facebook") "● Facebook" else "Facebook") })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { liveTarget = "JSOMI" }, content = { Text(text = if (liveTarget == "JSOMI") "● JSOMI" else "JSOMI") })
                    Button(onClick = { liveTarget = "Custom" }, content = { Text(text = if (liveTarget == "Custom") "● Custom" else "Custom") })
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Live Target now: $liveTarget", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val dests = selectedDests()
                    if (!isStreaming || dests.size < 2) { onToast("GO LIVE first, tick 2+ destinations to switch") } else {
                        val nextIndex = (currentDestIndex + 1) % dests.size; val next = dests[nextIndex]
                        if (next.third.isBlank()) { setStatus(next.first, "FAILED - Enter Key"); onToast("Enter key for " + next.first) } else {
                            val ok = onSwitchUrl(next.second)
                            if (ok) { dests.forEach { d -> if (d.first != next.first) { setStatus(d.first, "READY") } }; setStatus(next.first, "LIVE ✅"); currentDestIndex = nextIndex; liveTarget = next.first; liveStatus = "LIVE to " + next.first; onToast("Switched to " + next.first) } else { setStatus(next.first, "FAILED - Check Key") }
                        }
                    }
                }, enabled = isStreaming, modifier = Modifier.fillMaxWidth(), content = { Text(text = "SWITCH LIVE TARGET (while live)") })
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "2) TICK DESTINATIONS + KEYS", fontSize = 18.sp)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(checked = useYoutube, onCheckedChange = { useYoutube = it }); Text(text = "YouTube  "); Text(text = statusYoutube, color = statusColor(statusYoutube)) }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(checked = useFacebook, onCheckedChange = { useFacebook = it }); Text(text = "Facebook  "); Text(text = statusFacebook, color = statusColor(statusFacebook)) }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(checked = useJsomi, onCheckedChange = { useJsomi = it }); Text(text = "JSOMI TV  "); Text(text = statusJsomi, color = statusColor(statusJsomi)) }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(checked = useCustom, onCheckedChange = { useCustom = it }); Text(text = "Custom RTMP  "); Text(text = statusCustom, color = statusColor(statusCustom)) }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = youtubeKey, onValueChange = { youtubeKey = it }, label = { Text("YouTube Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = facebookKey, onValueChange = { facebookKey = it }, label = { Text("Facebook Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Facebook tip: In Facebook app > + > Live > Get Stream Key, copy ONLY the key, not the URL. Key changes every live.", fontSize = 12.sp, color = Color.Blue)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = jsomiKey, onValueChange = { jsomiKey = it }, label = { Text("JSOMI TV Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = customUrl, onValueChange = { customUrl = it }, label = { Text("Custom RTMP URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = customKey, onValueChange = { customKey = it }, label = { Text("Custom Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "3) REMOTE - Presenter / Director", fontSize = 18.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { appMode = "Presenter" }, content = { Text(text = if (appMode == "Presenter") "● PRESENTER" else "Presenter") })
                    Button(onClick = { appMode = "Director" }, content = { Text(text = if (appMode == "Director") "● DIRECTOR" else "Director") })
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = roomCode, onValueChange = { roomCode = it }, label = { Text("Room Code") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                if (appMode == "Director") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { directorAction("CAM 1") }, content = { Text(text = "CAM 1") }); Button(onClick = { directorAction("CAM 2") }, content = { Text(text = "CAM 2") }); Button(onClick = { directorAction("CAM 3") }, content = { Text(text = "CAM 3") })
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { directorAction(if (showTitle) "HIDE TITLE" else "SHOW TITLE") }, content = { Text(text = if (showTitle) "Hide Title" else "Show Title") }); Button(onClick = { directorAction(if (isPlayingGallery) "STOP GALLERY" else "PLAY GALLERY") }, content = { Text(text = if (isPlayingGallery) "Stop Gallery" else "Play Gallery") }); Button(onClick = {
