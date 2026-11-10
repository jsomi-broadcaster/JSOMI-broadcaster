package com.jstv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var tickerText by remember { mutableStateOf("JSTV - JESUS SAVES | PREMIUM: Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000 | Paybill 516600 Acc 5600935001 | WhatsApp 0763052911") }
    var premiumCode by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }
    var scene by remember { mutableStateOf(1) }
    var isLive by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }

    // Valid codes - add your 50 codes here
    val validCodes = listOf(
        "JSTV-50-1A2B", "JSTV-1K-3C4D", "JSTV-3M-5E6F", "JSTV-YR-7G8H",
        "JSTV-50-2B3C", "JSTV-1K-4D5E", "JSTV-3M-6F7G", "JSTV-YR-8H9I"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LOGO - JSTV + By Kevin
        Text(text = "JSTV", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "By Kevin", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        Text(text = "Jesus Saves", fontSize = 12.sp, color = Color.Yellow)

        Spacer(modifier = Modifier.height(12.dp))

        // LIVE STATUS
        Text(
            text = if (isLive) "● LIVE" else "○ OFFLINE",
            color = if (isLive) Color.Red else Color.Gray,
            fontSize = 18.sp, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SCENES - S1 S2 S3 S4
        Text(text = "SCENES", color = Color.White, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scene = 1; showGallery = false }) { Text("S1 STUDIO") }
            Button(onClick = { scene = 2; showGallery = false }) { Text("S2 CHURCH") }
            Button(onClick = { scene = 3; showGallery = false }) { Text("S3 PREACHER") }
            Button(onClick = { scene = 4; showGallery = true }) { Text("S4 GALLERY") }
        }
        Text(text = "Current Scene: S$scene", color = Color.Yellow, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // BROADCAST BUTTONS
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { isLive = true }) { Text("START LIVE") }
            Button(onClick = { isLive = false }) { Text("STOP LIVE") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TICKER - Show based on Scene
        if (scene == 1 || scene == 2) {
            Text(text = tickerText, color = Color.White, fontSize = 13.sp, modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(8.dp)).padding(10.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GALLERY - Scene 4
        if (showGallery) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.DarkGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(text = "GALLERY / VIDEO HERE", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // PREMIUM CODE BOX
        Text(text = if (isPremium) "✅ PREMIUM ACTIVE" else "JSTV PREMIUM", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 12.sp)
        Text(text = "Paybill 516600 Acc 5600935001 | WhatsApp 0763052911", color = Color.White, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = premiumCode,
            onValueChange = { premiumCode = it },
            label = { Text("Enter Premium Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (validCodes.contains(premiumCode.trim().uppercase())) {
                    isPremium = true
                    premiumMessage = "✅ Welcome to Premium! Code Accepted."
                } else {
                    premiumMessage = "❌ Wrong Code. Pay via Paybill 516600 then WhatsApp 0763052911"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("UNLOCK PREMIUM") }

        if (premiumMessage.isNotEmpty()) {
            Text(text = premiumMessage, color = if (isPremium) Color.Green else Color.Red, fontSize = 13.sp)
        }
    }
}
