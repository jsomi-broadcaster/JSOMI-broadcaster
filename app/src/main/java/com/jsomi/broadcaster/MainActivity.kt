package com.jsomi.broadcaster

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        setContent {
            JSTVApp()
        }
    }
}

@Composable
fun JSTVApp() {
    var tab by remember { mutableStateOf(0) }
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2/") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Restoration Sunday - God Bless You") }
    var loopOn by remember { mutableStateOf(false) }
    var logoOn by remember { mutableStateOf(true) }
    var dest by remember { mutableStateOf("YouTube") }

    if (tab == 0) {
        StudioScreen(
            ytKey = ytKey,
            fbKey = fbKey,
            jsomiKey = jsomiKey,
            customKey = customKey,
            ticker = ticker,
            loopOn = loopOn,
            logoOn = logoOn,
            dest = dest,
            onDest = { v: String -> dest = v },
            onSettings = { tab = 1 }
        )
    } else {
        SettingsScreen(
            ytKey = ytKey,
            onYt = { v: String -> ytKey = v },
            fbKey = fbKey,
            onFb = { v: String -> fbKey = v },
            jsomiKey = jsomiKey,
            onJsomi = { v: String -> jsomiKey = v },
            customUrl = customUrl,
            onCustomUrl = { v: String -> customUrl = v },
            customKey = customKey,
            onCustomKey = { v: String -> customKey = v },
            roomCode = roomCode,
            onRoom = { v: String -> roomCode = v },
            quality = quality,
            onQuality = { v: String -> quality = v },
            ticker = ticker,
            onTicker = { v: String -> ticker = v },
            loopOn = loopOn,
            onLoop = { v: Boolean -> loopOn = v },
            logoOn = logoOn,
            onLogo = { v: Boolean -> logoOn = v },
            dest = dest,
            onDest = { v: String -> dest = v },
            onBack = { tab = 0 }
        )
    }
}

@Composable
fun CameraPreview(isFront: Boolean, modifier: Modifier) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    key(isFront) {
        AndroidView(
            factory = { c: Context ->
                val pv = PreviewView(c)
                val future = ProcessCameraProvider.getInstance(c)
                future.addListener(
                    {
                        val provider = future.get()
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(pv.surfaceProvider)
                        val facing = if (isFront) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                        val selector = CameraSelector.Builder().requireLensFacing(facing).build()
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(owner, selector, preview)
                        } catch (e: Exception) {
                            // camera busy, will retry on next flip
                        }
                    },
                    ContextCompat.getMainExecutor(c)
                )
                pv
            },
            modifier = modifier
        )
    }
}

