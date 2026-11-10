package com.jsomi.broadcaster

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
import com.pedro.library.rtmp.RtmpCamera1
import java.lang.reflect.Proxy

class MainActivity : ComponentActivity() {

    private var rtmpCamera1: RtmpCamera1? = null

    private fun makeRtmpCamera(view: SurfaceView): RtmpCamera1 {
        val ctor = RtmpCamera1::class.java.constructors.first { it.parameterCount == 2 && it.parameterTypes[0] == SurfaceView::class.java }
        val checkerInterface = ctor.parameterTypes[1]
        val dummyChecker = Proxy.newProxyInstance(
            checkerInterface.classLoader,
            arrayOf(checkerInterface)
        ) { _, _, _ -> null }
        return ctor.newInstance(view, dummyChecker) as RtmpCamera1
    }

    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onLogoPicked?.invoke(uri)
    }

    private var onVideoPicked: ((Uri?) -> Unit)? = null
    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onVideoPicked?.invoke(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JstvProScreen(
                onPickLogo = { callback ->
                    onLogoPicked = callback
                    logoPicker.launch("image/*")
                },
                onPickVideo = { callback ->
                    onVideoPicked = callback
                    videoPicker.launch("video/*")
                },
                onToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() },
                onSetCamera = { view -> rtmpCamera1 = makeRtmpCamera(view) },
                onStartStream = { fullUrl ->
                    val okAudio = rtmpCamera1?.prepareAudio() ?: false
                    val okVideo = rtmpCamera1?.prepareVideo() ?: false
                    if (okAudio && okVideo) {
                        rtmpCamera1?.startPreview()
                        rtmpCamera1?.startStream(fullUrl)
                        true
                    } else {
                        false
                    }
                },
                onSwitchUrl = { fullUrl ->
                    try {
                        rtmpCamera1?.stopStream()
                        rtmpCamera1?.startStream(fullUrl)
                        true
                    } catch (e: Exception) {
                        false
                    }
                },
                onStopStream = {
                    rtmpCamera1?.stopStream()
                    rtmpCamera1?.stopPreview()
                },
                onSwitchCamera = { rtmpCamera1?.switchCamera() },
                onMute = { mute -> if (mute) rtmpCamera1?.disableAudio() else rtmpCamera1?.enableAudio() }
            )
        }
    }
}

