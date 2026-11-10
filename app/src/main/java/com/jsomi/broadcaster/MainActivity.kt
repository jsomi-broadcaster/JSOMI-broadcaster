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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val cameraOk = results[Manifest.permission.CAMERA] == true
        val micOk = results[Manifest.permission.RECORD_AUDIO] == true
        hasPermissions = cameraOk && micOk
        if (!hasPermissions) {
            Toast.makeText(this, "Camera + Mic needed to go LIVE.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val cameraOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (cameraOk && micOk) {
            hasPermissions = true
        } else {
            hasPermissions = false
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
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
                    try { rtmpCamera1?.setVideoOrientation(if (isLandscape) 90 else 0) } catch (e: Exception) { }
                },
                onPickLogo = { callback -> onLogoPicked = callback; logoPicker.launch("image/*") },
                onPickVideo = { callback -> onVideoPicked = callback; videoPicker.launch("video/*") },
                onToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() },
                onSetCamera = { view -> rtmpCamera1 = makeRtmpCamera(view) },
                onStartPreview = {
                    try {
                        if (rtmpCamera1?.isOnPreview != true) {
                            rtmpCamera1?.prepareVideo()
                            rtmpCamera1?.prepareAudio()
                            rtmpCamera1?.startPreview()
                        }
                    } catch (e: Exception) { }
                },
                onStartStream = { fullUrl ->
                    val okAudio = rtmpCamera1?.prepareAudio() ?: false
                    val okVideo = rtmpCamera1?.prepareVideo() ?: false
                    if (okAudio && okVideo) {
                        try { rtmpCamera1?.startPreview() } catch (e: Exception) { }
                        rtmpCamera1?.startStream(fullUrl)
                        true
                    } else { false }
                },
                onSwitchUrl = { fullUrl ->
                    try { rtmpCamera1?.stopStream(); rtmpCamera1?.startStream(fullUrl); true } catch (e: Exception) { false }
                },
                onStopStream = { rtmpCamera1?.stopStream(); try { rtmpCamera1?.startPreview() } catch (e: Exception) { } },
                onSwitchCamera = { try { rtmpCamera1?.switchCamera() } catch (e: Exception) { } },
                onMute = { mute -> if (mute) rtmpCamera1?.disableAudio() else rtmpCamera1?.enableAudio() }
            )
        }
    }
    override fun onDestroy() {
        try { rtmpCamera1?.stopStream(); rtmpCamera1?.stopPreview() } catch (e: Exception) { }
        super.onDestroy()
    }
}