@Composable
fun MovingTicker(text: String, modifier: Modifier) {
    var x by remember { mutableStateOf(400f) }
    LaunchedEffect(text) {
        while (true) {
            delay(20L)
            x = x - 3f
            if (x < -1200f) {
                x = 400f
            }
        }
    }
    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.75f))) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.offset { IntOffset(x.roundToInt(), 0) }.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun StudioScreen(
    ytKey: String,
    fbKey: String,
    jsomiKey: String,
    customKey: String,
    ticker: String,
    loopOn: Boolean,
    logoOn: Boolean,
    dest: String,
    onDest: (String) -> Unit,
    onSettings: () -> Unit
) {
    val ctx = LocalContext.current
    val act = ctx as? Activity
    var hasPermission by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var isFront by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("RESTORATION SUNDAY") }
    var viewers by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Ready - Add key in Settings") }
    var showTitleOverlay by remember { mutableStateOf(true) }
    var overlayText by remember { mutableStateOf("RESTORATION") }
    var overlayColor by remember { mutableStateOf(Color.Yellow) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    var showGallery by remember { mutableStateOf(false) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 220f)) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        var allOk = true
        for (entry in result.values) {
            if (entry == false) {
                allOk = false
            }
        }
        hasPermission = allOk
    }
    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri
        }
    }
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            galleryUri = uri
            showGallery = true
        }
    }

    LaunchedEffect(isLive) {
        if (isLive) {
            viewers = 1
            while (isLive) {
                delay(3000L)
                viewers = viewers + Random.nextInt(0, 5)
            }
        } else {
            viewers = 0
        }
    }

    fun toggleOrientation() {
        isLandscape = !isLandscape
        if (act != null) {
            if (isLandscape) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        }
    }

    fun activeKey(): String {
        if (dest == "YouTube") {
            return ytKey
        }
        if (dest == "Facebook") {
            return fbKey
        }
        if (dest == "JSOMI") {
            return jsomiKey
        }
        return customKey
    }

    fun activeUrl(): String {
        if (dest == "YouTube") {
            return "rtmp://a.rtmp.youtube.com/live2/"
        }
        if (dest == "Facebook") {
            return "rtmps://live-api-s.facebook.com:443/rtmp/"
        }
        if (dest == "JSOMI") {
            return "rtmp://a.rtmp.youtube.com/live2/"
        }
        return "rtmp://a.rtmp.youtube.com/live2/"
    }

    if (isLive) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (hasPermission && showGallery == false) {
                CameraPreview(isFront = isFront, modifier = Modifier.fillMaxSize())
            }
            if (showGallery && galleryUri != null) {
                val safeUri = galleryUri
                if (safeUri != null) {
                    AndroidView(
                        factory = { c: Context ->
                            val vv = android.widget.VideoView(c)
                            vv.setVideoURI(safeUri)
                            vv.setOnPreparedListener { mp: android.media.MediaPlayer ->
                                mp.isLooping = loopOn
                                mp.start()
                            }
                            vv
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            if (logoOn && logoUri != null) {
                val safeLogo = logoUri
                if (safeLogo != null) {
                    AsyncImage(
                        model = safeLogo,
                        contentDescription = "Logo",
                        modifier = Modifier.offset { IntOffset(logoOffset.x.roundToInt(), logoOffset.y.roundToInt()) }
                            .size(70.dp)
                            .align(Alignment.TopStart)
                            .pointerInput(Unit) {
                                detectDragGestures { change, drag: Offset ->
                                    change.consume()
                                    logoOffset = Offset(logoOffset.x + drag.x, logoOffset.y + drag.y)
                                }
                            }
                    )
                }
            }
            if (showTitleOverlay) {
                Text(
                    text = overlayText,
                    color = overlayColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset { IntOffset(titleOffset.x.roundToInt(), titleOffset.y.roundToInt()) }
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag: Offset ->
                                change.consume()
                                titleOffset = Offset(titleOffset.x + drag.x, titleOffset.y + drag.y)
                            }
                        }
                )
            }
            Row(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LIVE - $viewers watching",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            if (ticker.isNotBlank()) {
                MovingTicker(
                    text = ticker,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp).fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isFront = !isFront },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(text = if (isFront) "FRONT" else "BACK")
                }
                Button(
                    onClick = { toggleOrientation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(text = if (isLandscape) "PORTRAIT" else "LANDSCAPE")
                }
                Button(
                    onClick = { showTitleOverlay = !showTitleOverlay },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(text = "TITLE")
                }
                Button(
                    onClick = {
                        isLive = false
                        statusText = "Stream ended"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "STOP LIVE", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(12.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "JSTV PRO v2", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Red)
            Button(
                onClick = onSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = "SETTINGS")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Send to: $dest", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val list = listOf("YouTube", "Facebook", "JSOMI", "Custom")
            for (d in list) {
                Button(
                    onClick = { onDest(d) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (dest == d) Color.Red else Color.DarkGray)
                ) {
                    Text(text = d, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(340.dp).background(Color.Black, RoundedCornerShape(16.dp))
        ) {
            if (hasPermission && showGallery == false) {
                CameraPreview(isFront = isFront, modifier = Modifier.fillMaxSize())
            } else if (showGallery && galleryUri != null) {
                val safeUri2 = galleryUri
                if (safeUri2 != null) {
                    AndroidView(
                        factory = { c: Context ->
                            val vv = android.widget.VideoView(c)
                            vv.setVideoURI(safeUri2)
                            vv.setOnPreparedListener { mp: android.media.MediaPlayer ->
                                mp.isLooping = loopOn
                                mp.start()
                            }
                            vv
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text(text = "Camera will show here", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
            if (logoOn && logoUri != null) {
                val safeLogo2 = logoUri
                if (safeLogo2 != null) {
                    AsyncImage(
                        model = safeLogo2,
                        contentDescription = "Logo",
                        modifier = Modifier.offset { IntOffset(logoOffset.x.roundToInt(), logoOffset.y.roundToInt()) }
                            .size(80.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, drag: Offset ->
                                    change.consume()
                                    logoOffset = Offset(logoOffset.x + drag.x, logoOffset.y + drag.y)
                                }
                            }
                    )
                }
            }
            if (showTitleOverlay) {
                Text(
                    text = overlayText,
                    color = overlayColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset { IntOffset(titleOffset.x.roundToInt(), titleOffset.y.roundToInt()) }
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag: Offset ->
                                change.consume()
                                titleOffset = Offset(titleOffset.x + drag.x, titleOffset.y + drag.y)
                            }
                        }
                )
            }
            if (ticker.isNotBlank()) {
                MovingTicker(
                    text = ticker,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { isFront = !isFront },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = if (isFront) "FRONT" else "BACK")
            }
            Button(
                onClick = { toggleOrientation() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = if (isLandscape) "PORTRAIT" else "LANDSCAPE")
            }
            Button(onClick = { showTitleOverlay = !showTitleOverlay }) {
                Text(text = if (showTitleOverlay) "HIDE TITLE" else "SHOW TITLE")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { logoPicker.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = "LOGO")
            }
            Button(
                onClick = { videoPicker.launch("video/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = "GALLERY")
            }
            Button(
                onClick = {
                    if (overlayColor == Color.Yellow) {
                        overlayColor = Color.Red
                    } else if (overlayColor == Color.Red) {
                        overlayColor = Color.White
                    } else {
                        overlayColor = Color.Yellow
                    }
                }
            ) {
                Text(text = "COLOR")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val titles = listOf("ENQUIRY", "RESTORATION", "BREAKING")
            for (t in titles) {
                Button(
                    onClick = {
                        overlayText = t
                        showTitleOverlay = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text(text = t, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { v: String ->
                title = v
                overlayText = v
            },
            label = { Text(text = "Stream Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = statusText, color = Color.White, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "RTMP: ${activeUrl()} + key", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        if (showGallery) {
            Button(
                onClick = { showGallery = false },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = "BACK TO CAMERA")
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        if (hasPermission == false) {
            Button(
                onClick = {
                    permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "ALLOW CAMERA + MIC", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = {
                    val k = activeKey()
                    if (k.isBlank()) {
                        statusText = "FAILED - No Stream Key for $dest! Go to SETTINGS first."
                    } else if (k.length < 8) {
                        statusText = "FAILED - Key too short for $dest. Check key in SETTINGS."
                    } else if (title.isBlank()) {
                        statusText = "Type a Title first!"
                    } else {
                        isLive = true
                        statusText = "LIVE to $dest: $title"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND)
                sendIntent.type = "text/plain"
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch JSTV LIVE: $title")
                ctx.startActivity(Intent.createChooser(sendIntent, "Share JSTV"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Text(text = "SHARE TO WHATSAPP", fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingsScreen(
    ytKey: String,
    onYt: (String) -> Unit,
    fbKey: String,
    onFb: (String) -> Unit,
    jsomiKey: String,
    onJsomi: (String) -> Unit,
    customUrl: String,
    onCustomUrl: (String) -> Unit,
    customKey: String,
    onCustomKey: (String) -> Unit,
    roomCode: String,
    onRoom: (String) -> Unit,
    quality: String,
    onQuality: (String) -> Unit,
    ticker: String,
    onTicker: (String) -> Unit,
    loopOn: Boolean,
    onLoop: (Boolean) -> Unit,
    logoOn: Boolean,
    onLogo: (Boolean) -> Unit,
    dest: String,
    onDest: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "JSTV SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "SEND TO: $dest", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val list = listOf("YouTube", "Facebook", "JSOMI", "Custom")
            for (d in list) {
                Button(
                    onClick = { onDest(d) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (dest == d) Color.Red else Color.DarkGray)
                ) {
                    Text(text = d, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "STREAM KEYS", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        FieldBox(label = "YouTube Stream Key", value = ytKey, onChange = onYt)
        FieldBox(label = "Facebook Stream Key", value = fbKey, onChange = onFb)
        FieldBox(label = "JSOMI TV Stream Key", value = jsomiKey, onChange = onJsomi)
        FieldBox(label = "Custom RTMP URL", value = customUrl, onChange = onCustomUrl)
        FieldBox(label = "Custom RTMP Key", value = customKey, onChange = onCustomKey)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "MULTI-CAMERA / REMOTE", color = Color.White, fontWeight = FontWeight.Bold)
        FieldBox(label = "Room Code (e.g. JSTV-1234)", value = roomCode, onChange = onRoom)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "VIDEO QUALITY: $quality", color = Color.White, fontWeight = FontWeight.Bold)
        Row {
            Button(
                onClick = { onQuality("720p") },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults
