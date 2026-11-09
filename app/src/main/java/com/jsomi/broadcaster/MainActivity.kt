package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.rtmp.RtmpCamera1

class MainActivity : ComponentActivity(), ConnectChecker {
    private var rtmpCamera: RtmpCamera1? = null
    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LiveScreen() }
    }

    @Composable
    fun LiveScreen() {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            )
        }
        var streamKey by remember { mutableStateOf("") }
        var isLive by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf("READY - Allow Camera + Mic") }
        var useFront by remember { mutableStateOf(true) }

        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            hasPermission = perms[Manifest.permission.CAMERA] == true && perms[Manifest.permission.RECORD_AUDIO] == true
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }

        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
                Text("JSTV - JSOMI BROADCASTER", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(status, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp).background(Color.DarkGray)) {
                    AndroidView(factory = { ctx ->
                        SurfaceView(ctx).also { sv ->
                            surfaceView = sv
                            sv.holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(h: SurfaceHolder) {
                                    if (rtmpCamera == null) {
                                        rtmpCamera = RtmpCamera1(sv, this@MainActivity)
                                    }
                                    if (hasPermission) {
                                        rtmpCamera?.startPreview(if (useFront) com.pedro.common.CameraFacing.FRONT else com.pedro.common.CameraFacing.BACK)
                                    }
                                }
                                override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hh: Int) {}
                                override fun surfaceDestroyed(h: SurfaceHolder) {
                                    rtmpCamera?.stopPreview()
                                }
                            })
                        }
                    }, modifier = Modifier.fillMaxSize())
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = {
                        useFront = !useFront
                        rtmpCamera?.switchCamera()
                    }) { Text(if (useFront) "FRONT CAM" else "BACK CAM") }
                    Text(if (isLive) "● LIVE" else "○ OFFLINE", color = if (isLive) Color.Red else Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = streamKey, onValueChange = { streamKey = it },
                    label = { Text("YouTube Stream Key", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (!hasPermission) { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)); return@Button }
                        if (streamKey.isBlank()) { status = "Enter Stream Key first!"; return@Button }
                        if (!isLive) {
                            val url = "rtmp://a.rtmp.youtube.com/live2/$streamKey"
                            if (rtmpCamera?.isStreaming == true) rtmpCamera?.stopStream()
                            rtmpCamera?.startStream(url)
                            isLive = true; status = "CONNECTING TO YOUTUBE..."
                        } else {
                            rtmpCamera?.stopStream(); isLive = false; status = "STOPPED"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFFD60000))
                ) { Text(if (isLive) "STOP LIVE" else "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }

    override fun onConnectionStarted(url: String) { runOnUiThread { Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show() } }
    override fun onConnectionSuccess() { runOnUiThread { Toast.makeText(this, "LIVE ON YOUTUBE!", Toast.LENGTH_LONG).show() } }
    override fun onConnectionFailed(reason: String) { runOnUiThread { Toast.makeText(this, "Failed: $reason", Toast.LENGTH_LONG).show() } }
    override fun onNewBitrate(bitrate: Long) {}
    override fun onDisconnect() { runOnUiThread { Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show() } }
    override fun onAuthError() { runOnUiThread { Toast.makeText(this, "Auth Error - Check Key", Toast.LENGTH_LONG).show() } }
    override fun onAuthSuccess() {}
}
