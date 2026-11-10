package com.jsomi.broadcaster

import android.Manifest
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.library.rtmp.RtmpCamera1
import java.lang.reflect.Proxy

class MainActivity : ComponentActivity() {
    private var rtmpCamera1: RtmpCamera1? = null
    private var hasPermissions by mutableStateOf(false)
    private var isLandscape by mutableStateOf(false)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val cameraOk = results[Manifest.permission.CAMERA] == true
        val micOk = results[Manifest.permission.RECORD_AUDIO] == true
        hasPermissions = cameraOk && micOk
        if (!hasPermissions) { Toast.makeText(this, "Camera + Mic needed to go LIVE.", Toast.LENGTH_LONG).show() }
    }
    private fun checkAndRequestPermissions() {
        val cameraOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (cameraOk && micOk) { hasPermissions = true } else { hasPermissions = false; permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }
    }
    private fun makeRtmpCamera(view: SurfaceView): RtmpCamera1 {
        val ctor = RtmpCamera1::class.java.constructors.first { it.parameterCount == 2 && it.parameterTypes[0] == SurfaceView::class.java }
        val checkerInterface = ctor.parameterTypes[1]
        val dummyChecker = Proxy.newProxyInstance(checkerInterface.classLoader, arrayOf(checkerInterface)) { _, _, _ -> null }
        return ctor.newInstance(view, dummyChecker) as RtmpCamera1
    }
    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onLogoPicked?.invoke(uri) }
    private var onVideoPicked: ((Uri?) -> Unit)? = null
    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onVideoPicked?.invoke(uri) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            JstvProScreen(
                hasPermissions = hasPermissions,
                isLandscape = isLandscape,
                onRequestPermissions = { checkAndRequestPermissions() },
                onToggleRotate = {
                    isLandscape = !isLandscape
                    requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                },
                onPickLogo = { callback -> onLogoPicked = callback; logoPicker.launch("image/*") },
                onPickVideo = { callback -> onVideoPicked = callback; videoPicker.launch("video/*") },
                onToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() },
                onSetCamera = { view -> rtmpCamera1 = makeRtmpCamera(view) },
                onStartPreview = { try { if (rtmpCamera1?.isOnPreview != true) { rtmpCamera1?.prepareVideo(); rtmpCamera1?.prepareAudio(); rtmpCamera1?.startPreview() } } catch (e: Exception) { } },
                onStartStream = { fullUrl ->
                    val okAudio = rtmpCamera1?.prepareAudio() ?: false
                    val okVideo = rtmpCamera1?.prepareVideo() ?: false
                    if (okAudio && okVideo) { try { rtmpCamera1?.startPreview() } catch (e: Exception) { }; rtmpCamera1?.startStream(fullUrl); true } else { false }
                },
                onSwitchUrl = { fullUrl -> try { rtmpCamera1?.stopStream(); rtmpCamera1?.startStream(fullUrl); true } catch (e: Exception) { false } },
                onStopStream = { try { rtmpCamera1?.stopStream() } catch (e: Exception) { }; try { rtmpCamera1?.startPreview() } catch (e: Exception) { } },
                onSwitchCamera = { try { rtmpCamera1?.switchCamera() } catch (e: Exception) { } },
                onMute = { mute -> if (mute) rtmpCamera1?.disableAudio() else rtmpCamera1?.enableAudio() }
            )
        }
    }
    override fun onDestroy() { try { rtmpCamera1?.stopStream(); rtmpCamera1?.stopPreview() } catch (e: Exception) { }; super.onDestroy() }
}

// COMMIT 8: Each title has its own id + text + position, so it moves alone
data class TitleItem(val id: Int, val text: String, val offset: Offset)

