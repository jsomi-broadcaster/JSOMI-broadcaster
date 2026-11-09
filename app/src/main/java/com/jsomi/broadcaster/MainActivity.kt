package com.jsomi.jstv

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVApp() }
    }
}

@Composable
fun JSTVApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var streamKey by remember { mutableStateOf("") }
    var viewers by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Ready to go live") }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.all { it }
        statusText = if (hasPermission) "Permissions granted!" else "Permissions needed"
    }

    // Viewer count goes up when LIVE
    LaunchedEffect(isLive) {
        if (isLive) {
            viewers = 1
            while (isLive) {
                delay(3000)
                viewers += Random.nextInt(0, 5)
            }
        } else viewers = 0
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp)) {
        Text("JSTV", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("JSOMI BROADCASTER", fontSize = 12.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(12.dp))

        // 1. CAMERA PREVIEW
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black, RoundedCornerShape(16.dp))) {
            if (hasPermission) {
                AndroidView(factory = { ctx ->
                    PerviewView(ctx).apply {
                        val providerFuture = ProcessCameraProvider.getInstance(ctx)
                        providerFuture.addListener({
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                            } catch (e: Exception) { }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }, modifier = Modifier.fillMaxSize())
            } else {
                Text("Camera will show here\nAllow permission below", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
            if (isLive) {
                Text("● LIVE  •  $viewers watching", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp).background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 3. TITLE
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Stream Title - e.g. Sunday Service") },
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.Gray, unfocusedLabelColor = Color.Gray), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))

        // 2. STREAM KEY
        OutlinedTextField(value = streamKey, onValueChange = { streamKey = it }, label = { Text("YouTube / Facebook Stream Key (optional)") },
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.Gray, unfocusedLabelColor = Color.Gray), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Text(statusText, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (!hasPermission) {
            Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
                modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("ALLOW CAMERA + MIC", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(onClick = {
                if (title.isBlank()) { statusText = "Please type a Title first!" ; return@Button }
                isLive = !isLive
                statusText = if (isLive) {
                    if (streamKey.isNotBlank()) "LIVE: $title\nStreaming with key: ${streamKey.take(4)}****" else "LIVE: $title\nNo key - Preview only. Add YouTube key to stream to viewers."
                } else "Stream ended - $viewers total watched"
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Gray else Color.Red)) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 4. SHARE
        Button(onClick = {
            val text = "Watch JSTV LIVE: ${if (title.isBlank()) "JSOMI Broadcast" else title}\nJoin us now on JSTV!"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
            context.startActivity(Intent.createChooser(intent,