@Composable
fun JstvProScreen(
    hasPermissions: Boolean,
    isLandscape: Boolean,
    onRequestPermissions: () -> Unit,
    onToggleRotate: () -> Unit,
    onPickLogo: ((Uri?) -> Unit) -> Unit,
    onPickVideo: ((Uri?) -> Unit) -> Unit,
    onToast: (String) -> Unit,
    onSetCamera: (SurfaceView) -> Unit,
    onStartPreview: () -> Unit,
    onStartStream: (String) -> Boolean,
    onSwitchUrl: (String) -> Boolean,
    onStopStream: () -> Unit,
    onSwitchCamera: () -> Unit,
    onMute: (Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf("BROADCAST") }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live") }
    var appMode by remember { mutableStateOf("Presenter") }
    var camIndex by remember { mutableStateOf(1) }
    var remoteCommand by remember { mutableStateOf("Waiting for Director...") }
    var directorJoined by remember { mutableStateOf(false) }
    var useYoutube by remember { mutableStateOf(true) }
    var useFacebook by remember { mutableStateOf(false) }
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
    var titleText by remember { mutableStateOf("BREAKING") }
    var showTitle by remember { mutableStateOf(true) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 200f)) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isPlayingGallery by remember { mutableStateOf(false) }

    // COMMIT 6 FIX: Start camera preview as soon as permission granted, not only on LIVE
    LaunchedEffect(hasPermissions) {
        if (hasPermissions) { onStartPreview() }
    }

    fun selectedDests(): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        if (useYoutube) { list.add(Triple("YouTube", "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey, youtubeKey)) }
        if (useFacebook) { list.add(Triple("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey, facebookKey)) }
        if (useJsomi) { list.add(Triple("JSOMI", "rtmp://jsomi.tv/live/" + jsomiKey, jsomiKey)) }
        if (useCustom) {
            val full = if (customUrl.isBlank()) "" else customUrl + "/" + customKey
            list.add(Triple("Custom", full, customKey))
        }
        return list
    }

    fun setStatus(name: String, status: String) {
        when (name) {
            "YouTube" -> statusYoutube = status
            "Facebook" -> statusFacebook = status
            "JSOMI" -> statusJsomi = status
            "Custom" -> statusCustom = status
        }
    }

    fun statusColor(s: String): Color {
        return when {
            s.startsWith("LIVE") -> Color.Green
            s.startsWith("FAILED") -> Color.Red
            else -> Color.Black
        }
    }

    fun startLive() {
        if (!hasPermissions) { onToast("Grant Camera + Mic first"); onRequestPermissions() } else {
            val dests = selectedDests()
            if (dests.isEmpty()) { liveStatus = "FAILED - Tick 1 destination"; onToast("Tick at least 1 destination in SETUP") } else {
                val first = dests[0]
                if (first.third.isBlank()) {
                    setStatus(first.first, "FAILED - Enter Key")
                    liveStatus = "FAILED - Enter Key for ${first.first}"
                    onToast("Enter key for ${first.first} in SETUP first")
                    currentTab = "SETUP"
                } else {
                    liveStatus = "CONNECTING to ${first.first}..."
                    val started = onStartStream(first.second)
                    if (started) {
                        currentDestIndex = 0
                        isStreaming = true
                        setStatus(first.first, "LIVE ✅")
                        dests.drop(1).forEach { d -> setStatus(d.first, "READY - Tap Switch") }
                        liveStatus = "LIVE to ${first.first}"
                        onToast("LIVE to ${first.first}")
                    } else {
                        setStatus(first.first, "FAILED - Check Key")
                        liveStatus = "FAILED - Check Key"
                        onToast("FAILED - Check key for ${first.first}")
                    }
                }
            }
        }
    }

    fun stopLive() {
        onStopStream()
        isStreaming = false
        currentDestIndex = -1
        liveStatus = "STOPPED"
        statusYoutube = "OFF"; statusFacebook = "OFF"; statusJsomi = "OFF"; statusCustom = "OFF"
        onToast("Stopped All")
    }

    fun directorAction(action: String) {
        remoteCommand = action
        directorJoined = true
        when (action) {
            "CAM 1" -> camIndex = 1
            "CAM 2" -> camIndex = 2
            "CAM 3" -> camIndex = 3
            "MUTE" -> { isMuted = true; onMute(true) }
            "UNMUTE" -> { isMuted = false; onMute(false) }
            "SHOW LOGO" -> showLogo = true
            "HIDE LOGO" -> showLogo = false
            "SHOW TITLE" -> showTitle = true
            "HIDE TITLE" -> showTitle = false
            "PLAY GALLERY" -> isPlayingGallery = true
            "STOP GALLERY" -> isPlayingGallery = false
        }
        onToast("Director: $action - Room $roomCode")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP BAR - ALWAYS VISIBLE
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { currentTab = "BROADCAST" }, content = { Text(text = if (currentTab == "BROADCAST") "● 📺 BROADCAST" else "📺 BROADCAST") })
            Button(onClick = { currentTab = "SETUP" }, content = { Text(text = if (currentTab == "SETUP") "● ⚙️ SETUP" else "⚙️ SETUP") })
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = liveStatus, color = if (liveStatus.startsWith("LIVE")) Color.Green else if (liveStatus.startsWith("FAILED")) Color.Red else Color.White, fontSize = 14.sp)
            Text(text = if (isLandscape) "LANDSCAPE" else "PORTRAIT", color = Color.Yellow, fontSize = 14.sp)
            Text(text = if (hasPermissions) "CAM ✅" else "CAM ❌", color = if (hasPermissions) Color.Green else Color.Red, fontSize = 14.sp)
        }

        if (!hasPermissions) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(16.dp)) {
                Column {
                    Text(text = "⚠️ Camera + Mic Needed", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onRequestPermissions() }, content = { Text(text = "GRANT CAMERA + MIC") })
                }
            }
        }

        if (currentTab == "BROADCAST") {
            // ============ BROADCAST SCREEN - FULL CAMERA ============
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
                if (hasPermissions) {
                    AndroidView(factory = { ctx -> SurfaceView(ctx).also { view -> onSetCamera(view); } }, modifier = Modifier.fillMaxSize())
                } else {
                    Text(text = "Camera blocked - Grant permission above", color = Color.White, modifier = Modifier.align(androidx.compose.ui.Alignment.Center).padding(16.dp))
                }
                // LIVE badge
                if (isStreaming) {
                    Box(modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart).padding(12.dp).background(Color.Red).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(text = "● LIVE", color = Color.White, fontSize = 18.sp)
                    }
                }
                Text(text = "CAM $camIndex - $appMode - $roomCode", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(12.dp).background(Color.Black).padding(4.dp))
                if (showLogo) {
                    Box(modifier = Modifier.offset(logoOffset.x.dp, logoOffset.y.dp).size(logoSize.dp).background(Color.White).pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); logoOffset += dragAmount } }) {
                        Text(text = if (logoUri == null) "LOGO" else "LOGO ✓", fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                    }
                }
                if (showTitle) {
                    Box(modifier = Modifier.offset(titleOffset.x.dp, titleOffset.y.dp).background(when (titleColor) { "Red" -> Color.Red; "Blue" -> Color.Blue; "Green" -> Color.Green; else -> Color.Black }).padding(8.dp).pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); titleOffset += dragAmount } }) {
                        Text(text = titleText, color = Color.White, fontSize = titleSize.sp)
                    }
                }
                Text(text = tickerText, color = Color.Yellow, fontSize = 14.sp, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(bottom = 8.dp).background(Color.Black).padding(4.dp))
                if (isPlayingGallery) {
                    Text(text = "▶ Gallery Video Playing - Loop: ${if (loopOn) "ON" else "OFF"}", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(12.dp).background(Color.Blue).padding(4.dp))
                }
            }
            // BROADCAST CONTROLS - ALWAYS VISIBLE, NO SCROLL NEEDED
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { if (isStreaming) stopLive() else startLive() }, content = { Text(text = if (isStreaming) "⏹ STOP LIVE" else "🔴 GO LIVE") })
                    Button(onClick = { onSwitchCamera(); onToast("Camera Switched") }, content = { Text(text = "Switch Cam") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onToggleRotate() }, content = { Text(text = if (isLandscape) "↻ Portrait" else "↻ Landscape") })
                    Button(onClick = { isMuted = !isMuted; onMute(isMuted); onToast(if (isMuted) "Muted" else "Unmuted") }, content = { Text(text = if (isMuted) "Unmute" else "Mute") })
                    Button(onClick = { directorAction(if (showLogo) "HIDE LOGO" else "SHOW LOGO") }, content = { Text(text = if (showLogo) "Hide Logo" else "Show Logo") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = "YouTube: $statusYoutube", color = statusColor(statusYoutube), fontSize = 12.sp)
                    Text(text = "FB: $statusFacebook", color = statusColor(statusFacebook), fontSize = 12.sp)
                    Text(text = "JSOMI: $statusJsomi", color = statusColor(statusJsomi), fontSize = 12.sp)
                    Text(text = "Custom: $statusCustom", color = statusColor(statusCustom), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        val dests = selectedDests()
                        if (!isStreaming || dests.size < 2) { onToast("Tick 2+ destinations in SETUP to switch") } else {
                            val nextIndex = (currentDestIndex + 1) % dests.size
                            val next = dests[nextIndex]
                            if (next.third.isBlank()) { setStatus(next.first, "FAILED - Enter Key"); onToast("Enter key for ${next.first}") } else {
                                val ok = onSwitchUrl(next.second)
                                if (ok) {
                                    dests.forEach { d -> if (d.first != next.first) { setStatus(d.first, "READY") } }
                                    setStatus(next.first, "LIVE ✅"); currentDestIndex = nextIndex; liveStatus = "LIVE to ${next.first}"; onToast("Switched to ${next.first}")
                                } else { setStatus(next.first, "FAILED - Check Key"); liveStatus = "FAILED - Check Key" }
                            }
                        }
                    },
                    enabled = isStreaming, modifier = Modifier.fillMaxWidth(), content = { Text(text = "SWITCH DESTINATION LIVE") }
                )
            }
        } else {
            // ============ SETUP SCREEN - SCROLL ============
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = "SETUP - Do this BEFORE GO LIVE", fontSize = 20.sp)
                Text(text = "Director says: $remoteCommand", fontSize = 14.sp, color = Color.Blue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "REMOTE - Presenter / Director", fontSize = 18.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { appMode = "Presenter" }, content = { Text(text = if (appMode == "Presenter") "● PRESENTER" else "Presenter") })
                    Button(onClick = { appMode = "Director" }, content = { Text(text = if (appMode == "Director") "● DIRECTOR" else "Director") })
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = roomCode, onValueChange = { roomCode = it }, label = { Text("Room Code") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                if (appMode == "Director") {
                    Text(text = "DIRECTOR PANEL", fontSize = 18.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { directorAction("CAM 1") }, content = { Text(text = "CAM 1") })
                        Button(onClick = { directorAction("CAM 2") }, content = { Text(text = "CAM 2") })
                        Button(onClick = { directorAction("CAM 3") }, content = { Text(text = "CAM 3") })
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { directorAction(if (showTitle) "HIDE TITLE" else "SHOW TITLE") }, content = { Text(text = if (showTitle) "Hide Title" else "Show Title") })
                        Button(onClick = { directorAction(if (isPlayingGallery) "STOP GALLERY" else "PLAY GALLERY") }, content = { Text(text = if (isPlayingGallery) "Stop Gallery" else "Play Gallery") })
                        Button(onClick = { onToggleRotate() }, content = { Text(text = "Rotate") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text = "MULTISTREAM - Tick + Enter Key", fontSize = 18.sp)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = useYoutube, onCheckedChange = { useYoutube = it })
                    Text(text = "YouTube  "); Text(text = statusYoutube, color = statusColor(statusYoutube))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = useFacebook, onCheckedChange = { useFacebook = it })
                    Text(text = "Facebook  "); Text(text = statusFacebook, color = statusColor(statusFacebook))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = useJsomi, onCheckedChange = { useJsomi = it })
                    Text(text = "JSOMI TV  "); Text(text = statusJsomi, color = statusColor(statusJsomi))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = useCustom, onCheckedChange = { useCustom = it })
                    Text(text = "Custom RTMP  "); Text(text = statusCustom, color = statusColor(statusCustom))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "STREAM KEYS - Paste here", fontSize = 18.sp)
                OutlinedTextField(value = youtubeKey, onValueChange = { youtubeKey = it }, label = { Text("YouTube Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = facebookKey, onValueChange = { facebookKey = it }, label = { Text("Facebook Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = jsomiKey, onValueChange = { jsomiKey = it }, label = { Text("JSOMI TV Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = customUrl, onValueChange = { customUrl = it }, label = { Text("Custom RTMP URL e.g. rtmp://server/live") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = customKey, onValueChange = { customKey = it }, label = { Text("Custom Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Ticker Text - Shows on Broadcast") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = "Quality: $quality  ")
                    Button(onClick = { quality = if (quality == "720p") "1080p" else "720p" }, content = { Text(text = "Switch to ${if (quality == "720p") "1080p" else "720p"}") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "LOGO - Shows on Broadcast", fontSize = 18.sp)
                Button(onClick = { onPickLogo { uri -> logoUri = uri } }, content = { Text(text = "Pick Logo from Gallery") })
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = "Show Logo  ")
                    Switch(checked = showLogo, onCheckedChange = { showLogo = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { logoSize = (logoSize + 20f).coerceAtMost(180f) }, content = { Text(text = "Logo +") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { logoSize = (logoSize - 20f).coerceAtLeast(40f) }, content = { Text(text = "Logo -") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "TITLES - Shows on Broadcast", fontSize = 18.sp)
                OutlinedTextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Custom Title Text") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { titleText = "ENQUIRY" }, content = { Text(text = "ENQUIRY") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { titleText = "RESTORATION" }, content = { Text(text = "RESTORATION") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { titleText = "BREAKING" }, content = { Text(text = "BREAKING") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = "Show Title  ")
                    Switch(checked = showTitle, onCheckedChange = { showTitle = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { titleColor = if (titleColor == "Red") "Blue" else if (titleColor == "Blue") "Green" else "Red" }, content = { Text(text = "Color: $titleColor") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { titleSize = (titleSize + 4f).coerceAtMost(48f) }, content = { Text(text = "Title +") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { titleSize = (titleSize - 4f).coerceAtLeast(12f) }, content = { Text(text = "Title -") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "GALLERY + LOOP", fontSize = 18.sp)
                Button(onClick = { onPickVideo { uri -> galleryVideoUri = uri } }, content = { Text(text = "Pick Gallery Video") })
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (galleryVideoUri == null) "No video picked" else "Video picked ✓")
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = "Loop ON/OFF  ")
                    Switch(checked = loopOn, onCheckedChange = { loopOn = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = if (loopOn) "LOOP: ON" else "LOOP: OFF")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { if (galleryVideoUri == null) onToast("Pick a video first") else { isPlayingGallery = !isPlayingGallery; onToast(if (isPlayingGallery) "Playing Gallery Video" else "Gallery Paused") } }, content = { Text(text = if (isPlayingGallery) "Stop Gallery Video" else "Play Gallery Video") })
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { currentTab = "BROADCAST"; onToast("Back to Broadcast - Tap GO LIVE") }, modifier = Modifier.fillMaxWidth(), content = { Text(text = "✅ DONE - GO TO BROADCAST") })
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