@Composable
fun JstvProScreen(
    hasPermissions: Boolean, isLandscape: Boolean, onRequestPermissions: () -> Unit, onToggleRotate: () -> Unit,
    onPickLogo: ((Uri?) -> Unit) -> Unit, onPickVideo: ((Uri?) -> Unit) -> Unit, onToast: (String) -> Unit,
    onSetCamera: (SurfaceView) -> Unit, onStartPreview: () -> Unit, onStartStream: (String) -> Boolean,
    onSwitchUrl: (String) -> Boolean, onStopStream: () -> Unit, onSwitchCamera: () -> Unit, onMute: (Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf("BROADCAST") }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live - Breaking News - ") }
    var appMode by remember { mutableStateOf("Presenter") }
    var camIndex by remember { mutableStateOf(1) }
    var remoteCommand by remember { mutableStateOf("Waiting for Director...") }
    var directorJoined by remember { mutableStateOf(false) }
    var liveTarget by remember { mutableStateOf("Facebook") }
    var useYoutube by remember { mutableStateOf(true) }
    var useFacebook by remember { mutableStateOf(true) }
    var useJsomi by remember { mutableStateOf(false) }
    var useCustom by remember { mutableStateOf(false) }
    var statusYoutube by remember { mutableStateOf("OFF") }
    var statusFacebook by remember { mutableStateOf("OFF") }
    var statusJsomi by remember { mutableStateOf("OFF") }
    var statusCustom by remember { mutableStateOf("OFF") }
    var liveStatus by remember { mutableStateOf("READY") }
    var isStreaming by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var currentDestIndex by remember { mutableStateOf(-1) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var logoSize by remember { mutableStateOf(80f) }
    // COMMIT 8: Titles move separately - each has own offset
    var titlesList by remember { mutableStateOf(listOf(TitleItem(1, "BREAKING", Offset(20f, 200f)), TitleItem(2, "ENQUIRY", Offset(20f, 260f)), TitleItem(3, "RESTORATION", Offset(20f, 320f)))) }
    var nextTitleId by remember { mutableStateOf(4) }
    var newTitleText by remember { mutableStateOf("") }
    var showTitle by remember { mutableStateOf(true) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }
    // COMMIT 8: Moving text + Loop
    var tickerMoving by remember { mutableStateOf(true) }
    var tickerLoop by remember { mutableStateOf(true) }
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isPlayingGallery by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermissions) { if (hasPermissions) { onStartPreview() } }

    // Scrolling ticker - loops forever
    val infinite = rememberInfiniteTransition(label = "ticker")
    val tickerX by infinite.animateFloat(initialValue = 400f, targetValue = -800f, animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "tickerX")

    fun destFor(name: String): Triple<String, String, String> {
        return when (name) {
            "YouTube" -> Triple("YouTube", "rtmp://a.rtmp.youtube.com/live2/" + youtubeKey, youtubeKey)
            "Facebook" -> Triple("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/" + facebookKey, facebookKey)
            "JSOMI" -> Triple("JSOMI", "rtmp://jsomi.tv/live/" + jsomiKey, jsomiKey)
            else -> Triple("Custom", if (customUrl.isBlank()) "" else customUrl + "/" + customKey, customKey)
        }
    }
    fun selectedDests(): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        if (useYoutube) { list.add(destFor("YouTube")) }
        if (useFacebook) { list.add(destFor("Facebook")) }
        if (useJsomi) { list.add(destFor("JSOMI")) }
        if (useCustom) { list.add(destFor("Custom")) }
        return list
    }
    fun setStatus(name: String, status: String) {
        when (name) { "YouTube" -> statusYoutube = status; "Facebook" -> statusFacebook = status; "JSOMI" -> statusJsomi = status; "Custom" -> statusCustom = status }
    }
    fun statusColor(s: String): Color { return when { s.startsWith("LIVE") -> Color.Green; s.startsWith("FAILED") -> Color.Red; else -> Color.Black } }

    fun startLive() {
        if (!hasPermissions) { onToast("Grant Camera + Mic first"); onRequestPermissions() } else {
            val dest = destFor(liveTarget)
            if (dest.third.isBlank()) {
                setStatus(dest.first, "FAILED - Enter Key"); liveStatus = "FAILED - Enter Key for ${dest.first}"; onToast("Enter ${dest.first} key in SETUP first"); currentTab = "SETUP"
            } else {
                liveStatus = "CONNECTING to ${dest.first}..."
                val started = onStartStream(dest.second)
                if (started) {
                    val dests = selectedDests()
                    currentDestIndex = dests.indexOfFirst { it.first == dest.first }.coerceAtLeast(0)
                    isStreaming = true; setStatus(dest.first, "LIVE ✅"); liveStatus = "LIVE to ${dest.first}"; onToast("LIVE to ${dest.first} - Check ${dest.first} app")
                } else { setStatus(dest.first, "FAILED - Check Key"); liveStatus = "FAILED - Check Key for ${dest.first}"; onToast("FAILED - Check key for ${dest.first}. Facebook: use Stream Key from Facebook > Live Producer, not Page password.") }
            }
        }
    }
    fun endLive() {
        val endedTo = liveTarget
        onStopStream(); isStreaming = false; currentDestIndex = -1; liveStatus = "ENDED - Saved to $endedTo ✅"; statusYoutube = "OFF"; statusFacebook = "OFF"; statusJsomi = "OFF"; statusCustom = "OFF"
        onToast("ENDED - Saved to $endedTo. Open $endedTo app > Your Videos to see it.")
    }
    fun directorAction(action
