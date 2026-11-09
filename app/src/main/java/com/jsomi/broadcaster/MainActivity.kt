package com.jsomi.jstv

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVApp() }
    }
}

object JSTVSettings {
    var youtubeKey by mutableStateOf("")
    var facebookKey by mutableStateOf("")
    var jsomiUrl by mutableStateOf("rtmp://jsomi.tv/live")
    var jsomiKey by mutableStateOf("")
    var customUrl by mutableStateOf("")
    var customKey by mutableStateOf("")
    var roomCode by mutableStateOf("JSTV-1234")
    var tickerText by mutableStateOf("WELCOME TO JSOMI TV • RESTORATION • ")
    var quality by mutableStateOf("720p")
    var loopOn by mutableStateOf(true)
    var logoOn by mutableStateOf(true)
    var logoUri by mutableStateOf<Uri?>(null)
    var galleryVideoUri by mutableStateOf<Uri?>(null)
}

data class MovableTitle(val text: String, var offX: Float = 40f, var offY: Float = 500f, var show: Boolean = true)

@Composable
fun JSTVApp() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )}
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result[Manifest.permission.CAMERA] == true
    }

    // ASK PERMISSION ON START
    LaunchedEffect(Unit) {
        if (!permissionGranted) permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    if (!permissionGranted) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text("JSTV NEEDS PERMISSION", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text("Camera + Mic for Live Streaming", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                    Text("ALLOW PERMISSION")
                }
            }
        }
        return
    }

    var showSettings by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var titles = remember { mutableStateListOf(
        MovableTitle("RESTORATION", 40f, 600f, true),
        MovableTitle("ENQUIRY: +254 700 000 000", 40f, 700f, false),
        MovableTitle("BREAKING NEWS", 40f, 100f, false)
    )}
    var newTitleText by remember { mutableStateOf("") }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    // One offset per title - FIXED, no remember inside loop
    var titleOffsets by remember { mutableStateOf(List(10) { Offset(40f, 500f + it * 80f) }) }

    val logoPicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) JSTVSettings.logoUri = uri
    }
    val videoPicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { JSTVSettings.galleryVideoUri = uri; showGallery = true }
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false }, onPickLogo = { logoPicker.launch("image/*") })
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("JSTV", color = Color(0xFFD4AF37), fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(if (isLive) "● LIVE" else "OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111111))) {
                if (showGallery && JSTVSettings.galleryVideoUri != null) {
                    val uri = JSTVSettings.galleryVideoUri
                    AndroidView(factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(uri)
                            setOnPreparedListener { it.isLooping = JSTVSettings.loopOn; start() }
                        }
                    }, modifier = Modifier.fillMaxSize())
                } else {
                    Text("CAMERA 1 - MAIN\nFront / Back\nPermission OK", color = Color.White, modifier = Modifier.padding(16.dp))
                }
                if (JSTVSettings.logoOn && JSTVSettings.logoUri != null) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(JSTVSettings.logoUri).build(),
                        contentDescription = "Logo",
                        modifier = Modifier.offset(logoOffset.x.dp, logoOffset.y.dp).size(80.dp)
                            .pointerInput(Unit) { detectDragGestures { _, d -> logoOffset += d } },
                        contentScale = ContentScale.Fit)
                }
                // TITLES - FIXED RENDERING
                titles.forEachIndexed { index, t ->
                    if (t.show) {
                        val off = if (index < titleOffsets.size) titleOffsets[index] else Offset(40f, 500f)
                        Text(t.text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                            modifier = Modifier.offset(off.x.dp, off.y.dp)
                                .background(Color.Black.copy(alpha = 0.6f)).padding(6.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { _, d ->
                                        val newList = titleOffsets.toMutableList()
                                        newList[index] = newList[index] + d
                                        titleOffsets = newList
                                    }
                                })
                    }
                }
            }
            Box(Modifier.fillMaxWidth().background(Color(0xFFD4AF37)).padding(6.dp)) {
                Text(JSTVSettings.tickerText, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Column(Modifier.padding(12.dp)) {
                Button(onClick = { isLive = !isLive }, colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Gray else Color.Red), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                    Text(if (isLive) "STOP LIVE" else "GO LIVE - All 4 Destinations", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { showGallery = !showGallery }) { Text(if (showGallery) "Camera" else "Gallery") }
                    Button(onClick = { videoPicker.launch("video/*") }) { Text("Add Video") }
                    Button(onClick = { logoPicker.launch("image/*") }) { Text("Add Logo") }
                    Button(onClick = { showSettings = true }) { Text("Settings") }
                }
                Spacer(Modifier.height(8.dp))
                Text("Titles - Tap to Show/Hide:", color = Color.White)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    titles.forEach { t ->
                        Button(onClick = { t.show = !t.show }, colors = ButtonDefaults.buttonColors(containerColor = if (t.show) Color(0xFFD4AF37) else Color.Gray)) { Text(t.text.take(8)) }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = newTitleText, onValueChange = { newTitleText = it }, label = { Text("New Title", color = Color.White) }, modifier = Modifier.weight(1f))
                    Button(onClick = { if (newTitleText.isNotEmpty()) { titles.add(MovableTitle(newTitleText)); newTitleText = "" } }) { Text("Add") }
                }
                Text("Drag LOGO and TITLES to move them!", color = Color.Yellow, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onPickLogo: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SETTINGS - ALL FEATURES", color = Color(0xFFD4AF37), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onBack) { Text("Back") }
        }
        Spacer(Modifier.height(16.dp))
        Text("1. MULTISTREAM KEYS", color = Color.White, fontWeight = FontWeight.Bold)
        SettingsField("YouTube Stream Key", JSTVSettings.youtubeKey) { JSTVSettings.youtubeKey = it }
        SettingsField("Facebook Stream Key", JSTVSettings.facebookKey) { JSTVSettings.facebookKey = it }
        SettingsField("JSOMI TV URL", JSTVSettings.jsomiUrl) { JSTVSettings.jsomiUrl = it }
        SettingsField("JSOMI TV Key", JSTVSettings.jsomiKey) { JSTVSettings.jsomiKey = it }
        SettingsField("Custom RTMP URL", JSTVSettings.customUrl) { JSTVSettings.customUrl = it }
        SettingsField("Custom RTMP Key", JSTVSettings.customKey) { JSTVSettings.customKey = it }
        Spacer(Modifier.height(16.dp))
        Text("2. ROOM CODE", color = Color.White, fontWeight = FontWeight.Bold)
        SettingsField("Room Code", JSTVSettings.roomCode) { JSTVSettings.roomCode = it }
        Spacer(Modifier.height(16.dp))
        Text("3. VIDEO & LOOP", color = Color.White, fontWeight = FontWeight.Bold)
        SettingsField("Ticker Text", JSTVSettings.tickerText) { JSTVSettings.tickerText = it }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Loop Gallery Video: ", color = Color.White)
            Switch(checked = JSTVSettings.loopOn, onCheckedChange = { JSTVSettings.loopOn = it })
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Show Logo: ", color = Color.White)
            Switch(checked = JSTVSettings.logoOn, onCheckedChange = { JSTVSettings.logoOn = it })
        }
        Button(onClick = onPickLogo, modifier = Modifier.fillMaxWidth()) { Text("Pick Logo From Gallery") }
        Text("Quality: ${JSTVSettings.quality}", color = Color.White)
        Row {
            listOf("720p", "1080p").forEach { q ->
                Button(onClick = { JSTVSettings.quality = q }) { Text(q) }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun SettingsField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label, color = Color.Gray) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
}
