package com.jsomi.broadcaster

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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

class MainActivity : ComponentActivity() {

    // Logo picker - Gallery
    private var onLogoPicked: ((Uri?) -> Unit)? = null
    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onLogoPicked?.invoke(uri)
    }

    // Gallery Video picker
    private var onVideoPicked: ((Uri?) -> Unit)? = null
    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onVideoPicked?.invoke(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JstvProScreen(
                onPickLogo = { callback ->
                    onLogoPicked = callback
                    logoPicker.launch("image/*")
                },
                onPickVideo = { callback ->
                    onVideoPicked = callback
                    videoPicker.launch("video/*")
                },
                onToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

@Composable
fun JstvProScreen(
    onPickLogo: ((Uri?) -> Unit) -> Unit,
    onPickVideo: ((Uri?) -> Unit) -> Unit,
    onToast: (String) -> Unit
) {
    // --- SETTINGS: All Keys in one place ---
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var jsomiKey by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("JSTV-1234") }
    var quality by remember { mutableStateOf("720p") }
    var tickerText by remember { mutableStateOf("Welcome to JSTV PRO - Live") }

    // --- LOGO + TITLES ---
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogo by remember { mutableStateOf(true) }
    var logoOffset by remember { mutableStateOf(Offset(20f, 20f)) }
    var logoSize by remember { mutableStateOf(80f) }

    var titleText by remember { mutableStateOf("BREAKING") }
    var showTitle by remember { mutableStateOf(true) }
    var titleOffset by remember { mutableStateOf(Offset(20f, 200f)) }
    var titleSize by remember { mutableStateOf(24f) }
    var titleColor by remember { mutableStateOf("Red") }

    // --- GALLERY + LOOP ---
    var galleryVideoUri by remember { mutableStateOf<Uri?>(null) }
    var loopOn by remember { mutableStateOf(false) }
    var isPlayingGallery by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "JSTV PRO v2 - COMMIT 1", fontSize = 22.sp, color = Color.Black)
        Text(text = "Settings + Logo + Titles + Gallery Loop", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // ============ PREVIEW BOX - Movable Logo + Title ============
        Text(text = "PREVIEW - Drag Logo and Title", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(260.dp).background(Color.DarkGray)
        ) {
            // Movable Logo
            if (showLogo) {
                Box(
                    modifier = Modifier
                        .offset(logoOffset.x.dp, logoOffset.y.dp)
                        .size(logoSize.dp)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                logoOffset += dragAmount
                            }
                        }
                ) {
                    Text(
                        text = if (logoUri == null) "LOGO\nTap Pick" else "LOGO ✓",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Movable Title
            if (showTitle) {
                Box(
                    modifier = Modifier
                        .offset(titleOffset.x.dp, titleOffset.y.dp)
                        .background(
                            when (titleColor) {
                                "Red" -> Color.Red
                                "Blue" -> Color.Blue
                                "Green" -> Color.Green
                                else -> Color.Black
                            }
                        )
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                titleOffset += dragAmount
                            }
                        }
                ) {
                    Text(text = titleText, color = Color.White, fontSize = titleSize.sp)
                }
            }

            // Ticker at bottom
            Text(
                text = tickerText,
                color = Color.Yellow,
                fontSize = 12.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ SETTINGS ============
        Text(text = "SETTINGS - Stream Keys", fontSize = 18.sp)
        OutlinedTextField(value = youtubeKey, onValueChange = { youtubeKey = it }, label = { Text("YouTube Key") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = facebookKey, onValueChange = { facebookKey = it }, label = { Text("Facebook Key") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = jsomiKey, onValueChange = { jsomiKey = it }, label = { Text("JSOMI TV Key") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = customUrl, onValueChange = { customUrl = it }, label = { Text("Custom RTMP URL") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = customKey, onValueChange = { customKey = it }, label = { Text("Custom Key") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = roomCode, onValueChange = { roomCode = it }, label = { Text("Room Code e.g. JSTV-1234") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = tickerText, onValueChange = { tickerText = it }, label = { Text("Ticker Text") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(text = "Quality: $quality  ")
            Button(onClick = { quality = if (quality == "720p") "1080p" else "720p" }) {
                Text(text = "Switch to ${if (quality == "720p") "1080p" else "720p"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ LOGO CONTROLS ============
        Text(text = "LOGO", fontSize = 18.sp)
        Button(onClick = { onPickLogo { uri -> logoUri = uri } }) { Text("Pick Logo from Gallery") }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(text = "Show Logo  ")
            Switch(checked = showLogo, onCheckedChange = { showLogo = it })
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { logoSize = (logoSize + 20f).coerceAtMost(180f) }) { Text("Logo +") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { logoSize = (logoSize - 20f).coerceAtLeast(40f) }) { Text("Logo -") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ TITLE CONTROLS ============
        Text(text = "TITLES", fontSize = 18.sp)
        OutlinedTextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Custom Title Text") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { titleText = "ENQUIRY" }) { Text("ENQUIRY") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { titleText = "RESTORATION" }) { Text("RESTORATION") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { titleText = "BREAKING" }) { Text("BREAKING") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(text = "Show Title  ")
            Switch(checked = showTitle, onCheckedChange = { showTitle = it })
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { titleColor = if (titleColor == "Red") "Blue" else if (titleColor == "Blue") "Green" else "Red" }) { Text("Color: $titleColor") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { titleSize = (titleSize + 4f).coerceAtMost(48f) }) { Text("Title +") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { titleSize = (titleSize - 4f).coerceAtLeast(12f) }) { Text("Title -") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============ GALLERY + LOOP ============
        Text(text = "GALLERY + LOOP", fontSize = 18.sp)
        Button(onClick = { onPickVideo { uri -> galleryVideoUri = uri } }) { Text("Pick Gallery Video") }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = if (galleryVideoUri == null) "No video picked" else "Video picked ✓ - Ready to play")
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(text = "Loop ON/OFF  ")
            Switch(checked = loopOn, onCheckedChange = { loopOn = it })
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = if (loopOn) "LOOP: ON" else "LOOP: OFF")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (galleryVideoUri == null) {
                onToast("Pick a video first")
            } else {
                isPlayingGallery = !isPlayingGallery
                onToast(if (isPlayingGallery) "Playing Gallery Video" else "Gallery Paused")
            }
        }) {
            Text(if (isPlayingGallery) "Stop Gallery Video" else "Play Gallery Video")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onToast("Settings Saved - Commit 1 OK") }, modifier = Modifier.fillMaxWidth()) {
            Text("SAVE SETTINGS")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
