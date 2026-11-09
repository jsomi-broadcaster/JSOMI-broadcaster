package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
        setContent { BroadcasterScreen() }
    }
}

@Composable
fun BroadcasterScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var streamKey by remember { mutableStateOf("") }
    var isLive by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("READY - Grant Camera + Mic") }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermission = perms[Manifest.permission.CAMERA] == true && perms[Manifest.permission.RECORD_AUDIO] == true
        status = if (hasPermission) "READY - Enter Stream Key" else "Permission Denied - Allow in Settings"
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
            Text("JSTV - JSOMI BROADCASTER", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(status, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp).background(Color.DarkGray)) {
                if (hasPermission) {
                    AndroidView(factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                            } catch (e: Exception) { }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }, modifier = Modifier.fillMaxSize())
                } else {
                    Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, modifier = Modifier.align(Alignment.Center)) {
                        Text("ALLOW CAMERA + MIC")
                    }
                }
            }

            OutlinedTextField(
                value = streamKey,
                onValueChange = { streamKey = it },
                label = { Text("YouTube / Facebook Stream Key", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!hasPermission) {
                        launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    } else if (streamKey.isBlank()) {
                        status = "Enter Stream Key first!"
                    } else {
                        isLive = !isLive
                        status = if (isLive) "LIVE - Streaming..." else "STOPPED"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFFD60000))
            ) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
