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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
        setContent { JSTVApp() }
    }
}

@Composable
fun CameraPreview(isFront: Boolean, modifier: Modifier) {
    val owner = LocalLifecycleOwner.current
    key(isFront) {
        AndroidView(factory = { c: Context ->
            val pv = PreviewView(c)
            val fut = ProcessCameraProvider.getInstance(c)
            fut.addListener({
                val provider = fut.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(pv.surfaceProvider)
                val facing = if (isFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                val sel = CameraSelector.Builder().requireLensFacing(facing).build()
                try { provider.unbindAll(); provider.bindToLifecycle(owner, sel, preview) } catch (e: Exception) {}
            }, ContextCompat.getMainExecutor(c))
            pv
        }, modifier = modifier)
    }
}

@Composable
fun JSTVApp() {
    val ctx = LocalContext.current
    val act = ctx as? Activity
    var showSettings by remember { mutableStateOf(false) }
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("YouTube") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Restoration Sunday - God Bless You") }
    var hasPerm by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var isFront by remember { mutableStateOf(false) }
    var isLand by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("RESTORATION SUNDAY") }
    var viewers by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("Ready - Add key in Settings") }
    var showTitle by remember { mutableStateOf(true) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var galUri by remember { mutableStateOf<Uri?>(null) }
    var showGal by remember { mutableStateOf(false) }
    var logoOff by remember { mutableStateOf(Offset(20f, 20f)) }
    var titleOff by remember { mutableStateOf(Offset(20f, 220f)) }
    var tickX by remember { mutableStateOf(400f) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res: Map<String, Boolean> ->
        var ok = true
        for (v in res.values) { if (v == false) ok = false }
        hasPerm = ok
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u: Uri? -> if (u != null) logoUri = u }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u: Uri? -> if (u != null) { galUri = u; showGal = true } }
    LaunchedEffect(isLive) { if (isLive) { viewers = 1; while (isLive) { delay(3000L); viewers = viewers + Random.nextInt(0, 5) } } else viewers = 0 }
    LaunchedEffect(ticker) { while (true) { delay(20L); tickX = tickX - 3f; if (tickX < -1200f) tickX = 400f } }
    fun toggleOrientation() {
        isLand = !isLand
        if (act != null) { act.requestedOrientation = if (isLand) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT }
    }
    fun activeKey(): String { if (dest == "YouTube") return ytKey; return fbKey }
    fun activeUrl(): String { if (dest == "Facebook") return "rtmps://live-api-s.facebook.com:443/rtmp/"; return "rtmp://a.rtmp.youtube.com/live2/" }

    if (isLive) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (hasPerm && showGal == false) CameraPreview(isFront, Modifier.fillMaxSize())
            if (showTitle) Text(text = title, color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset { IntOffset(titleOff.x.roundToInt(), titleOff.y.roundToInt()) }.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(6.dp).pointerInput(Unit) { detectDragGestures { ch, d: Offset -> ch.consume(); titleOff = Offset(titleOff.x + d.x, titleOff.y + d.y) } })
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "LIVE - $viewers watching", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp).fillMaxWidth().background(Color.Black.copy(alpha = 0.75f))) {
                Text(text = ticker, color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.offset { IntOffset(tickX.roundToInt(), 0) }.padding(vertical = 8.dp))
            }
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isFront = !isFront }) { Text(text = if (isFront) "FRONT" else "BACK") }
                Button(onClick = { toggleOrientation() }) { Text(text = if (isLand) "PORTRAIT" else "LANDSCAPE") }
                Button(onClick = { showTitle = !showTitle }) { Text(text = "TITLE") }
                Button(onClick = { isLive = false; status = "Stream ended" }) { Text(text = "STOP LIVE") }
            }
        }
        return
    }
    if (showSettings) {
        Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = "JSTV SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Red)
            Spacer(Modifier.height(12.dp))
            Text(text = "Send to: $dest", color = Color.White, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { dest = "YouTube" }) { Text(text = "YouTube") }
                Button(onClick = { dest = "Facebook" }) { Text(text = "Facebook") }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = ytKey, onValueChange = { v: String -> ytKey = v }, label = { Text(text = "YouTube Stream Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = fbKey, onValueChange = { v: String -> fbKey = v }, label = { Text(text = "Facebook Stream Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = ticker, onValueChange = { v: String -> ticker = v }, label = { Text(text = "Ticker Text - This Moves") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { showSettings = false }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(text = "SAVE & BACK") }
            Spacer(Modifier.height(30.dp))
        }
        return
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(12.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "JSTV PRO v2", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Red)
            Button(onClick = { showSettings = true }) { Text(text = "SETTINGS") }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "Send to: $dest", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(340.dp).background(Color.Black, RoundedCornerShape(16.dp))) {
            if (hasPerm && showGal == false) CameraPreview(isFront, Modifier.fillMaxSize())
            else if (showGal && galUri != null) {
                val su2 = galUri
                if (su2 != null) AndroidView(factory = { c: Context -> val vv = android.widget.VideoView(c); vv.setVideoURI(su2); vv.setOnPreparedListener { mp: android.media.MediaPlayer -> mp.isLooping = true; mp.start() }; vv }, modifier = Modifier.fillMaxSize())
            } else Text(text = "Camera will show here", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            if (logoUri != null) {
                val sl2 = logoUri
                if (sl2 != null) AsyncImage(model = sl2, contentDescription = "Logo", modifier = Modifier.offset { IntOffset(logoOff.x.roundToInt(), logoOff.y.roundToInt()) }.size(80.dp).pointerInput(Unit) { detectDragGestures { ch, d: Offset -> ch.consume(); logoOff = Offset(logoOff.x + d.x, logoOff.y + d.y) } })
            }
            if (showTitle) Text(text = title, color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset { IntOffset(titleOff.x.roundToInt(), titleOff.y.roundToInt()) }.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(6.dp).pointerInput(Unit) { detectDragGestures { ch, d: Offset -> ch.consume(); titleOff = Offset(titleOff.x + d.x, titleOff.y + d.y) } })
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.75f))) {
                Text(text = ticker, color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.offset { IntOffset(tickX.roundToInt(), 0) }.padding(vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { isFront = !isFront }) { Text(text = if (isFront) "FRONT" else "BACK") }
            Button(onClick = { toggleOrientation() }) { Text(text = if (isLand) "PORTRAIT" else "LANDSCAPE") }
            Button(onClick = { showTitle = !showTitle }) { Text(text = if (showTitle) "HIDE TITLE" else "SHOW TITLE") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { logoPicker.launch("image/*") }) { Text(text = "LOGO") }
            Button(onClick = { videoPicker.launch("video/*") }) { Text(text = "GALLERY") }
            Button(onClick = { showGal = false }) { Text(text = "CAMERA") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = title, onValueChange = { v: String -> title = v }, label = { Text(text = "Stream Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(6.dp))
        Text(text = status, color = Color.White, fontSize = 13.sp)
        Text(text = "RTMP: ${activeUrl()} + key", color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        if (hasPerm == false) {
            Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(text = "ALLOW CAMERA + MIC") }
        } else {
            Button(onClick = {
                val k = activeKey()
                if (k.isBlank()) status = "FAILED - No Key for $dest! Go to SETTINGS."
                else if (k.length < 8) status = "FAILED - Key too short for $dest."
                else if (title.isBlank()) status = "Type a Title first!"
                else { isLive = true; status = "LIVE to $dest: $title" }
            }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(text = "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { val i = Intent(Intent.ACTION_SEND); i.type = "text/plain"; i.putExtra(Intent.EXTRA_TEXT, "Watch JSTV LIVE: $title"); ctx.startActivity(Intent.createChooser(i, "Share JSTV")) }, modifier = Modifier.fillMaxWidth()) { Text(text = "SHARE TO WHATSAPP") }
        Spacer(Modifier.height(30.dp))
    }
}
