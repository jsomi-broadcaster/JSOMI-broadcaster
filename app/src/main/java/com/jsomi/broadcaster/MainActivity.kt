package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.pedro.library.base.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1

class MainActivity : ComponentActivity() {

    private var rtmpCamera1: RtmpCamera1? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            Toast.makeText(this, "Camera + Mic permission needed to stream", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ask for Camera + Mic on first open
        val needed = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed)
        }

        setContent {
            LiveScreen()
        }
    }

    @Composable
    fun LiveScreen() {
        var url by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/") }
        var streamKey by remember { mutableStateOf("") }
        var isStreaming by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(false) }
        var isFrontCamera by remember { mutableStateOf(false) }
        var isFlashOn by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf("Ready") }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // Status text
            Text(text = "Status: $status")
            Spacer(modifier = Modifier.height(8.dp))

            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).also { view ->
                        rtmpCamera1 = RtmpCamera1(view, object : ConnectChecker {})
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // RTMP URL
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("RTMP URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stream Key
            OutlinedTextField(
                value = streamKey,
                onValueChange = { streamKey = it },
                label = { Text("Stream Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // START / STOP
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (!isStreaming) {
                            if (streamKey.isBlank()) {
                                Toast.makeText(this@MainActivity, "Enter Stream Key first", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (rtmpCamera1?.prepareAudio() == true && rtmpCamera1?.prepareVideo() == true) {
                                rtmpCamera1?.startPreview()
                                rtmpCamera1?.startStream(url + streamKey)
                                isStreaming = true
                                status = "LIVE - Streaming..."
                                Toast.makeText(this@MainActivity, "Streaming...", Toast.LENGTH_SHORT).show()
                            } else {
                                status = "Encoder failed"
                                Toast.makeText(this@MainActivity, "Encoder failed to start", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isStreaming
                ) {
                    Text("Start Stream")
                }
                Button(
                    onClick = {
                        if (isStreaming) {
                            rtmpCamera1?.stopStream()
                            rtmpCamera1?.stopPreview()
                            isStreaming = false
                            status = "Stopped"
                            Toast.makeText(this@MainActivity, "Stopped", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isStreaming
                ) {
                    Text("Stop Stream")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTROLS: Camera / Flash / Mute
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    try {
                        rtmpCamera1?.switchCamera()
                        isFrontCamera = !isFrontCamera
                        Toast.makeText(this@MainActivity, if (isFrontCamera) "Front Camera" else "Back Camera", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Cannot switch camera", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(if (isFrontCamera) "Front Cam" else "Back Cam")
                }
                Button(onClick = {
                    try {
                        if (isFlashOn) {
                            rtmpCamera1?.turnOffLantern()
                            isFlashOn = false
                        } else {
                            rtmpCamera1?.turnOnLantern()
                            isFlashOn = true
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Flash not available", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(if (isFlashOn) "Flash ON" else "Flash OFF")
                }
                Button(onClick = {
                    try {
                        if (isMuted) {
                            rtmpCamera1?.enableAudio()
                            isMuted = false
                        } else {
                            rtmpCamera1?.disableAudio()
                            isMuted = true
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Cannot mute", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(if (isMuted) "Unmute" else "Mute")
                }
            }
        }
    }
}
