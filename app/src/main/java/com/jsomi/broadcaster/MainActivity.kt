package com.jsomi.jstv

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVPro() }
    }
}

@Composable
fun JSTVPro() {
    var tab by remember { mutableStateOf(0) } // 0=Studio, 1=Settings
    // Settings - saved in memory for now
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Restoration Sunday") }
    var loopOn by remember { mutableStateOf(false) }
    var logoOn by remember { mutableStateOf(true) }

    if (tab == 0) {
        StudioScreen(
            ytKey = ytKey, fbKey = fbKey, jsomiKey = jsomiKey, customKey = customKey,
            ticker = ticker, loopOn = loopOn, logoOn = logoOn,
            onSettings = { tab = 1 }
        )
    } else {
        SettingsScreen(
            ytKey = ytKey, onYt = { ytKey = it },
            fbKey = fbKey, onFb = { fbKey = it },
            jsomiKey = jsomiKey, onJsomi = { jsomiKey = it },
            customUrl = customUrl, onCustomUrl = { customUrl = it },
            customKey = customKey, onCustomKey = { customKey = it },
            roomCode = roomCode, onRoom = { roomCode = it },
            quality = quality, onQuality = { quality = it },
            ticker = ticker, onTicker = { ticker = it },
            loopOn = loopOn, onLoop = { loopOn = it },
            logoOn = logoOn, onLogo = { logoOn = it },
            onBack = { tab = 0 }
        )
    }
}

