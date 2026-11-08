package com.jsomi.broadcast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
    androidx.compose.material3.MaterialTheme {
        JSOMIApp92()
   }
 }
}
@Composable
fun JSOMIApp92() {
    var showWelcome by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var selectedCam by remember { mutableStateOf("CAM 1 - Main") }
    var remoteOn by remember { mutableStateOf(false) }

    if (showWelcome) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0B0B12)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("JS", fontSize = 70.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
            Text("JEHOVAH SHALOM", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("JESUS SON OF MAN INTERNATIONAL", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Healing & Deliverance - Mombasa", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { showWelcome = false },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) { Text("ENTER STUDIO", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("JSOMI STUDIO", color = Color.White, fontWeight = FontWeight.Bold)
                if (isLive) {
                    Box(modifier = Modifier.background(Color.Red, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Box(modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("OFFLINE", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CAMERA", fontSize = 50.sp)
                    Text(selectedCam, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (isLive) "Broadcasting to YouTube + Facebook" else "Preview - Tap GO LIVE to start", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("MULTI-CAMERA", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CAM 1 - Main", "CAM 2 - Choir", "CAM 3 - Crowd").forEach { cam ->
                    Button(
                        onClick = { selectedCam = cam },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedCam == cam) Color(0xFFFFD700) else Color.DarkGray)
                    ) { Text(cam, color = if (selectedCam == cam) Color.Black else Color.White, fontSize = 11.sp) }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("REMOTE CONTROL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (remoteOn) "Phone 2 can control this studio" else "Allow 2nd phone to switch cameras", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(checked = remoteOn, onCheckedChange = { remoteOn = it })
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { isLive = !isLive },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isLive) "STOP LIVE" else "GO LIVE TO ALL", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
