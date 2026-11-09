package com.pedro.streamingapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.SurfaceView
import com.pedro.library.rtmp.ConnectCheckerRtmp
import com.pedro.rtmp.RtmpCamera1

class MainActivity : ComponentActivity(), ConnectCheckerRtmp {

    private var rtmpCamera1: RtmpCamera1? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveScreen()
        }
    }

    @Composable
    fun LiveScreen() {
        val context = LocalContext.current
        var url by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/") }
        var streamKey by remember { mutableStateOf("") }
        var isStreaming by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).also { view ->
                        rtmpCamera1 = RtmpCamera1(view, this@MainActivity)
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("RTMP URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = streamKey,
                onValueChange = { streamKey = it },
                label = { Text("Stream Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    if (!isStreaming) {
                        rtmpCamera1?.startPreview()
                        rtmpCamera1?.startStream(url + streamKey)
                        isStreaming = true
                    }
                }) {
                    Text("Start Stream")
                }
                Button(onClick = {
                    if (isStreaming) {
                        rtmpCamera1?.stopStream()
                        rtmpCamera1?.stopPreview()
                        isStreaming = false
                    }
                }) {
                    Text("Stop Stream")
                }
            }
        }
    }

    override fun onConnectionStarted(url: String) {
        runOnUiThread { Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionSuccess() {
        runOnUiThread { Toast.makeText(this, "Connected - LIVE!", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailed(reason: String) {
        runOnUiThread { Toast.makeText(this, "Failed: $reason", Toast.LENGTH_LONG).show() }
    }

    override fun onNewBitrate(bitrate: Long) {}

    override fun onDisconnect() {
        runOnUiThread { Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show() }
    }

    override fun onAuthError() {
        runOnUiThread { Toast.makeText(this, "Auth Error - Check Key", Toast.LENGTH_LONG).show() }
    }

    override fun onAuthSuccess() {}
}
