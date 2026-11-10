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
                    // Live switch: stop old stream, keep preview, start new URL
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
    // --- SETTINGS / KEYS ---
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live") }

    // --- COMMIT 3: MULTISTREAM ---
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

    // --- LOGO + TITLES ---
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var logoSize by remember { mutableStateOf(80f) }

    var titleText by remember { mutableStateOf("BREAKING") }
    var showTitle by remember { mutableStateOf(true) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 200f)) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }

    // --- GALLERY + LOOP ---
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isPlayingGallery by remember { mutableStateOf(false) }

    // Helper: build list of selected destinations as (name, fullUrl, key)
    fun selectedDests(): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        if (useYoutube) list.add(Triple("YouTube", "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey, youtubeKey))
        if (useFacebook) list.add(Triple("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey, facebookKey))
        if (useJsomi) list.add(Triple("JSOMI", "rtmp://jsomi.tv/live/" + jsomiKey, jsomiKey))
        if (useCustom) list.add(Triple("Custom", if (customUrl.isBlank()) "" else customUrl + "/" + customKey, customKey))
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "JSTV PRO v2 - COMMIT 3", fontSize = 22.sp)
        Text(text = "Status: $liveStatus", fontSize = 18.sp, color = when {
            liveStatus.startsWith("LIVE") -> Color.Green
            liveStatus.startsWith("FAILED") -> Color.Red
            else -> Color.Black
        })
        Spacer(modifier = Modifier.height(8.dp))

        // ============ CAMERA PREVIEW + OVERLAY ============
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.DarkGray)
        ) {
            AndroidView(
                factory = { ctx -> SurfaceView(ctx).also { view -> onSetCamera(view) } },
                modifier = Modifier.fillMaxSize()
            )
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

        // ============ COMMIT 3: MULTISTREAM PANEL ============
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
                onClick = {
                    val dests = selectedDests()
                    if (dests.isEmpty()) {
                        liveStatus = "FAILED - Check Key"
                        onToast("Tick at least 1 destination")
                        return@Button
                    }
                    val first = dests[0]
                    if (first.third.isBlank() || first.second.isBlank()) {
                        setStatus(first.first, "FAILED - Check Key")
                        liveStatus = "FAILED - Check Key"
                        onToast("Enter key for ${first.first} first")
                        return@Button
                    }
                    liveStatus = "CONNECTING to ${first.first}..."
                    val started = onStartStream(first.second)
                    if (started) {
                        currentDestIndex = 0
                        isStreaming = true
                        setStatus(first.first, "LIVE ✅")
                        // Others are ready to switch to
                        for (i in 1 until dests.size) setStatus(dests[i].first, "READY - Tap Switch")
                        liveStatus = "LIVE to ${first.first}"
                        onToast("LIVE to ${first.first}")
                    } else {
                        setStatus(first.first, "FAILED - Check Key")
                        liveStatus = "FAILED - Check Key"
                        isStreaming = false
                        onToast("FAILED - Check key for ${first.first}")
                    }
                },
                enabled = !isStreaming
            ) { Text("START MULTISTREAM") }

            Button(
                onClick = {
                    onStopStream()
                    isStreaming = false
                    currentDestIndex = -1
                    liveStatus = "STOPPED"
                    statusYoutube = if (useYoutube) "OFF" else "OFF"
                    statusFacebook = "OFF"
                    statusJsomi = "OFF"
                    statusCustom = "OFF"
                    onToast("Stopped All")
                },
                enabled = isStreaming
            ) { Text("STOP ALL") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val dests = selectedDests()
                if (!isStreaming || dests.size 
