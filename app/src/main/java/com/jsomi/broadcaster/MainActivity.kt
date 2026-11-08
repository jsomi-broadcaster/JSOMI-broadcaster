package com.jsomi.broadcaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var screen by remember { mutableStateOf("home") }
                var isLive by remember { mutableStateOf(false) }
                var camera by remember { mutableStateOf("REAR") }
                var activeCam by remember { mutableStateOf("CAM 1") }
                var error by remember { mutableStateOf("") }

                // YOU TYPE THESE WHEN YOU GO LIVE - NOT SAVED IN CODE
                var youtubeOn by remember { mutableStateOf(true) }
                var facebookOn by remember { mutableStateOf(true) }
                var websiteOn by remember { mutableStateOf(false) }
                var youtubeUrl by remember { mutableStateOf("") }
                var facebookUrl by remember { mutableStateOf("") }
                var websiteUrl by remember { mutableStateOf("") }

                fun startLive(): Boolean {
                    if (!youtubeOn && !facebookOn && !websiteOn) { error = "Turn ON at least 1 destination"; return false }
                    if (youtubeOn && youtubeUrl.isBlank()) { error = "Paste YouTube RTMP URL first"; return false }
                    if (facebookOn && facebookUrl.isBlank()) { error = "Paste Facebook RTMP URL first"; return false }
                    if (websiteOn && websiteUrl.isBlank()) { error = "Paste Website RTMP URL first"; return false }
                    error = ""; return true
                }

                when (screen) {
                    "home" -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(painter = painterResource(id = R.mipmap.jstv_logo), contentDescription = "JSTV", modifier = Modifier.size(140.dp).clip(CircleShape))
                        Spacer(Modifier.height(12.dp))
                        Text("JEHOVAH SHALOM\nOUTREACH MINISTRIES", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("JSTV - Multi-Camera Live Studio", fontSize = 15.sp)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { screen = "multi" }, modifier = Modifier.fillMaxWidth()) { Text("MULTI-CAMERA") }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { screen = "remote" }, modifier = Modifier.fillMaxWidth()) { Text("REMOTE CONTROL") }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { screen = "dest" }, modifier = Modifier.fillMaxWidth()) { Text("STREAM URLs - TYPE HERE") }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { if (!isLive) { if (startLive()) isLive = true } else isLive = false }, colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A)), modifier = Modifier.fillMaxWidth()) { Text(if (isLive) "● STOP LIVE" else "GO LIVE") }
                        if (error.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(error, color = Color.Red, textAlign = TextAlign.Center) }
                        if (isLive) { Spacer(Modifier.height(8.dp)); Text("● LIVE to: " + listOfNotNull(if (youtubeOn) "YouTube" else null, if (facebookOn) "Facebook" else null, if (websiteOn) "Website" else null).joinToString(", "), color = Color.Red, textAlign = TextAlign.Center) }
                    }
                    "multi" -> Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MULTI-CAMERA STUDIO", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black), contentAlignment = Alignment.Center) { Text("$activeCam\n$camera Camera\n${if (isLive) "● LIVE" else "Preview"}", color = Color.White, textAlign = TextAlign.Center) }
                        Spacer(Modifier.height(12.dp))
                        Row { Button(onClick = { activeCam = "CAM 1" }) { Text("CAM 1") }; Spacer(Modifier.width(6.dp)); Button(onClick = { activeCam = "CAM 2" }) { Text("CAM 2") }; Spacer(Modifier.width(6.dp)); Button(onClick = { activeCam = "CAM 3" }) { Text("CAM 3") } }
                        Spacer(Modifier.height(12.dp))
                        Row { Button(onClick = { camera = if (camera == "FRONT") "REAR" else "FRONT" }) { Text("SWITCH: $camera") }; Spacer(Modifier.width(8.dp)); Button(onClick = {}) { Text("+ ADD CAMERA") } }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { screen = "dest" }) { Text("SET STREAM URLs →") }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { if (!isLive) { if (startLive()) isLive = true } else isLive = false }, colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A))) { Text(if (isLive) "STOP LIVE" else "GO LIVE") }
                        if (error.isNotEmpty()) Text(error, color = Color.Red, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { screen = "home" }) { Text("← BACK") }
                    }
                    "remote" -> Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REMOTE CONTROL", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("2nd phone as CAM 2\nInstall JSTV on 2nd phone > JOIN AS CAM 2", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {}) { Text("CONNECT CAM 2") }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {}) { Text("CONNECT CAM 3") }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { screen = "home" }) { Text("← BACK") }
                    }
                    "dest" -> Column(Modifier.fillMaxSize().padding(20.dp)) {
                        Text("STREAM URLs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Paste when you want to go live - like PRISM", fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = youtubeOn, onCheckedChange = { youtubeOn = it }); Text("YouTube Live", fontWeight = FontWeight.Bold) }
                        OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("YouTube RTMP URL + Key") }, placeholder = { Text("rtmp://a.rtmp.youtube.com/live2/xxxx-xxxx") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = facebookOn, onCheckedChange = { facebookOn = it }); Text("Facebook Live", fontWeight = FontWeight.Bold) }
                        OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("Facebook RTMP URL + Key") }, placeholder = { Text("rtmps://live-api-s.facebook.com:443/rtmp/xxxx") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = websiteOn, onCheckedChange = { websiteOn = it }); Text("JSTV Website", fontWeight = FontWeight.Bold) }
                        OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Website RTMP URL + Key") }, placeholder = { Text("rtmp://your-website/live/xxxx") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        if (error.isNotEmpty()) Text(error, color = Color.Red)
                        Button(onClick = { if (startLive()) { isLive = true; screen = "home" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A3A))) { Text("SAVE & GO LIVE") }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { screen = "home" }, modifier = Modifier.fillMaxWidth()) { Text("← BACK WITHOUT GOING LIVE") }
                    }
                }
            }
        }
    }
}
