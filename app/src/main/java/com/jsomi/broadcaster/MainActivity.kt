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
import androidx.compose.foundation.horizontalScroll
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

// MOVING TICKER - scrolls forever
@Composable
fun MovingTicker(text: String, modifier: Modifier = Modifier) {
    var x by remember { mutableStateOf(800f) }
    LaunchedEffect(text) {
        while (true) {
            delay(30)
            x -= 4f
            if (x < -800f) x = 800f
        }
    }
    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.75f)).height(28.dp)) {
        Text(text, color = Color.White, fontSize = 13.sp, maxLines = 1,
            modifier = Modifier.offset { IntOffset(x.roundToInt(), 4) }.align(Alignment.CenterStart))
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
    var dest by remember { mutableStateOf("YouTube") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Restoration Sunday - God is Good - ") }
    var loopOn by remember { mutableStateOf(false) }
    var logoOn by remember { mutableStateOf(true) }

    if (tab == 0) {
        StudioScreen(ytKey, fbKey, jsomiKey, customUrl, customKey, dest, { dest = it }, ticker, loopOn, logoOn, { tab = 1 })
    } else {
        SettingsScreen(ytKey, { ytKey = it }, fbKey, { fbKey = it }, jsomiKey, { jsomiKey = it }, customUrl, { customUrl = it }, customKey, { customKey = it }, roomCode, { roomCode = it }, quality, { quality = it }, ticker, { ticker = it }, loopOn, { loopOn = it }, logoOn, { logoOn = it }, { tab = 0 })
    }
}

@Composable
fun StudioScreen(ytKey: String, fbKey: String, jsomiKey: String, customUrl: String, customKey: String, dest: String, onDest: (String) -> Unit, ticker: String, loopOn: Boolean, logoOn: Boolean, onSettings: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(false) }
   
