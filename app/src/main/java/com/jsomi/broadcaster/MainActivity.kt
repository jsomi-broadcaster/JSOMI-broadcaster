package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BroadcasterApp() } }
    }
}

@Composable
fun RealCamera2(modifier: Modifier = Modifier, isFront: Boolean) {
    val lifecycle = LocalLifecycleOwner.current
    AndroidView(factory = { c ->
        val pv = PreviewView(c)
        val fut = ProcessCameraProvider.getInstance(c)
        fut.addListener({
            val provider = fut.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
            try { provider.unbindAll(); provider.bindToLifecycle(lifecycle, if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, preview) } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(c))
        pv
    }, modifier = modifier)
}

@Composable
fun BroadcasterApp() {
    val ctx = LocalContext.current
    var hasPerm by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res -> hasPerm = res.values.all { it } }
    LaunchedEffect(Unit) { if (!hasPerm) permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }

    var screen by remember { mutableStateOf("ROLE") }
    var role by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var isLinked by remember { mutableStateOf(false) }

    var isLive by remember { mutableStateOf(false) }
    var liveSeconds by remember { mutableStateOf(0) }
    var isSubscribed by remember { mutableStateOf(false) }
    var isFront by remember { mutableStateOf(true) }
    var isMicOn by remember { mutableStateOf(true) }
    var channelName by remember { mutableStateOf("JSTV") }
    var ticker by remember { mutableStateOf("Welcome to JSTV") }
    var verse by remember { mutableStateOf("John 3:16") }
    var showTicker by remember { mutableStateOf(true) }
    var showVerse by remember { mutableStateOf(true) }
    var showLogo by remember { mutableStateOf(true) }
    var logoX by remember { mutableStateOf(20f) }; var logoY by remember { mutableStateOf(20f) }
    var tickerX by remember { mutableStateOf(0f) }; var tickerY by remember { mutableStateOf(400f) }
    var verseX by remember { mutableStateOf(20f) }; var verseY by remember { mutableStateOf(300f) }

    var payPlan by remember { mutableStateOf("Monthly") }
    var payPhone by remember { mutableStateOf("") }
    var payAmount by remember { mutableStateOf("1000") }
    var payStatus by remember { mutableStateOf("") }
    var receiptCode by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }

    // ===== TWO POPS =====
    var showPremiumPopup by remember { mutableStateOf(false) } // POP 1 - After 30 sec
    var showPayMethodPopup by remember { mutableStateOf(false) } // POP 2 - Choose How To Pay
    var payMethod by remember { mutableStateOf("") } // "PAYBILL" or "STK"

    val inbox = remember { mutableStateListOf<String>() }
    var unread by remember { mutableStateOf(0) }

    fun toast(m: String) { Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(num: String, msg: String) {
        val n = if (num.startsWith("0")) "254" + num.substring(1) else num.replace("+", "")
        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$n?text=" + Uri.encode(msg)))) } catch (e: Exception) { toast("WhatsApp not found") }
    }
    fun priceFor(p: String) = when (p) { "Daily" -> "200"; "Monthly" -> "1000"; "3 Months" -> "2500"; else -> "8000" }
    fun daysFor(p: String) = when (p) { "Daily" -> 1; "Monthly" -> 30; "3 Months" -> 90; else -> 365 }

    LaunchedEffect(isLive) {
        if (isLive && !isSubscribed) {
            liveSeconds = 0
            while (isLive && liveSeconds < 30) { delay(1000); liveSeconds++ }
            if (isLive && !isSubscribed) { isLive = false; showPremiumPopup = true }
        }
    }

    if (screen == "ROLE") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Cyan).padding(10.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("BROADCASTER STUDIO", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("CYAN + GREEN", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("CYAN + GREEN = BROADCASTER STUDIO", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(if (hasPerm) "Camera + Mic: ALLOWED" else "Camera + Mic: NOT ALLOWED YET", color = if (hasPerm) Color.Green else Color.Red, fontSize = 12.sp)
            if (!hasPerm) Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("ALLOW CAMERA + MIC") }
            Text("Time: ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())}", color = Color.Gray)
            Text("Choose Your Phone Role:", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { role = "MAIN STUDIO"; screen = "SIGNUP" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MAIN STUDIO", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Goes LIVE - Controlled by ADMIN", color = Color.Black, fontSize = 12.sp) } }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { role = "REMOTE CONTROL"; screen = "SIGNUP" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("REMOTE CONTROL", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Control Main From Far", color = Color.Black, fontSize = 12.sp) } }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { role = "WIRELESS CAMERA"; screen = "SIGNUP" }, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("WIRELESS CAMERA", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("2nd Phone As Camera", color = Color.Black, fontSize = 12.sp) } }
            Spacer(modifier = Modifier.height(16.dp))
            Text("LINK TO JSTV STUDIO ADMIN (RED LOGO)", color = Color.Yellow, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            OutlinedTextField(value = roomCode, onValueChange = { roomCode = it }, label = { Text("Room Code from ADMIN") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { if (roomCode.isBlank()) toast("Enter Room Code given by Admin") else { isLinked = true; toast("Linked to ADMIN: $roomCode") } }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text(if (isLinked) "LINKED - $roomCode" else "LINK TO ADMIN", color = Color.Black, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { openWhatsApp("254763052911", "Hi Admin, I want to link. Room: $roomCode") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("WHATSAPP ADMIN - 0763052911", color = Color.White) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { screen = "INBOX"; unread = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))) { Text("📩 INBOX ${if (unread > 0) "($unread NEW)" else ""}") }
        }
    }

    if (screen == "SIGNUP") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("JSTV BROADCASTER - $role", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(if (isLinked) "Linked: $roomCode" else "Not Linked - Enter Room Code on First Page", color = if (isLinked) Color.Green else Color.Red, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Create Username + WhatsApp + PIN (6 chars)", color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Create Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it; if (payPhone.isBlank()) payPhone = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 6) pin = it }, label = { Text("Create PIN - 6 chars") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { if (username.isBlank() || whatsapp.isBlank() || pin.length != 6) toast("Need Username + WhatsApp + 6-char PIN") else { screen = "MAIN"; toast("Welcome $username - Role: $role") } }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("CONTINUE TO STUDIO", color = Color.Black, fontWeight = FontWeight.Bold) }
            Button(onClick = { screen = "ROLE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK TO ROLES") }
        }
    }

    if (screen == "MAIN") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("JSTV - $role", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Button(onClick = { screen = "INBOX"; unread = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))) { Text("📩 INBOX ${if (unread > 0) "($unread)" else ""}", fontSize = 11.sp) }
            }
            Text("Welcome $username | ${if (isSubscribed) "PREMIUM - Until $expiry" else "FREE PREVIEW - 30 sec FREE LIVE"} | Linked: $roomCode", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp))
            if (isLive) Text("● LIVE - ${30 - liveSeconds} sec left FREE", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
                if (hasPerm) RealCamera2(modifier = Modifier.fillMaxSize(), isFront = isFront)
                else Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) { Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) { Text("ALLOW CAMERA TO SEE YOURSELF") } }
                if (showLogo) Box(modifier = Modifier.offset { IntOffset(logoX.roundToInt(), logoY.roundToInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> logoX += d.x; logoY += d.y } }.background(Color.Red).padding(6.dp)) { Text("JSTV $channelName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                if (showVerse) Box(modifier = Modifier.offset { IntOffset(verseX.roundToInt(), verseY.roundToInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> verseX += d.x; verseY += d.y } }.background(Color.Black).padding(6.dp)) { Text("Verse: $verse", color = Color.Yellow, fontSize = 11.sp) }
                if (showTicker) Box(modifier = Modifier.offset { IntOffset(tickerX.roundToInt(), tickerY.roundToInt()) }.pointerInput(Unit) { detectDragGestures { _, d -> tickerX += d.x; tickerY += d.y } }.fillMaxWidth().background(Color.Yellow).padding(6.dp)) { Text("Ticker: $ticker", color = Color.Black, fontSize = 11.sp) }
                Text(if (isLive) "● LIVE" else "○ PREVIEW - NOT LIVE YET", color = if (isLive) Color.Red else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Drag Logo / Verse / Ticker to move. Do all settings BEFORE Go Live.", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isLive) Button(onClick = { isLive = false; toast("Live Ended") }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("END LIVE", color = Color.White, fontWeight = FontWeight.Bold) }
                else Button(onClick = { if (!hasPerm) permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) else { isLive = true; toast("You are LIVE! 30 sec FREE") } }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("GO LIVE - 30 SEC FREE", color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { screen = "SETUP" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("LIVE SETUP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    Button(onClick = { screen = "ROLE" }, modifier = Modifier.weight(1f)) { Text("BACK", fontSize = 11.sp) }
                }
            }
        }
    }

    if (screen == "SETUP") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("LIVE SETUP", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Set here > OK > Items appear on Main Screen > Drag them", color = Color.Gray, fontSize = 12.sp)
            OutlinedTextField(value = channelName, onValueChange = { channelName = it }, label = { Text("Channel Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = verse, onValueChange = { verse = it }, label = { Text("Verse") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Logo", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = showLogo, onCheckedChange = { showLogo = it }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Verse", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = showVerse, onCheckedChange = { showVerse = it }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Show Ticker", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = showTicker, onCheckedChange = { showTicker = it }) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Button(onClick = { isFront = true }, modifier = Modifier.weight(1f)) { Text("FRONT CAM") }; Button(onClick = { isFront = false }, modifier = Modifier.weight(1f)) { Text("BACK CAM") } }
            Button(onClick = { isMicOn = !isMicOn }, modifier = Modifier.fillMaxWidth()) { Text(if (isMicOn) "MIC ON" else "MIC OFF") }
            if (!hasPerm) Button(onClick = { permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("ALLOW CAMERA + MIC PERMISSION") }
            Spacer(modifier = Modifier.height(14.dp))
            Text("PREMIUM - Subscribe to continue after 30 sec FREE:", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Button(onClick = { showPayMethodPopup = true }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("PREMIUM - SUBSCRIBE NOW", color = Color.White, fontWeight = FontWeight.Bold) }
            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { screen = "MAIN"; toast("Setup Saved") }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) { Text("OK - APPLY TO MAIN SCREEN", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    }

    // ===== POP 1 - AFTER 30 SEC - PREMIUM ASKS =====
    if (showPremiumPopup) {
        AlertDialog(onDismissRequest = { showPremiumPopup = false },
            title = { Text("30 Seconds Finished!") },
            text = { Text("Your FREE 30 sec is over. Subscribe to Premium to continue LIVE.\n\nDaily 200 | Monthly 1000 | 3 Months 2500 | Year 8000\n\nPaybill 516600 Acc 5600935001") },
            confirmButton = { Button(onClick = { showPremiumPopup = false; showPayMethodPopup = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("SUBSCRIBE NOW") } },
            dismissButton = { Button(onClick = { showPremiumPopup = false }) { Text("LATER") } }
        )
    }

    // ===== POP 2 - CHOOSE HOW TO PAY - TWO WAYS =====
    if (showPayMethodPopup) {
        AlertDialog(onDismissRequest = { showPayMethodPopup = false },
            title = { Text("Choose How To Pay") },
            text = { Text("TWO REAL WAYS:\n\n1. PAYBILL MANUAL - You pay yourself in M-PESA menu.\nPaybill 516600 Acc 5600935001\n\n2. STK PUSH - Admin pushes to your phone. A REAL Safaricom PIN popup will come on YOUR phone. You enter PIN there.\n\nChoose one:") },
            confirmButton = {
                Column {
                    Button(onClick = { payMethod = "PAYBILL"; showPayMethodPopup = false; screen = "PAYMENT" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("1. PAYBILL MANUAL") }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = {
                        payMethod = "STK"
                        if (payPhone.length < 10 && whatsapp.length < 10) toast("Enter your number in SIGNUP / PAYMENT first")
                        else {
                            showPayMethodPopup = false; screen = "PAYMENT"
                            openWhatsApp("254763052911", "Hi Admin, REQUEST STK PUSH\nName: $username\nPhone: ${if (payPhone.isNotBlank()) payPhone else whatsapp}\nPlan: $payPlan - ${priceFor(payPlan)} KES\nPlease PUSH STK to my phone. I will enter PIN on Safaricom popup.")
                            toast("STK Request sent to Admin - Wait for Safaricom popup")
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))) { Text("2. REQUEST STK PUSH") }
                }
            },
            dismissButton = { Button(onClick = { showPayMethodPopup = false }) { Text("CANCEL") } }
        )
    }

    if (screen == "PAYMENT") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("PREMIUM - PAYMENT - REAL M-PESA", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(if (payMethod == "STK") "You chose: STK PUSH - Wait for REAL Safaricom PIN popup on your phone. Admin must push it. Do NOT enter PIN in this app." else "You chose: PAYBILL MANUAL - Pay yourself in M-PESA menu.", color = if (payMethod == "STK") Color.Cyan else Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            // Button to change method - shows POP 2 again
            Button(onClick = { showPayMethodPopup = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("CHANGE PAYMENT METHOD - SHOW POP 2 AGAIN") }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1B5E20)).padding(14.dp)) {
                Text("PAYBILL: 516600", color = Color.Yellow, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("ACCOUNT: 5600935001", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("CHOOSE PLAN:", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Daily", "Monthly", "3 Months", "Year").forEach { p -> Button(onClick = { payPlan = p; payAmount = priceFor(p) }, colors = ButtonDefaults.buttonColors(containerColor = if (payPlan == p) Color.Yellow else Color.DarkGray), modifier = Modifier.weight(1f)) { Text(p, fontSize = 10.sp, color = if (payPlan == p) Color.Black else Color.White) } } }
            Text("Selected: $payPlan - ${priceFor(payPlan)} KES - ${daysFor(payPlan)} Days", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // POP 2 - METHOD 1 - PAYBILL
            if (payMethod != "STK") {
                Text("METHOD 1 - PAYBILL MANUAL:", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Text("1. Open M-PESA on your phone\n2. Lipa Na M-PESA > Paybill\n3. Business: 516600\n4. Account: 5600935001\n5. Amount: $payAmount\n6. Enter M-PESA PIN in M-PESA menu\n7. You will get SMS with Receipt Code", color = Color.White, fontSize = 14.sp)
            }
            // POP 2 - METHOD 2 - STK
            if (payMethod == "STK") {
                Text("METHOD 2 - STK PUSH:", color = Color.Cyan, fontWeight = FontWeight.Bold)
                Text("1. Admin will PUSH STK to your phone\n2. A REAL Safaricom popup will appear on YOUR phone\n3. Enter YOUR M-PESA PIN on THAT Safaricom popup - NOT in this app\n4. You will get SMS with Receipt Code\n5. If no popup in 2 mins, tap REQUEST AGAIN below", color = Color.White, fontSize = 14.sp)
                Button(onClick = { openWhatsApp("254763052911", "Hi Admin, REQUEST STK AGAIN\nName: $username\nPhone: $payPhone\nPlan: $payPlan - $payAmount KES") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) { Text("REQUEST STK PUSH AGAIN") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("STEP - ENTER REAL RECEIPT CODE:", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Copy Code from YOUR M-PESA SMS. Do not invent a code.", color = Color.Gray, fontSize = 12.sp)
            OutlinedTextField(value = receiptCode, onValueChange = { receiptCode = it.uppercase() }, label = { Text("Receipt Code from M-PESA SMS") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = payPhone, onValueChange = { payPhone = it }, label = { Text("Your M-PESA Number") }, modifier = Modifier.fillMaxWidth())
            if (payStatus.isNotEmpty()) { Text(payStatus, color = Color.Yellow, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (receiptCode.length < 6) toast("Enter REAL Receipt Code from M-PESA SMS")
                else if (payPhone.length < 10) toast("Enter your M-PESA Number")
                else {
                    payStatus = "WAITING FOR ADMIN...\nMethod: $payMethod\nReceipt: $receiptCode\nPlan: $payPlan - $payAmount KES\nAdmin will check M-PESA and UNLOCK you."
                    toast("Receipt sent to Admin!")
                    openWhatsApp("254763052911", "Hi Admin, I PAID JSTV\nMethod: $payMethod\nName: $username\nPhone: $payPhone\nPlan: $payPlan - $payAmount KES\nReceipt: $receiptCode\nPlease verify and UNLOCK me.")
                }
            }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) { Text("I HAVE PAID - SEND RECEIPT TO ADMIN", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(8.dp))
            Text("No auto-unlock. Admin must verify in M-PESA.", color = Color.Gray, fontSize = 12.sp)
            Button(onClick = { screen = "MAIN" }, modifier = Modifier.fillMaxWidth()) { Text("BACK TO STUDIO") }
        }
    }

    if (screen == "INBOX") {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("📩 INBOX - ADMIN NOTIFICATIONS", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Only Admin can send here. No messages yet means Admin has not sent any.", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            if (inbox.isEmpty()) { Text("No messages yet.", color = Color.White) }
            inbox.forEach { msg -> Column(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(12.dp)) { Text(msg, color = Color.White) }; Spacer(modifier = Modifier.height(6.dp)) }
            Button(onClick = { openWhatsApp("254763052911", "Hi Admin, message from Broadcaster $username") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("REPLY TO ADMIN ON WHATSAPP") }
            Button(onClick = { screen = "ROLE" }, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
        }
    }
}