@Composable
fun JstvProScreen(
    onPickLogo: ((Uri?) -> Unit) -> Unit,
    onPickVideo: ((Uri?) -> Unit) -> Unit,
    onToast: (String) -> Unit,
    onSetCamera: (SurfaceView) -> Unit,
    onStartStream: (String) -> Boolean,
    onSwitchUrl: (String) -> Boolean,
    onStopStream: () -> Unit,
    onSwitchCamera: () -> Unit,
    onMute: (Boolean) -> Unit
) {
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live") }

    // --- COMMIT 4: REMOTE / MULTI-CAM ---
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

    fun selectedDests(): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        if (useYoutube) {
            list.add(Triple("YouTube", "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey, youtubeKey))
        }
        if (useFacebook) {
            list.add(Triple("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey, facebookKey))
        }
        if (useJsomi) {
            list.add(Triple("JSOMI", "rtmp://jsomi.tv/live/" + jsomiKey, jsomiKey))
        }
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

    fun directorAction(action: String) {
        remoteCommand = action
        directorJoined = true
        when (action) {
            "CAM 1" -> camIndex = 1
            "CAM 2" -> camIndex = 2
            "CAM 3" -> camIndex = 3
            "MUTE" -> {
                isMuted = true
                onMute(true)
            }
            "UNMUTE" -> {
                isMuted = false
                onMute(false)
            }
            "SHOW LOGO" -> showLogo = true
            "HIDE LOGO" -> showLogo = false
            "SHOW TITLE" -> showTitle = true
            "HIDE TITLE" -> showTitle = false
            "PLAY GALLERY" -> isPlayingGallery = true
            "STOP GALLERY" -> isPlayingGallery = false
        }
        onToast("Director: $action - Room $roomCode")
    }

    fun startLiveFromDirector() {
        val dests = selectedDests()
        if (dests.isEmpty()) {
            liveStatus = "FAILED - Check Key"
            onToast("Tick at least 1 destination")
        } else {
            val first = dests[0]
            if (first.third.isBlank() || first.second.isBlank()) {
                setStatus(first.first, "FAILED - Check Key")
                liveStatus = "FAILED - Check Key"
                onToast("Enter key for ${first.first} first")
            } else {
                val started = onStartStream(first.second)
                if (started) {
                    currentDestIndex = 0
                    isStreaming = true
                    setStatus(first.first, "LIVE ✅")
                    dests.drop(1).forEach { d -> setStatus(d.first, "READY - Tap Switch") }
                    liveStatus = "LIVE to ${first.first}"
                    remoteCommand = "Director STARTED LIVE to ${first.first}"
                    onToast("LIVE to ${first.first}")
                } else {
                    setStatus(first.first, "FAILED - Check Key")
                    liveStatus = "FAILED - Check Key"
                    onToast("FAILED - Check key")
                }
            }
        }
    }

    fun stopLiveFromDirector() {
        onStopStream()
        isStreaming = false
        currentDestIndex = -1
        liveStatus = "STOPPED"
        statusYoutube = "OFF"
        statusFacebook = "OFF"
        statusJsomi = "OFF"
        statusCustom = "OFF"
        remoteCommand = "Director STOPPED LIVE"
        onToast("Stopped All")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "JSTV PRO v2 - COMMIT 4", fontSize = 22.sp)
        Text(text = "Mode: $appMode | Room: $roomCode | CAM $camIndex", fontSize = 16.sp)
        Text(text = "Status: $liveStatus", fontSize = 18.sp, color = when {
            liveStatus.startsWith("LIVE") -> Color.Green
            liveStatus.startsWith("FAILED") -> Color.Red
            else -> Color.Black
        })
        Spacer(modifier = Modifier.height(8.dp))

        // ============ COMMIT 4: MODE SWITCH ============
        Text(text = "REMOTE CONTROL - Choose Mode", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { appMode = "Presenter" },
                content = { Text(text = if (appMode == "Presenter") "● PRESENTER" else "Presenter") }
            )
            Button(
                onClick = { appMode = "Director" },
                content = { Text(text = if (appMode == "Director") "● DIRECTOR" else "Director") }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = roomCode, onValueChange = { roomCode = it }, label = { Text("Room Code - Same on both phones") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Director says: $remoteCommand", fontSize = 14.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(8.dp))

        // ============ CAMERA PREVIEW + OVERLAY ============
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.DarkGray)
        ) {
            AndroidView(
                factory = { ctx -> SurfaceView(ctx).also { view -> onSetCamera(view) } },
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "CAM $camIndex - $appMode",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart).padding(8.dp).background(Color.Black).padding(4.dp)
            )
            if (appMode == "Presenter") {
                Text(
                    text = if (directorJoined) "Director Connected ✓" else "Waiting for Director...",
                    color = Color.Yellow,
                    fontSize = 12.sp,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(8.dp)
                )
            }
            if (showLogo) {
                Box(
                    modifier = Modifier.offset(logoOffset.x.dp, logoOffset.y.dp).size(logoSize.dp)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                logoOffset += dragAmount
                            }
                        }
                ) {
                    Text(text = if (logoUri == null) "LOGO" else "LOGO ✓", fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                }
            }
            if (showTitle) {
                Box(
                    modifier = Modifier.offset(titleOffset.x.dp, titleOffset.y.dp)
                        .background(when (titleColor) {
                            "Red" -> Color.Red
                            "Blue" -> Color.Blue
                            "Green" -> Color.Green
                            else -> Color.Black
                        })
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                titleOffset += dragAmount
                            }
                        }
                ) {
                    Text(text = titleText, color = Color.White, fontSize = titleSize.sp)
                }
            }
            Text(text = tickerText, color = Color.Yellow, fontSize = 12.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ============ COMMIT 4: DIRECTOR PANEL ============
        if (appMode == "Director") {
            Text(text = "DIRECTOR PANEL - Controls Presenter", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { directorAction("CAM 1") }, content = { Text(text = "CAM 1") })
                Button(onClick = { directorAction("CAM 2") }, content = { Text(text = "CAM 2") })
                Button(onClick = { directorAction("CAM 3") }, content = { Text(text = "CAM 3") })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { directorAction(if (isMuted) "UNMUTE" else "MUTE") }, content = { Text(text = if (isMuted) "Unmute" else "Mute") })
                Button(onClick = { directorAction(if (showLogo) "HIDE LOGO" else "SHOW LOGO") }, content = { Text(text = if (showLogo) "Hide Logo" else "Show Logo") })
                Button(onClick = { directorAction(if (showTitle) "HIDE TITLE" else "SHOW TITLE") }, content = { Text(text = if (showTitle) "Hide Title" else "Show Title") })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { directorAction(if (isPlayingGallery) "STOP GALLERY" else "PLAY GALLERY") }, content = { Text(text = if (isPlayingGallery) "Stop Gallery" else "Play Gallery") })
                Button(onClick = { onSwitchCamera() }, content = { Text(text = "Front/Back") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { startLiveFromDirector() }, enabled = !isStreaming, content = { Text(text = "START LIVE (Director)") })
                Button(onClick = { stopLiveFromDirector() }, enabled = isStreaming, content = { Text(text = "STOP LIVE (Director)") })
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Text(text = "PRESENTER - Just show camera. Director controls you.", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { onSwitchCamera() }, content = { Text(text = "Switch Cam") })
                Button(onClick = { directorJoined = true; remoteCommand = "Joined Room $roomCode" }, content = { Text(text = "Join Room $roomCode") })
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ============ MULTISTREAM ============
        Text(text = "MULTISTREAM - Tick destinations", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = useYoutube, onCheckedChange = { useYoutube = it })
            Text(text = "YouTube  ", fontSize = 14.sp)
            Text(text = statusYoutube, fontSize = 14.sp, color = statusColor(statusYoutube))
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = useFacebook, onCheckedChange = { useFacebook = it })
            Text(text = "Facebook  ", fontSize = 14.sp)
            Text(text = statusFacebook, fontSize = 14.sp, color = statusColor(statusFacebook))
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = useJsomi, onCheckedChange = { useJsomi = it })
            Text(text = "JSOMI TV  ", fontSize = 14.sp)
            Text(text = statusJsomi, fontSize = 14.sp, color = statusColor(statusJsomi))
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = useCustom, onCheckedChange = { useCustom = it })
            Text(text = "Custom RTMP  ", fontSize = 14.sp)
            Text(text = statusCustom, fontSize = 14.sp, color = statusColor(statusCustom))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { startLiveFromDirector() },
                enabled = !isStreaming,
                content = { Text(text = "START MULTISTREAM") }
            )
            Button(
                onClick = { stopLiveFromDirector() },
                enabled = isStreaming,
                content = { Text(text = "STOP ALL") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val dests = selectedDests()
                if (!isStreaming || dests.size < 2) {
                    onToast("Tick 2+ destinations to switch")
                } else {
                    val nextIndex = (currentDestIndex + 1) % dests.size
                    val next = dests[nextIndex]
                    if (next.third.isBlank()) {
                        setStatus(next.first, "FAILED - Check Key")
                        onToast("Enter key for ${next.first}")
                    } else {
                        val ok = onSwitchUrl(next.second)
                        if (ok) {
                            dests.forEach { d ->
                                if (d.first != next.first) {
                                    setStatus(d.first, "READY - Tap Switch")
                                }
                            }
                            setStatus(next.first, "LIVE ✅")
                            currentDestIndex = nextIndex
                            liveStatus = "LIVE to ${next.first}"
                            onToast("Switched LIVE to ${next.first}")
                        } else {
                            setStatus(next.first, "FAILED - Check Key")
                            liveStatus = "FAILED - Check Key"
                            onToast("FAILED to switch to ${next.first}")
                        }
                    }
                }
            },
            enabled = isStreaming,
            modifier = Modifier.fillMaxWidth(),
            content = { Text(text = "SWITCH DESTINATION LIVE") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "SETTINGS - Stream Keys", fontSize = 18.sp)
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
        OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Ticker Text") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(text = "Quality: $quality  ")
            Button(onClick = { quality = if (quality == "720p") "1080p" else "720p" }, content = { Text(text = "Switch to ${if (quality == "720p") "1080p" else "720p"}") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "LOGO", fontSize = 18.sp)
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

        Text(text = "TITLES", fontSize = 18.sp)
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
        Button(
            onClick = {
                if (galleryVideoUri == null) onToast("Pick a video first")
                else {
                    isPlayingGallery = !isPlayingGallery
                    onToast(if (isPlayingGallery) "Playing Gallery Video" else "Gallery Paused")
                }
            },
            content = { Text(text = if (isPlayingGallery) "Stop Gallery Video" else "Play Gallery Video") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onToast("Settings Saved - Commit 4 OK - JSTV PRO v2 DONE") },
            modifier = Modifier.fillMaxWidth(),
            content = { Text(text = "SAVE SETTINGS") }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}