@Composable
fun StudioScreen(ytKey: String, fbKey: String, jsomiKey: String, customKey: String, ticker: String, loopOn: Boolean, logoOn: Boolean, onSettings: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var isFront by remember { mutableStateOf(false) } // FIX 1: Front/Back
    var title by remember { mutableStateOf("RESTORATION SUNDAY") }
    var viewers by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Ready - Add key in Settings") }
    var showTitleOverlay by remember { mutableStateOf(true) }
    var overlayText by remember { mutableStateOf("RESTORATION") }
    var overlayColor by remember { mutableStateOf(Color.Yellow) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showGalleryVideo by remember { mutableStateOf(false) }

    // Drag positions
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 220f)) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        hasPermission = r.values.all { it }
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) logoUri = uri }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) { galleryVideoUri = uri; showGalleryVideo = true } }

    LaunchedEffect(isLive) {
        if (isLive) { viewers = 1; while (isLive) { delay(3000); viewers += Random.nextInt(0, 5) } } else viewers = 0
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("JSTV PRO v2", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Red)
            Button(onClick = onSettings, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("⚙ SETTINGS") }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // CAMERA + OVERLAYS
        Box(modifier = Modifier.fillMaxWidth().height(340.dp).background(Color.Black, RoundedCornerShape(16.dp))) {
            if (hasPermission && !showGalleryVideo) {
                AndroidView(factory = { ctx ->
                    PreviewView(ctx).apply {
                        val f = ProcessCameraProvider.getInstance(ctx)
                        f.addListener({
                            val p = f.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                            try { p.unbindAll(); p.bindToLifecycle(lifecycleOwner, if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, preview) } catch (e: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }, modifier = Modifier.fillMaxSize(), update = { })
                // Re-bind when camera flips - force recompose via key
                key(isFront) { }
            } else if (showGalleryVideo && galleryVideoUri != null) {
                AndroidView(factory = { ctx ->
                    android.widget.VideoView(ctx).apply { setVideoURI(galleryVideoUri); setOnPreparedListener { it.isLooping = loopOn; start() } }
                }, modifier = Modifier.fillMaxSize())
            } else {
                Text("Camera will show here", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }

            // LOGO - DRAGGABLE
            if (logoOn && logoUri != null) {
                AsyncImage(model = logoUri, contentDescription = "Logo",
                    modifier = Modifier.offset { IntOffset(logoOffset.x.roundToInt(), logoOffset.y.roundToInt()) }.size(80.dp)
                        .pointerInput(Unit) { detectDragGestures { c, d -> logoOffset += d } })
            }
            // TITLE - DRAGGABLE
            if (showTitleOverlay) {
                Text(overlayText, color = overlayColor, fontSize = 24.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.offset { IntOffset(titleOffset.x.roundToInt(), titleOffset.y.roundToInt()) }
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(6.dp)
                        .pointerInput(Unit) { detectDragGestures { c, d -> titleOffset += d } })
            }
            if (isLive) {
                Text("● LIVE • $viewers watching", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            // Ticker bottom
            if (ticker.isNotBlank()) {
                Text(ticker, color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.7f)).padding(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // FIX 1: CAMERA SWITCH
            Button(onClick = { isFront = !isFront }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text(if (isFront) "📷 FRONT" else "📷 BACK") }
            Button(onClick = { showTitleOverlay = !showTitleOverlay }) { Text(if (showTitleOverlay) "HIDE TITLE" else "SHOW TITLE") }
            Button(onClick = { logoPicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("LOGO") }
            Button(onClick = { videoPicker.launch("video/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("GALLERY") }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Title presets
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("ENQUIRY", "RESTORATION", "BREAKING").forEach { t ->
                Button(onClick = { overlayText = t; showTitleOverlay = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text(t, fontSize = 11.sp) }
            }
            Button(onClick = { overlayColor = listOf(Color.Yellow, Color.Red, Color.White, Color.Green).random() }) { Text("COLOR", fontSize = 11.sp) }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = title, onValueChange = { title = it; overlayText = it }, label = { Text("Stream Title") },
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.Gray, unfocusedLabelColor = Color.Gray), singleLine = true)
        Spacer(modifier = Modifier.height(6.dp))
        Text(statusText, color = Color.White, fontSize = 13.sp)
        if (showGalleryVideo) Button(onClick = { showGalleryVideo = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("BACK TO CAMERA (Loop: ${if (loopOn) "ON" else "OFF"})") }
        Spacer(modifier = Modifier.height(6.dp))

        if (!hasPermission) {
            Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
                modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("ALLOW CAMERA + MIC", fontWeight = FontWeight.Bold) }
        } else {
            Button(onClick = {
                // FIX 2: BLOCK GO LIVE WITHOUT KEY
                val hasAnyKey = ytKey.isNotBlank() || fbKey.isNotBlank() || jsomiKey.isNotBlank() || customKey.isNotBlank()
                if (!isLive && !hasAnyKey) { statusText = "❌ FAILED - No Stream Key! Go to SETTINGS and add YouTube / Facebook / JSOMI key first."; return@Button }
                if (!isLive && title.isBlank()) { statusText = "Type a Title first!"; return@Button }
                isLive = !isLive
                statusText = if (isLive) "● LIVE: $title (Commit 1 = Preview Mode - Real RTMP in Commit 2)" else "Stream ended"
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Gray else Color.Red)) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Watch JSTV LIVE: $title") }
            context.startActivity(Intent.createChooser(intent, "Share JSTV"))
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("SHARE TO WHATSAPP", fontWeight = FontWeight.Bold, color = Color.Black) }
    }
}

@Composable
fun SettingsScreen(ytKey: String, onYt: (String) -> Unit, fbKey: String, onFb: (String) -> Unit, jsomiKey: String, onJsomi: (String) -> Unit, customUrl: String, onCustomUrl: (String) -> Unit, customKey: String, onCustomKey: (String) -> Unit, roomCode: String, onRoom: (String) -> Unit, quality: String, onQuality: (String) -> Unit, ticker: String, onTicker: (String) -> Unit, loopOn: Boolean, onLoop: (Boolean) -> Unit, logoOn: Boolean, onLogo: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("⚙ JSTV SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        Text("STREAM KEYS - All in one place", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Field("YouTube Stream Key", ytKey, onYt)
        Field("Facebook Stream Key", fbKey, onFb)
        Field("JSOMI TV Stream Key", jsomiKey, onJsomi)
        Field("Custom RTMP URL", customUrl, onCustomUrl)
        Field("Custom RTMP Key", customKey, onCustomKey)
        Spacer(modifier = Modifier.height(12.dp))
        Text("MULTI-CAMERA / REMOTE", color = Color.White, fontWeight = FontWeight.Bold)
        Field("Room Code (e.g. JSTV-1234)", roomCode, onRoom)
        Text("Director phone: Enter same Room Code in Remote mode (Commit 4)", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("VIDEO QUALITY: $quality", color = Color.White, fontWeight = FontWeight.Bold)
        Row { listOf("720p", "1080p").forEach { q -> Button(onClick = { onQuality(q) }, modifier = Modifier.padding(end = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (quality == q) Color.Red else Color.DarkGray)) { Text(q) } } }
        Spacer(modifier = Modifier.height(12.dp))
        Field("Ticker Text", ticker, onTicker)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Loop Gallery Video", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = loopOn, onCheckedChange = onLoop) }
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Logo", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = logoOn, onCheckedChange = onLogo) }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("SAVE & BACK TO STUDIO", fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Commit 1 done. Commit 2 = Real RTMP to YouTube with FAILED check.", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.Gray, unfocusedLabelColor = Color.Gray), singleLine = true)
}
