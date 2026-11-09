package com.jsomi.broadcaster

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JSTVPro()
        }
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
            ytKey = ytKey,
            fbKey = fbKey,
            jsomiKey = jsomiKey,
            customKey = customKey,
            customUrl = customUrl,
            dest = dest,
            onDest = { newDest: String -> dest = newDest },
            ticker = ticker,
            loopOn = loopOn,
            logoOn = logoOn,
            onSettings = { tab = 1 }
        )
    } else {
        SettingsScreen(
            ytKey = ytKey,
            onYt = { v: String -> ytKey = v },
            fbKey = fbKey,
            onFb = { v: String -> fbKey = v },
            jsomiKey = jsomiKey,
            onJsomi = { v: String -> jsomiKey = v },
            customUrl = customUrl,
            onCustomUrl = { v: String -> customUrl = v },
            customKey = customKey,
            onCustomKey = { v: String -> customKey = v },
            roomCode = roomCode,
            onRoom = { v: String -> roomCode = v },
            quality = quality,
            onQuality = { v: String -> quality = v },
            ticker = ticker,
            onTicker = { v: String -> ticker = v },
            loopOn = loopOn,
            onLoop = { v: Boolean -> loopOn = v },
            logoOn = logoOn,
            onLogo = { v: Boolean -> logoOn = v },
            onBack = { tab = 0 }
        )
    }
}

@Composable
fun CameraView(isFront: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner
