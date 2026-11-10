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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1

class MainActivity : ComponentActivity() {
    private var rtmpCamera1: RtmpCamera1? = null
    private var hasPermissions by mutableStateOf(false)
    private var isLandscape by mutableStateOf(false)
    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private var onVideoPicked: ((Uri?) -> Unit)? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val camOk = results[Manifest.permission.CAMERA] == true
            val micOk = results[Manifest.permission.RECORD_AUDIO] == true
            hasPermissions = camOk && micOk
        }

    private val logoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            onLogoPicked?.invoke(uri)
        }

    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            onVideoPicked?.invoke(uri)
        }

    private fun askPermissions() {
        val camOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val micOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (camOk && micOk) {
            hasPermissions = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun makeCamera(view: SurfaceView): RtmpCamera1 {
        val checker = object : ConnectChecker {
            override fun onConnectionStarted(url: String) {
            }
            override fun onConnectionSuccess() {
            }
            override fun onConnectionFailed(reason: String) {
            }
            override fun onNewBitrate(bitrate: Long) {
            }
            override fun onDisconnect() {
            }
            override fun onAuthError() {
            }
            override fun onAuthSuccess() {
            }
        }
        return RtmpCamera1(view, checker)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()
        setContent {
            JstvScreen(
                hasPermissions = hasPermissions,
                isLandscape = isLandscape,
                onAsk = { askPermissions() },
                onRotate = {
                    isLandscape = !isLandscape
                    requestedOrientation = if (isLandscape) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                },
                onPickLogo = { cb ->
                    onLogoPicked = cb
                    logoPicker.launch("image/*")
                },
                onPickVideo = { cb ->
                    onVideoPicked = cb
                    videoPicker.launch("video/*")
                },
                onToast = { msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                },
                onSetCamera = { view ->
                    rtmpCamera1 = makeCamera(view)
                },
                onPreview = {
                    try {
                        if (rtmpCamera1?.isOnPreview != true) {
                            rtmpCamera1?.prepareVideo()
                            rtmpCamera1?.prepareAudio()
                            rtmpCamera1?.startPreview()
                        }
                    } catch (e: Exception) {
                    }
                },
                onStartStream = { url ->
                    val a = rtmpCamera1?.prepareAudio() ?: false
                    val v = rtmpCamera1?.prepareVideo() ?: false
                    if (a && v) {
                        try {
                            rtmpCamera1?.startPreview()
                        } catch (e: Exception) {
                        }
                        rtmpCamera1?.startStream(url)
                        true
                    } else {
                        false
                    }
                },
                onSwitchUrl = { url ->
                    try {
                        rtmpCamera1?.stopStream()
                        rtmpCamera1?.startStream(url)
                        true
                    } catch (e: Exception) {
                        false
                    }
                },
                onStop = {
                    try {
                        rtmpCamera1?.stopStream()
                    } catch (e: Exception) {
                    }
                    try {
                        rtmpCamera1?.startPreview()
                    } catch (e: Exception) {
                    }
                },
                onSwitchCam = {
                    try {
                        rtmpCamera1?.switchCamera()
                    } catch (e: Exception) {
                    }
                },
                onMute = { mute ->
                    if (mute) {
                        rtmpCamera1?.disableAudio()
                    } else {
                        rtmpCamera1?.enableAudio()
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        try {
            rtmpCamera1?.stopStream()
            rtmpCamera1?.stopPreview()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }
}

data class TitleItem(
    val id: Int,
    val text: String,
    val offset: Offset
)

@Composable
fun JstvScreen(
    hasPermissions: Boolean,
    isLandscape: Boolean,
    onAsk: () -> Unit,
    onRotate: () -> Unit,
    onPickLogo: ((Uri?) -> Unit) -> Unit,
    onPickVideo: ((Uri?) -> Unit) -> Unit,
    onToast: (String) -> Unit,
    onSetCamera: (SurfaceView) -> Unit,
    onPreview: () -> Unit,
    onStartStream: (String) -> Boolean,
    onSwitchUrl: (String) -> Boolean,
    onStop: () -> Unit,
    onSwitchCam: () -> Unit,
    onMute: (Boolean) -> Unit
) {
    var tab by remember { mutableStateOf("BROADCAST") }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var liveTarget by remember { mutableStateOf("Facebook") }
    var useYoutube by remember { mutableStateOf(true) }
    var useFacebook by remember { mutableStateOf(true) }
    var liveStatus by remember { mutableStateOf("READY") }
    var isStreaming by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var logoSize by remember { mutableStateOf(80f) }
    var showTitle by remember { mutableStateOf(true) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live - ") }
    var tickerMoving by remember { mutableStateOf(true) }
    var tickerLoop by remember { mutableStateOf(true) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isGallery by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(4) }

    var titles by remember {
        mutableStateOf(
            listOf(
                TitleItem(1, "BREAKING", Offset(20f, 200f)),
                TitleItem(2, "ENQUIRY", Offset(20f, 260f)),
                TitleItem(3, "RESTORATION", Offset(20f, 320f))
            )
        )
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            onPreview()
        }
    }

    val infinite = rememberInfiniteTransition(label = "ticker")
    val tickerX by infinite.animateFloat(
        initialValue = 400f,
        targetValue = -800f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerX"
    )

    fun urlFor(name: String): String {
        if (name == "YouTube") {
            return "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey
        }
        if (name == "Facebook") {
            return "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey
        }
        return ""
    }

    fun keyFor(name: String): String {
        if (name == "YouTube") {
            return youtubeKey
        }
        return facebookKey
    }

    fun goLive() {
        if (!hasPermissions) {
            onToast("Grant Camera + Mic first")
            onAsk()
            return
        }
        val key = keyFor(liveTarget)
        if (key.isBlank()) {
            liveStatus = "FAILED - Enter Key for " + liveTarget
            onToast("Enter " + liveTarget + " key in SETUP")
            tab = "SETUP"
            return
        }
        liveStatus = "CONNECTING to " + liveTarget
        val ok = onStartStream(urlFor(liveTarget))
        if (ok) {
            isStreaming = true
            liveStatus = "LIVE to " + liveTarget
            onToast("LIVE to " + liveTarget)
        } else {
            liveStatus = "FAILED - Check Key"
            onToast("FAILED - Check Key")
        }
    }

    fun endLive() {
        val to = liveTarget
        onStop()
        isStreaming = false
        liveStatus = "ENDED - Saved to " + to
        onToast("ENDED - Saved to " + to)
    }

    fun titleBg(): Color {
        if (titleColor == "Blue") {
            return Color.Blue
        }
        if (titleColor == "Green") {
            return Color.Green
        }
        return Color.Red
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { tab = "BROADCAST" }) {
                Text("📺 BROADCAST")
            }
            Button(onClick = { tab = "SETUP" }) {
                Text("⚙️ SETUP")
            }
        }

        if (!hasPermissions) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding(12.dp)
            ) {
                Button(onClick = { onAsk() }) {
                    Text("GRANT CAMERA + MIC")
                }
            }
        }

        if (tab == "BROADCAST") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (hasPermissions) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).also { v ->
                                onSetCamera(v)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isStreaming) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Red)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "● LIVE to " + liveTarget,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                if (showLogo) {
                    Box(
                        modifier = Modifier
                            .offset(logoOffset.x.dp, logoOffset.y.dp)
                            .size(logoSize.dp)
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    logoOffset += drag
                                }
                            }
                    ) {
                        Text(
                            text = if (logoUri == null) "LOGO" else "LOGO ✓",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                if (showTitle) {
                    titles.forEach { item ->
                        val one = item
                        Box(
                            modifier = Modifier
                                .offset(one.offset.x.dp, one.offset.y.dp)
                                .background(titleBg())
                                .padding(8.dp, 4.dp)
                                .pointerInput(one.id) {
                                    detectDragGestures { change, drag ->
                                        change.consume()
                                        titles = titles.map {
                                            if (it.id == one.id) {
                                                it.copy(offset = it.offset + drag)
                                            } else {
                                                it
                                            }
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = one.text,
                                color = Color.White,
                                fontSize = titleSize.sp
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 105.dp)
                        .background(Color.Black)
                ) {
                    if (tickerMoving) {
                        Text(
                            text = tickerText + " • " + tickerText,
                            color = Color.Yellow,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .offset(if (tickerLoop) tickerX.dp else 0.dp)
                                .padding(6.dp)
                        )
                    } else {
                        Text(
                            text = tickerText,
                            color = Color.Yellow,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = liveStatus,
                        color = if (isStreaming) Color.Green else Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            if (isStreaming) {
                                endLive()
                            } else {
                                goLive()
                            }
                        }) {
                            if (isStreaming) {
                                Text("⏹ END LIVE")
                            } else {
                                Text("🔴 GO LIVE")
                            }
                        }
                        Button(onClick = { onSwitchCam() }) {
                            Text("Switch Cam")
                        }
                        Button(onClick = { onRotate() }) {
                            if (isLandscape) {
                                Text("↻ Portrait")
                            } else {
                                Text("↻ Landscape")
                            }
                        }
                        Button(onClick = {
                            isMuted = !isMuted
                            onMute(isMuted)
                        }) {
                            if (isMuted) {
                                Text("Unmute")
                            } else {
                                Text("Mute")
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "⚙️ SETUP - All settings here",
                    fontSize = 20.sp
                )
                Text(
                    text = "Status: " + liveStatus,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "1) LIVE TARGET - Pick ONE",
                    fontSize = 18.sp
                )
                Text(
                    text = "Phone streams to 1 place at a time.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { liveTarget = "YouTube" }) {
                        if (liveTarget == "YouTube") {
                            Text("● YouTube")
                        } else {
                            Text("YouTube")
                        }
                    }
                    Button(onClick = { liveTarget = "Facebook" }) {
                        if (liveTarget == "Facebook") {
                            Text("● Facebook")
                        } else {
                            Text("Facebook")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Target now: " + liveTarget,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "2) KEYS",
                    fontSize = 18.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = useYoutube,
                        onCheckedChange = { useYoutube = it }
                    )
                    Text(text = "YouTube")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = useFacebook,
                        onCheckedChange = { useFacebook = it }
                    )
                    Text(text = "Facebook")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = youtubeKey,
                    onValueChange = { youtubeKey = it },
                    label = { Text("YouTube Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = facebookKey,
                    onValueChange = { facebookKey = it },
                    label = { Text("Facebook Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Facebook: copy ONLY key from Facebook > Live > Stream Key. Get fresh key each live.",
                    fontSize = 12.sp,
                    color = Color.Blue
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "3) LOGO",
                    fontSize = 18.sp
                )
                Button(onClick = {
                    onPickLogo { uri ->
                        logoUri = uri
                    }
                }) {
                    Text("Pick Logo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Show Logo  ")
                    Switch(
                        checked = showLogo,
                        onCheckedChange = { showLogo = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        logoSize = (logoSize + 20f).coerceAtMost(180f)
                    }) {
                        Text("Logo +")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        logoSize = (logoSize - 20f).coerceAtLeast(40f)
                    }) {
                        Text("Logo -")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "4) TITLES - Each moves alone",
                    fontSize = 18.sp
                )
                Text(
                    text = "Drag one title on BROADCAST. Others stay.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                titles.forEach { item ->
                    val one = item
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = one.text,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                titles = titles.filter { it.id != one.id }
                            }) {
                                Text("Delete")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) {
                                        it.copy(offset = it.offset + Offset(-20f, 0f))
                                    } else {
                                        it
                                    }
                                }
                            }) {
                                Text("⬅️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) {
                                        it.copy(offset = it.offset + Offset(20f, 0f))
                                    } else {
                                        it
                                    }
                                }
                            }) {
                                Text("➡️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) {
                                        it.copy(offset = it.offset + Offset(0f, -20f))
                                    } else {
                                        it
                                    }
                                }
                            }) {
                                Text("⬆️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) {
                                        it.copy(offset = it.offset + Offset(0f, 20f))
                                    } else {
                                        it
                                    }
                                }
                            }) {
                                Text("⬇️")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("New Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Button(onClick = {
                        if (newTitle.isNotBlank()) {
                            titles = titles + TitleItem(
                                nextId,
                                newTitle,
                                Offset(20f, 200f + titles.size * 60f)
                            )
                            nextId = nextId + 1
                            newTitle = ""
                        }
                    }) {
                        Text("+ ADD TITLE")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        titles = listOf(
                            TitleItem(1, "BREAKING", Offset(20f, 200f)),
                            TitleItem(2, "ENQUIRY", Offset(20f, 260f)),
                            TitleItem(3, "RESTORATION", Offset(20f, 320f))
                        )
                        nextId = 4
                    }) {
                        Text("Reset to 3")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Show Titles  ")
                    Switch(
                        checked = showTitle,
                        onCheckedChange = { showTitle = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (titleColor == "Red") {
                            titleColor = "Blue"
                        } else if (titleColor == "Blue") {
                            titleColor = "Green"
                        } else {
                            titleColor = "Red"
                        }
                    }) {
                        Text("Color: " + titleColor)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = {
                        titleSize = (titleSize + 4f).coerceAtMost(48f)
                    }) {
                        Text("Title +")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        titleSize = (titleSize - 4f).coerceAtLeast(12f)
                    }) {
                        Text("Title -")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "5) MOVING TEXT + LOOP",
                    fontSize = 18.sp
                )
                OutlinedTextField(
                    value = tickerText,
                    onValueChange = { tickerText = it },
                    label = { Text("Moving Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Moving  ")
                    Switch(
                        checked = tickerMoving,
                        onCheckedChange = { tickerMoving = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (tickerMoving) "MOVING" else "STATIC")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Loop  ")
                    Switch(
                        checked = tickerLoop,
                        onCheckedChange = { tickerLoop = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (tickerLoop) "LOOP ON" else "LOOP OFF")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "6) GALLERY + LOOP",
                    fontSize = 18.sp
                )
                Button(onClick = {
                    onPickVideo { uri ->
                        galleryUri = uri
                    }
                }) {
                    Text("Pick Gallery Video")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (galleryUri == null) {
                        "No video picked"
                    } else {
                        "Video picked ✓"
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Loop  ")
                    Switch(
                        checked = loopOn,
                        onCheckedChange = { loopOn = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (loopOn) "LOOP ON" else "LOOP OFF")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (galleryUri == null) {
                        onToast("Pick a video first")
                    } else {
                        isGallery = !isGallery
                    }
                }) {
                    if (isGallery) {
                        Text("Stop Gallery Video")
                    } else {
                        Text("Play Gallery Video")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        tab = "BROADCAST"
                        onToast("Back to Broadcast - Target: " + liveTarget)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✅ DONE - GO TO BROADCAST")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
