package com.jsomi.jstv

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVPro() }
    }
}

@Composable
fun JSTVPro() {
    var tab by remember { mutableStateOf(0) }
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Restoration Sunday - Jesus Saves - ") }
    var loopOn by remember { mutableStateOf(false) }
    var logoOn by remember { mutableStateOf(true) }
    var dest by remember { mutableStateOf("YouTube") }

    if (tab == 0) {
        StudioScreen(
            ytKey = ytKey, fbKey = fbKey, jsomiKey = jsomiKey, customKey = customKey,
            ticker = ticker, loopOn = loopOn, logoOn = logoOn, dest = dest,
            onDest = { v -> dest = v },
            onSettings = { tab = 1 }
        )
    } else {
        SettingsScreen(
            ytKey = ytKey, onYt = { v -> ytKey = v },
            fbKey = fbKey, onFb = { v -> fbKey = v },
            jsomiKey = jsomiKey, onJsomi = { v -> jsomiKey = v },
            customUrl = customUrl, onCustomUrl = { v -> customUrl = v },
            customKey = customKey, onCustomKey = { v -> customKey = v },
            roomCode = roomCode, onRoom = { v -> roomCode = v },
            quality = quality, onQuality = { v -> quality = v },
            ticker = ticker, onTicker = { v -> ticker = v },
            loopOn = loopOn, onLoop = { v -> loopOn = v },
            logoOn = logoOn, onLogo = { v -> logoOn = v },
            dest = dest, onDest = { v -> dest = v },
            onBack = { tab = 0 }
        )
    }
}

// MOVING TICKER - real marquee, moves left forever
@Composable
fun MovingTicker(text: String) {
    var offsetX by remember { mutableStateOf(400f) }
    LaunchedEffect(text) {
        while (true) {
            delay(30)
            offsetX -= 4f
            if (offsetX < -1200f) offsetX = 400f
        }
    }
    Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.75f)).height(28.dp)) {
        Text(text, color = Color.White, fontSize = 13.sp, maxLines = 1,
            modifier = Modifier.offset { IntOffset(offsetX.roundToInt(), 4) }.padding(start = 0.dp))
    }
}

@Composable
fun StudioScreen(ytKey: String, fbKey: String, jsomiKey: String, customKey: String, ticker: String, loopOn: Boolean, logoOn: Boolean, dest: String, onDest: (String) -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var isFront by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("RESTORATION SUNDAY") }
    var viewers by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Ready - Add key in Settings") }
    var showTitleOverlay by remember { mutableStateOf(true) }
    var overlayText by remember { mutableStateOf("RESTORATION") }
    var overlayColor by remember { mutableStateOf(Color.Yellow) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showGalleryVideo by remember { mutableStateOf(false) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 220f)) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result -> hasPermission = result.values.all { v -> v } }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) logoUri = uri }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri
