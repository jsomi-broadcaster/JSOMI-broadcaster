package com.jsomi.broadcaster

import android.app.Activity
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { JSTVApp() } }
    }
}

@Composable
fun RealCamera(camera: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, if (camera == "FRONT") CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(ctx))
        previewView
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun JSTVApp() {
    val context = LocalContext.current
    val activity = context as? Activity

    var loggedIn by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    var screen by remember { mutableStateOf("studio") }
    var isLive by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf("REAR") }
    var activeCam by remember { mutableStateOf("CAM 1") }
    var liveSeconds by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf("") }
    var isLandscape by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsTab by remember { mutableStateOf(0) }

    // 1. VIDEO & ENCODER
    var resolution by remember { mutableStateOf("1080p") }
    var fps by remember { mutableStateOf("30fps") }
    var bitrate by remember { mutableStateOf("6000 Kbps") }
    var codec by remember { mutableStateOf("H.264") }
    var encoderType by remember { mutableStateOf("Hardware") }
    var aspect by remember { mutableStateOf("16:9 Horizontal") }
    var layoutMode by remember { mutableStateOf("Full Camera") }

    // 2. AUDIO
    var micOn by remember { mutableStateOf(true) }
    var micSource by remember { mutableStateOf("Phone Mic") }
    var sampleRate by remember { mutableStateOf("48kHz") }
    var channels by remember { mutableStateOf("Stereo") }
    var systemAudioOn by remember { mutableStateOf(false) }
    var noiseSuppression by remember { mutableStateOf(true) }
    var micGain by remember { mutableStateOf(70f) }
    var compressorOn by remember { mutableStateOf(false) }
    var smartMute by remember { mutableStateOf(false) }

    // 3. SCENE
    var showLogoOnVideo by remember { mutableStateOf(true) }
    var brandColor by remember { mutableStateOf("Gold") }
    var phoneNumber by remember { mutableStateOf("+254 700 000 000") }
        var topic = "Sunday Service"
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "JSOMI Broadcaster\n" + topic,
            textAlign = TextAlign.Center
        )
    }
}
