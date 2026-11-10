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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

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

data class Destination(
    val id: Int,
    val name: String,
    val key: String
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
    onStop: () -> Unit,
    onSwitchCam: () -> Unit,
    onMute: (Boolean) -> Unit
) {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf("BROADCAST") }
    var liveTarget by remember { mutableStateOf("Facebook") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var tiktokKey by remember { mutableStateOf("") }
    var instagramKey by remember { mutableStateOf("") }
    var twitchKey by remember { mutableStateOf("") }
    var kickKey by remember { mutableStateOf("") }
    var liveStatus by remember { mutableStateOf("READY") }
    var isStreaming by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var cameraName by remember { mutableStateOf("Back Camera") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
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
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    var galleryLoop by remember { mutableStateOf(true) }
    var isGallery by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var nextId by remember { mutableStateOf(4) }
    var introText by remember { mutableStateOf("Welcome to JSTV - Jesus Saves") }
    var showIntro by remember { mutableStateOf(true) }
    var introVisible by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }
    var nextDestId by remember { mutableStateOf(3) }
    var secondTarget by remember { mutableStateOf("None") }
    var destinations by remember {
        mutableStateOf(
            listOf(
                Destination(1, "Facebook", ""),
                Destination(2, "YouTube", "")
            )
        )
    }
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

    LaunchedEffect(Unit) {
        while (true) {
            val fmt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            currentTime = fmt.format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(introVisible) {
        if (introVisible) {
            delay(3000)
            introVisible = false
        }
    }

    val infinite = rememberInfiniteTransition(label = "ticker")
    val tickerX by infinite.animateFloat(
        initialValue = 400f,
        targetValue = -800f,
        animationSpec = infiniteRepeatable(
            animation = tween((tickerSpeed * 1000).toInt(), easing = LinearEasing),
            repeatMode = if (tickerLoop) RepeatMode.Restart else RepeatMode.Restart
        ),
        label = "tickerX"
    )

    fun keyFor(name: String): String {
        if (name == "YouTube") {
            return youtubeKey
        }
        if (name == "TikTok") {
            return tiktokKey
        }
        if (name == "Instagram") {
            return instagramKey
        }
        if (name == "Twitch") {
            return twitchKey
        }
        if (name == "Kick") {
            return kickKey
        }
        if (name == "Custom") {
            return customKey
        }
        return facebookKey
    }

    fun urlFor(name: String): String {
        if (name == "YouTube") {
            return "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey
        }
        if (name == "TikTok") {
            return "rtmp://push-rtmp-l1.tiktok.com/live/" + tiktokKey
        }
        if (name == "Instagram") {
            return "rtmps://live-upload.instagram.com:443/rtmp/" + instagramKey
        }
        if (name == "Twitch") {
            return "rtmp://live.twitch.tv/app/" + twitchKey
        }
        if (name == "Kick") {
            return "rtmp://live.kick.com/app/" + kickKey
        }
        if (name == "Custom") {
            if (customUrl.isBlank()) {
                return customKey
            }
            return customUrl + "/" + customKey
        }
        return "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey
    }

    fun watchLink(name: String): String {
        if (name == "YouTube") {
            return "https://youtube.com/live - JSTV Live"
        }
        if (name == "Facebook") {
            return "https://facebook.com/live - JSTV Live"
        }
        if (name == "Twitch") {
            return "https://twitch.tv - JSTV Live"
        }
        if (name == "TikTok") {
            return "https://tiktok.com/live - JSTV Live"
        }
        if (name == "Instagram") {
            return "https://instagram.com/live - JSTV Live"
        }
        if (name == "Kick") {
            return "https://kick.com - JSTV Live"
        }
        return "JSTV Live - " + name
    }

    fun goLive() {
        if (!hasPermissions) {
            onToast("Grant Camera + Mic first")
            onAsk()
            return
        }
        val key = keyFor(liveTarget)
        if (liveTarget != "Custom" && key.isBlank()) {
            liveStatus = "FAILED - Enter Key for " + liveTarget
            onToast("Enter " + liveTarget + " key in SETUP")
            tab = "SETUP"
            return
        }
        liveStatus = "CONNECTING to " + liveTarget
        val ok = onStartStream(urlFor(liveTarget))
        if (ok) {
            isStreaming = true
            showSave = false
            liveStatus = "LIVE to " + liveTarget
            if (showIntro) {
                introVisible = true
            }
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
        introVisible = false
        showSave = true
        liveStatus = "ENDED - Tap SAVE to save"
        onToast("ENDED - Tap SAVE")
    }

    fun titleBg(): Color {
        if (titleColor == "Blue") {
            return Color.Blue
        }
        if (titleColor == "Green") {
            return Color.Green
        }
        if (titleColor == "Yellow") {
            return Color.Yellow
        }
        if (titleColor == "Purple") {
            return Color(0xFF800080)
        }
        if (titleColor == "Orange") {
            return Color(0xFFFF9800)
        }
        return Color.Red
    }

    fun tickerBg(): Color {
        if (tickerColor == "White") {
            return Color.White
        }
        if (tickerColor == "Green") {
            return Color.Green
        }
        if (tickerColor == "Red") {
            return Color.Red
        }
        if (tickerColor == "Cyan") {
            return Color.Cyan
        }
        return Color.Yellow
    }

    fun fontFor(): FontFamily {
        if (titleFont == "Serif") {
            return FontFamily.Serif
        }
        if (titleFont == "Mono") {
            return FontFamily.Monospace
        }
        if (titleFont == "Cursive") {
            return FontFamily.Cursive
        }
        return FontFamily.Default
    }

    fun tickerAlignValue(): TextAlign {
        if (tickerAlign == "Center") {
            return TextAlign.Center
        }
        if (tickerAlign == "Right") {
            return TextAlign.End
        }
        return TextAlign.Start
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!isStreaming) {
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
                        factory = { ctx2 ->
                            SurfaceView(ctx2).also { v ->
                                onSetCamera(v)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isGallery && galleryUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GALLERY VIDEO PLAYING\nLoop: " + if (galleryLoop) "ON" else "OFF",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (introVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = introText,
                            color = Color.White,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
                if (isStreaming || showSave) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Red)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isStreaming) "● LIVE " else "● ENDED ",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentTime,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (isStreaming) {
                            Text(
                                text = "  " + liveTarget,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
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
                                fontSize = titleSize.sp,
                                fontFamily = fontFor(),
                                fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = if (isStreaming) 70.dp else 105.dp)
                        .background(Color.Black)
                ) {
                    if (tickerMoving) {
                        Text(
                            text = tickerText + " • " + tickerText,
                            color = tickerBg(),
                            fontSize = tickerSize.sp,
                            maxLines = 1,
                            textAlign = tickerAlignValue(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(if (tickerLoop) tickerX.dp else 0.dp)
                                .padding(6.dp)
                        )
                    } else {
                        Text(
                            text = tickerText,
                            color = tickerBg(),
                            fontSize = tickerSize.sp,
                            maxLines = 1,
                            textAlign = tickerAlignValue(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                        )
                    }
                }
                if (!isStreaming) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = liveStatus + " | " + currentTime,
                            color = if (isStreaming) Color.Green else Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { goLive() }) {
                                Text("🔴 GO LIVE")
                            }
                            Button(onClick = {
                                onSwitchCam()
                                cameraName = if (cameraName == "Back Camera") "Front Camera" else "Back Camera"
                            }) {
                                Text(cameraName)
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
                        if (showSave) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = {
                                    showSave = false
                                    liveStatus = "SAVED ✓"
                                    onToast("SAVED ✓ - Video saved")
                                }) {
                                    Text("💾 SAVE VIDEO")
                                }
                                Button(onClick = {
                                    val send = Intent(Intent.ACTION_SEND)
                                    send.type = "text/plain"
                                    send.putExtra(Intent.EXTRA_TEXT, watchLink(liveTarget))
                                    ctx.startActivity(Intent.createChooser(send, "Share Live Link"))
                                }) {
                                    Text("🔗 SHARE LINK")
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { endLive() }) {
                                Text("⏹ END LIVE")
                            }
                            Button(onClick = {
                                val send = Intent(Intent.ACTION_SEND)
                                send.type = "text/plain"
                                send.putExtra(Intent.EXTRA_TEXT, watchLink(liveTarget))
                                ctx.startActivity(Intent.createChooser(send, "Share Live Link"))
                            }) {
                                Text("🔗 SHARE")
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "⚙️ SETUP - All settings here", fontSize = 20.sp, color = Color.White)
                Text(text = "Status: " + liveStatus + " | " + currentTime, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "1) SOURCES - Pick ONE to stream to", fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                val sources = listOf("YouTube", "Facebook", "TikTok", "Instagram", "Twitch", "Kick", "Custom")
                sources.forEach { src ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = src, color = Color.White, fontSize = 16.sp)
                        Button(onClick = { liveTarget = src }) {
                            if (liveTarget == src) {
                                Text("● " + src)
                            } else {
                                Text(src)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text = "Target now: " + liveTarget, fontSize = 14.sp, color = Color.Green)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "2) KEYS - Paste key for each source", fontSize = 18.sp, color = Color.White)
                OutlinedTextField(value = youtubeKey, onValueChange = { youtubeKey = it }, label = { Text("YouTube Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = facebookKey, onValueChange = { facebookKey = it }, label = { Text("Facebook Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = tiktokKey, onValueChange = { tiktokKey = it }, label = { Text("TikTok Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = instagramKey, onValueChange = { instagramKey = it }, label = { Text("Instagram Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = twitchKey, onValueChange = { twitchKey = it }, label = { Text("Twitch Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = kickKey, onValueChange = { kickKey = it }, label = { Text("Kick Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = customUrl, onValueChange = { customUrl = it }, label = { Text("Custom RTMP URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = customKey, onValueChange = { customKey = it }, label = { Text("Custom Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "3) MULTIPLE CONNECTIONS", fontSize = 18.sp, color = Color.White)
                Text(text = "Main: " + liveTarget + " | 2nd: " + secondTarget, fontSize = 14.sp, color = Color.White)
                destinations.forEach { dest ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dest.name, color = Color.White)
                        Button(onClick = {
                            destinations = destinations.filter { it.id != dest.id }
                            onToast("Removed " + dest.name)
                        }) {
                            Text("Remove Connection")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row {
                    Button(onClick = {
                        secondTarget = if (secondTarget == "None") "YouTube" else "Facebook"
                        destinations = destinations + Destination(nextDestId, secondTarget, "")
                        nextDestId = nextDestId + 1
                    }) {
                        Text("+ ADD 2ND CONNECTION")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "4) CAMERA", fontSize = 18.sp, color = Color.White)
                Text(text = "Camera now: " + cameraName, color = Color.White)
                Row {
                    Button(onClick = {
                        onSwitchCam()
                        cameraName = if (cameraName == "Back Camera") "Front Camera" else "Back Camera"
                    }) {
                        Text("Switch Camera")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "5) INTRO - Shows for 3 sec on GO LIVE", fontSize = 18.sp, color = Color.White)
                OutlinedTextField(value = introText, onValueChange = { introText = it }, label = { Text("Intro Text") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Show Intro  ", color = Color.White)
                    Switch(checked = showIntro, onCheckedChange = { showIntro = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (showIntro) "INTRO ON" else "INTRO OFF", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { introVisible = true }) {
                        Text("Preview Intro")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "6) LOGO", fontSize = 18.sp, color = Color.White)
                Button(onClick = { onPickLogo { uri -> logoUri = uri } }) {
                    Text("Pick Logo")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Show Logo  ", color = Color.White)
                    Switch(checked = showLogo, onCheckedChange = { showLogo = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { logoSize = (logoSize + 20f).coerceAtMost(180f) }) {
                        Text("Logo +")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { logoSize = (logoSize - 20f).coerceAtLeast(40f) }) {
                        Text("Logo -")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "7) TITLES - SEE + EDIT EACH ONE", fontSize = 18.sp, color = Color.White)
                Text(text = "Each title has its own box, edit, delete, move.", fontSize = 13.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                titles.forEach { item ->
                    val one = item
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .padding(8.dp)
                    ) {
                        Text(text = "TITLE: " + one.text, color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = one.text,
                            onValueChange = { newText ->
                                titles = titles.map {
                                    if (it.id == one.id) it.copy(text = newText) else it
                                }
                            },
                            label = { Text("Edit this title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) it.copy(offset = it.offset + Offset(-20f, 0f)) else it
                                }
                            }) {
                                Text("⬅️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) it.copy(offset = it.offset + Offset(20f, 0f)) else it
                                }
                            }) {
                                Text("➡️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) it.copy(offset = it.offset + Offset(0f, -20f)) else it
                                }
                            }) {
                                Text("⬆️")
                            }
                            Button(onClick = {
                                titles = titles.map {
                                    if (it.id == one.id) it.copy(offset = it.offset + Offset(0f, 20f)) else it
                                }
                            }) {
                                Text("⬇️")
                            }
                            Button(onClick = {
                                titles = titles.filter { it.id != one.id }
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("New Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Button(onClick = {
                        if (newTitle.isNotBlank()) {
                            titles = titles + TitleItem(nextId, newTitle, Offset(20f, 200f + titles.size * 60f))
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
                Text(text = "THEME - Title Background Color", fontSize = 16.sp, color = Color.White)
                Row {
                    listOf("Red", "Blue", "Green", "Yellow", "Purple", "Orange").forEach { c ->
                        Button(onClick = { titleColor = c }) {
                            if (titleColor == c) Text("● " + c) else Text(c)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "FONT - Title Font Style", fontSize = 16.sp, color = Color.White)
                Row {
                    listOf("Normal", "Serif", "Mono", "Cursive").forEach { f ->
                        Button(onClick = { titleFont = f }) {
                            if (titleFont == f) Text("● " + f) else Text(f)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Show Titles  ", color = Color.White)
                    Switch(checked = showTitle, onCheckedChange = { showTitle = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Bold  ", color = Color.White)
                    Switch(checked = titleBold, onCheckedChange = { titleBold = it })
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Button(onClick = { titleSize = (titleSize + 4f).coerceAtMost(48f) }) {
                        Text("Title +")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { titleSize = (titleSize - 4f).coerceAtLeast(12f) }) {
                        Text("Title -")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "8) MOVING TEXT - SINGLE LINE + ALIGN", fontSize = 18.sp, color = Color.White)
                OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Moving Text - One Line") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Preview - Single Line:", color = Color.White)
                Text(text = tickerText, color = tickerBg(), fontSize = tickerSize.sp, maxLines = 1, textAlign = tickerAlignValue(), modifier = Modifier.fillMaxWidth().background(Color.Black))
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    listOf("Left", "Center", "Right").forEach { a ->
                        Button(onClick = { tickerAlign = a }) {
                            if (tickerAlign == a) Text("● " + a) else Text(a)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    listOf("Yellow", "White", "Green", "Red", "Cyan").forEach { c ->
                        Button(onClick = { tickerColor = c }) {
                            if (tickerColor == c) Text("● " + c) else Text(c)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Moving  ", color = Color.White)
                    Switch(checked = tickerMoving, onCheckedChange = { tickerMoving = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (tickerMoving) "MOVING" else "STATIC", color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "LOOP Moving Text  ", color = Color.White)
                    Switch(checked = tickerLoop, onCheckedChange = { tickerLoop = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (tickerLoop) "LOOP ON - Repeats" else "LOOP OFF - Stops", color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Button(onClick = { tickerSize = (tickerSize + 2f).coerceAtMost(32f) }) {
                        Text("Text +")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { tickerSize = (tickerSize - 2f).coerceAtLeast(10f) }) {
                        Text("Text -")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { tickerSpeed = (tickerSpeed - 2f).coerceAtLeast(4f) }) {
                        Text("Faster")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { tickerSpeed = (tickerSpeed + 2f).coerceAtMost(20f) }) {
                        Text("Slower")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "9) GALLERY + LOOP - VISIBLE", fontSize = 18.sp, color = Color.White)
                Button(onClick = { onPickVideo { uri -> galleryUri = uri } }) {
                    Text("Pick Gallery Video")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (galleryUri == null) "No video picked" else "Video picked ✓", color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "LOOP Gallery Video  ", color = Color.White)
                    Switch(checked = galleryLoop, onCheckedChange = { galleryLoop = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (galleryLoop) "LOOP ON - Repeats" else "LOOP OFF", color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(onClick = {
                    if (galleryUri == null) {
                        onToast("Pick a video first")
                    } else {
                        isGallery = !isGallery
                    }
                }) {
                    if (isGallery) Text("Stop Gallery Video") else Text("Play Gallery Video")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "10) SHARE LINK", fontSize = 18.sp, color = Color.White)
                Button(onClick = {
                    val send = Intent(Intent.ACTION_SEND)
                    send.type = "text/plain"
                    send.putExtra(Intent.EXTRA_TEXT, watchLink(liveTarget))
                    ctx.startActivity(Intent.createChooser(send, "Share Live Link"))
                }) {
                    Text("🔗 SHARE LINK - " + liveTarget)
                }
                Spacer(modifier = Modifier.height(20.dp))
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
