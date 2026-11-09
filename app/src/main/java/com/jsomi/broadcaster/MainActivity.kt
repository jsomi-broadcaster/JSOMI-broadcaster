package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVApp() }
    }
}

@Composable
fun JSTVApp() {
    var showWelcome by remember { mutableStateOf(true) }
    var streamerName by remember { mutableStateOf("JSOMI TV") }
    var topic by remember { mutableStateOf("Sunday Service") }
    var tickerText by remember { mutableStateOf("Welcome to JSOMI TV • Jesus Saves • Mombasa • ") }
    var destination by remember { mutableStateOf("YouTube") }
    var streamKey by remember { mutableStateOf("") }
    var isLive by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // PERMISSION - asks on open
    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (showWelcome) {
        // ===== WELCOME SCREEN =====
        Column(Modifier.fillMaxSize().background(Color.Black).padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(30.dp))
            Text("Welcome to JSTV", color = Color(0xFFD4AF37), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("JSOMI TV - Live Broadcaster", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = streamerName, onValueChange = { streamerName = it }, label = { Text("Your Name / Church") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic - e.g. Sunday Service") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Scrolling Text (Ticker)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Spacer(Modifier.height(16.dp))
            Text("Where to Stream?", color = Color.White, fontWeight = FontWeight.Bold)
            Row { listOf("YouTube", "Facebook", "Custom RTMP").forEach { d ->
                FilterChip(selected = destination == d, onClick = { destination = d }, label = { Text(d) }, modifier = Modifier.padding(4.dp))
            } }
            OutlinedTextField(value = streamKey, onValueChange = { streamKey = it }, label = { Text("Stream Key (paste from $destination)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Spacer(Modifier.height(8.dp))
            Text(if (hasCameraPermission) "✅ Camera Permission Granted" else "⏳ Tap Allow on camera popup - if no popup, go to Phone Settings > Apps > JSOMI > Camera > Allow", color = if (hasCameraPermission) Color.Green else Color.Yellow, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { showWelcome = false }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))) {
                Text("START STUDIO →", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // ===== STUDIO SCREEN - CLEAN =====
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("JSTV", color = Color(0xFFD4AF37), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(if (isLive) "● LIVE • $destination" else "OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontWeight = FontWeight.Bold)
            }
            // CAMERA INSIDE - no boxes inside
            Box(Modifier.weight(1f).fillMaxWidth().padding(8.dp).background(Color.DarkGray)) {
                if (hasCameraPermission) {
                    RealCameraPreview(isFront = isFrontCamera)
                } else {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 48.sp); Text("Camera needs permission", color = Color.White)
                        Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("Allow Camera") }
                    }
                }
                if (isLive) Text("● LIVE", color = Color.White, modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Red).padding(4.dp))
                Text(topic, color = Color.White, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).background(Color.Black.copy(0.6f)).padding(6.dp))
            }
            // SCROLLING TICKER
            ScrollingTicker(text = tickerText)
            // GO LIVE
            Button(onClick = { isLive = !isLive }, modifier = Modifier.fillMaxWidth().padding(12.dp).height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Gray else Color.Red)) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE to $destination", fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isFrontCamera = !isFrontCamera }) { Text("Switch Camera") }
                Button(onClick = { isMuted = !isMuted }) { Text(if (isMuted) "Unmute" else "Mute") }
                Button(onClick = { showSettings = true }) { Text("Settings") }
                Button(onClick = { showWelcome = true }) { Text("Back") }
            }
            Spacer(Modifier.height(10.dp))
        }
        // SETTINGS SHEET
        if (showSettings) {
            AlertDialog(onDismissRequest = { showSettings = false }, title = { Text("Settings") },
                text = {
                    Column {
                        Text("Stream Destination: $destination")
                        Row { listOf("YouTube", "Facebook", "Custom RTMP").forEach { d ->
                            FilterChip(selected = destination == d, onClick = { destination = d }, label = { Text(d) }, modifier = Modifier.padding(2.dp))
                        } }
                        OutlinedTextField(value = streamKey, onValueChange = { streamKey = it }, label = { Text("Stream Key") }, singleLine = true)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Scrolling Text") })
                        Spacer(Modifier.height(8.dp))
                        Text("Camera: ${if (isFrontCamera) "Front" else "Back"} - use Switch Camera to change")
                        Text("Sources: Camera + Mic (Audio ${if (isMuted) "Muted" else "On"})")
                        Text("Quality: 720p HD")
                    }
                },
                confirmButton = { Button(onClick = { showSettings = false }) { Text("Done") } })
        }
    }
}

@Composable
fun RealCameraPreview(isFront: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = { ctx ->
        PreviewView(ctx).apply {
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                val selector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                try { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, selector, preview) } catch (e: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))
        }
    }, update = { view ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            val selector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            try { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, selector, preview) } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun ScrollingTicker(text: String) {
    var offset by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            offset -= 3f
            if (offset < -1200f) offset = 0f
            kotlinx.coroutines.delay(16)
        }
    }
    Box(Modifier.fillMaxWidth().height(32.dp).background(Color(0xFFD4AF37))) {
        Text(text.repeat(4), color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = offset.dp).padding(4.dp), maxLines = 1)
    }
}
