package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var rtmpCamera1: RtmpCamera1? = null
    private var hasPermissions by mutableStateOf(false)
    private var isLandscape by mutableStateOf(false)
    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private var onVideoPicked: ((Uri?) -> Unit)? = null
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val camOk = results[Manifest.permission.CAMERA] == true
            val micOk = results[Manifest.permission.RECORD_AUDIO] == true
            hasPermissions = camOk && micOk
        }
    private val logoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            onLogoPicked?.invoke(uri)
        }
    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            onVideoPicked?.invoke(uri)
        }
    private fun askPermissions() {
        val camOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val micOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (camOk && micOk) {
            hasPermissions = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }
    private fun makeCamera(view: SurfaceView): RtmpCamera1 {
        val checker = object : ConnectChecker {
            override fun onConnectionStarted(url: String) {
            }
            override fun onConnectionSuccess() {
            }
            override fun onConnectionFailed(reason: String) {
            }
            override fun onNewBitrate(bitrate: Long) {
            }
            override fun onDisconnect() {
            }
            override fun onAuthError() {
            }
            override fun onAuthSuccess() {
            }
        }
        return RtmpCamera1(view, checker)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        askPermissions()
        setContent {
            JstvScreen(
                hasPermissions = hasPermissions,
                isLandscape = isLandscape,
                onAsk = { askPermissions() },
                onRotate = {
                    isLandscape = !isLandscape
                    requestedOrientation = if (isLandscape) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                },
                onPickLogo = { cb ->
                    onLogoPicked = cb
                    logoPicker.launch("image/*")
                },
                onPickVideo = { cb ->
                    onVideoPicked = cb
                    videoPicker.launch("video/*")
                },
                onToast = { msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                },
                onSetCamera = { view ->
                    rtmpCamera1 = makeCamera(view)
                },
                onPreview = {
                    try {
                        if (rtmpCamera1?.isOnPreview != true) {
                            rtmpCamera1?.prepareVideo()
                            rtmpCamera1?.prepareAudio()
                            rtmpCamera1?.startPreview()
                        }
                    } catch (e: Exception) {
                    }
                },
                onStartStream = { url ->
                    val a = rtmpCamera1?.prepareAudio() ?: false
                    val v = rtmpCamera1?.prepareVideo() ?: false
                    if (a && v) {
                        try {
                            rtmpCamera1?.startPreview()
                        } catch (e: Exception) {
                        }
                        rtmpCamera1?.startStream(url)
                        true
                    } else {
                        false
                    }
                },
                onStop = {
                    try {
                        rtmpCamera1?.stopStream()
                    } catch (e: Exception) {
                    }
                    try {
                        rtmpCamera1?.startPreview()
                    } catch (e:
