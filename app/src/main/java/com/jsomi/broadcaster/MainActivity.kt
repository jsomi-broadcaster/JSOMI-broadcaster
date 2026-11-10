package com.jstv.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
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
    // === 226 PERMISSION - THIS IS WHAT MADE 226 WORK ===
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> hasPermission = result.values.all { it } }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    var tickerText by remember { mutableStateOf("JSTV - JEHOVAH SHALOM | PREMIUM: Daily 50 | Monthly 1000 | 3 Months 2500 | Year 8000 | Paybill 516600 Acc 5600935001 | WhatsApp 0763052911") }
    var premiumCode by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var premiumMessage by remember { mutableStateOf("") }
    var premiumPlan by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Daily 50") }
    var scene by remember { mutableStateOf(1) }
    var isLive by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }

    val validCodes = listOf(
        "JSTV-50-1A2B", "JSTV-50-2B3C", "JSTV-50-3C4D", "JSTV-50-4D5E", "JSTV-50-5E6F",
        "JSTV-1K-3C4D", "JSTV-1K-4D5E", "JSTV-1K-5E6F", "JSTV-1K-6F7G", "JSTV-1K-7G8H",
        "JSTV-3M-5E6F", "JSTV-3M-6F7G", "JSTV-3M-7G8H", "JSTV-3M-8H9I", "JSTV-3M-9I0J",
        "JSTV-YR-7G8H", "JSTV-YR-8H9I", "JSTV-YR-9I0J", "JSTV-YR-1J2K", "JSTV-YR-2K3L"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "JSTV", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "By Kevin", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        Text(text = "Jehovah Shalom", fontSize = 12.sp, color = Color.Yellow)
        Spacer(modifier = Modifier.height(8.dp))

        // 226 PERMISSION BUTTON - Shows if not allowed yet
        if (!hasPermission) {
            Text(text = "Tap ALLOW when phone asks for Camera + Mic", color = Color.Yellow, fontSize = 12.sp, textAlign = TextAlign.Center)
            Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) { Text("ALLOW CAMERA + MIC") }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(text = "✅ Camera + Mic Allowed - 226 OK", color = Color.Green, fontSize = 12.sp)
        }

        Text(text = if (isLive) "● LIVE" else "○ OFFLINE", color = if (isLive) Color.Red else Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (isPremium) Text(text = "⭐ PREMIUM ACTIVE - $premiumPlan", color = Color.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "SCENES", color = Color.White, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scene = 1; showGallery = false }) { Text("S1 STUDIO") }
            Button(onClick = { scene = 2; showGallery = false }) { Text("S2 CHURCH") }
            Button(onClick = { scene = 3; showGallery = false }) { Text("S3 PREACHER") }
            Button(onClick = { if (isPremium) { scene = 4; showGallery = true } else { premiumMessage = "🔒 S4 GALLERY is Premium Only." } }) { Text("S4 🔒") }
        }
        Text(text = "Current Scene: S$scene", color = Color.Yellow, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { isLive = true }) { Text("START LIVE") }
            Button(onClick = { isLive = false }) { Text("STOP LIVE") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (scene == 1 || scene == 2) {
            Text(text = tickerText, color = Color.White, fontSize = 13.sp, modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(8.dp)).padding(10.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (showGallery) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.DarkGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(text = if (isPremium) "GALLERY / PREMIUM VIDEOS HERE" else "🔒 LOCKED", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(text = if (isPremium) "✅ PREMIUM ACTIVE" else "JSTV PREMIUM", color = Color.Yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { selectedPlan = "Daily 50" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Daily 50") Color.Yellow else Color.DarkGray)) { Text("Daily 50", color = if (selectedPlan == "Daily 50") Color.Black else Color.White) }
            Button(onClick = { selectedPlan = "Monthly 1000" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Monthly 1000") Color.Yellow else Color.DarkGray)) { Text("Monthly 1000", color = if (selectedPlan == "Monthly 1000") Color.Black else Color.White) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { selectedPlan = "3 Months 2500" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "3 Months 2500") Color.Yellow else Color.DarkGray)) { Text("3 Months 2500", color = if (selectedPlan == "3 Months 2500") Color.Black else Color.White) }
            Button(onClick = { selectedPlan = "Year 8000" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == "Year 8000") Color.Yellow else Color.DarkGray)) { Text("Year 8000", color = if (selectedPlan == "Year 8000") Color.Black else Color.White) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Selected: $selectedPlan", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = "Paybill 516600 | Acc 5600935001 | WhatsApp 0763052911", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = premiumCode, onValueChange = { premiumCode = it }, label = { Text("Enter Premium Code") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (validCodes.contains(premiumCode.trim().uppercase())) { isPremium = true; premiumPlan = selectedPlan; premiumMessage = "✅ Welcome to Premium! $selectedPlan Active - Jehovah Shalom" }
            else { premiumMessage = "❌ Wrong Code. Pay via Paybill 516600 then WhatsApp 0763052911" }
        }, modifier = Modifier.fillMaxWidth()) { Text("UNLOCK PREMIUM - $selectedPlan") }
        if (premiumMessage.isNotEmpty()) Text(text = premiumMessage, color = if (isPremium) Color.Green else Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "JSTV - Jehovah Shalom - By Kevin", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}
