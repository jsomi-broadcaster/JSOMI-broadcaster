package com.jsomi.jstv

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVApp() }
    }
}

@Composable
fun JSTVApp() {
    var hasPermission by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Ready to go live") }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.all { it }
        statusText = if (hasPermission) "Permissions granted - Ready!" else "Permissions needed"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("JSTV", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.Red)
        Text("JSOMI BROADCASTER", fontSize = 14.sp, color = Color.White, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isLive) "● LIVE" else "○ OFFLINE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (isLive) Color.Red else Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText, color = Color.White, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasPermission) {
            Button(
                onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("ALLOW CAMERA + MIC", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        } else {
            Button(
                onClick = {
                    isLive = !isLive
                    statusText = if (isLive) "You are LIVE!" else "Stream ended"
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color.Gray else Color.Red)
            ) { Text(if (isLive) "STOP LIVE" else "GO LIVE", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("1. Allow Camera + Mic\n2. Tap GO LIVE\n3. Your viewers will see you", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
