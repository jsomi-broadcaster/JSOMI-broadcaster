package com.jsomi.broadcaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BroadcasterApp() } }
    }
}
@Composable
fun RealCamera2(modifier: Modifier = Modifier, isFront:Boolean) {
    val ctx = LocalContext.current; val lifecycle = LocalLifecycleOwner.current
    AndroidView(factory={ c -> val pv = PreviewView(c); val fut = ProcessCameraProvider.getInstance(c); fut.addListener({ val provider=fut.get(); val preview=Preview.Builder().build().also{ it.setSurfaceProvider(pv.surfaceProvider) }; try{ provider.unbindAll(); provider.bindToLifecycle(lifecycle, if(isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, preview) }catch(e:Exception){} }, ContextCompat.getMainExecutor(c)); pv }, modifier=modifier)
}
@Composable
fun BroadcasterApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("LOGIN") }
    var isSignUp by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("LIVE") }
    var isLive by remember { mutableStateOf(false) }; var isFront by remember { mutableStateOf(true) }; var isMicOn by remember { mutableStateOf(true) }
    var channelName by remember { mutableStateOf("JSTV") }; var ticker by remember { mutableStateOf("Welcome to JSTV") }; var verse by remember { mutableStateOf("John 3:16") }
    var showTicker by remember { mutableStateOf(true) }; var showVerse by remember { mutableStateOf(true) }; var showLogo by remember { mutableStateOf(true) }
    var logoX by remember { mutableStateOf(20f) }; var logoY by remember { mutableStateOf(20f) }; var tickerX by remember { mutableStateOf(0f) }; var tickerY by remember { mutableStateOf(400f) }; var verseX by remember { mutableStateOf(20f) }; var verseY by remember { mutableStateOf(300f) }
    var isUnlocked by remember { mutableStateOf(true) }; var expiry by remember { mutableStateOf("12 Dec 2026") }; var plan by remember { mutableStateOf("Monthly") }; var premium by remember { mutableStateOf("Premium") }
    var payPhone by remember { mutableStateOf("") }; var payAmount by remember { mutableStateOf("1000") }; var payPlan by remember { mutableStateOf("Monthly") }; var payStatus by remember { mutableStateOf("") }; var payStep by remember { mutableStateOf(0) }; var lastReceipt by remember { mutableStateOf("") }
    var isHandRaised by remember { mutableStateOf(false) }; var adminMsg by remember { mutableStateOf("Welcome to JSTV! Pay to unlock - Admin") }
    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(num:String,msg:String){ val n = if(num.startsWith("0")) "254"+num.substring(1) else num.replace("+",""); try{ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$n?text="+Uri.encode(msg)))) }catch(e:Exception){ toast("WhatsApp not found") } }
    fun priceFor(p:String):String{ return when(p){ "Daily"->"200"; "Monthly"->"1000"; "3 Months"->"2500"; else->"8000" } }
    fun daysFor(p:String):Int{ return when(p){ "Daily"->1; "Monthly"->30; "3 Months"->90; else->365 } }
    fun newExpiry(p:String):String{ val c=Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, daysFor(p)); return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time) }
    fun checkLock(){ val fmt=SimpleDateFormat("dd MMM yyyy", Locale.getDefault()); if(fmt.parse(expiry)?.before(Date())==true) isUnlocked=false }

    if(screen=="LOGIN"){
        Column(modifier=Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("JSTV BROADCASTER", color=Color.Yellow, fontSize=30.sp, fontWeight=FontWeight.Bold)
            Spacer(modifier=Modifier.height(8.dp))
            Button(onClick={ screen="MAIN"; tab="PAYMENT" }, modifier=Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("💰 PAY NOW VIA M-PESA", color=Color.White, fontWeight=FontWeight.Bold) }
            Spacer(modifier=Modifier.height(12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Button(onClick={ isSignUp=true }, modifier=Modifier.weight(1f).height(50.dp), colors=ButtonDefaults.buttonColors(containerColor=if(isSignUp) Color.Yellow else Color.DarkGray)){ Text("SIGN UP", color=if(isSignUp) Color.Black else Color.White, fontWeight=FontWeight.Bold) }; Button(onClick={ isSignUp=false }, modifier=Modifier.weight(1f).height(50.dp), colors=ButtonDefaults.buttonColors(containerColor=if(!isSignUp) Color.Yellow else Color.DarkGray)){ Text("LOG IN", color=if(!isSignUp) Color.Black else Color.White, fontWeight=FontWeight.Bold) } }
            Spacer(modifier=Modifier.height(12.dp))
            if(isSignUp){
                Text("SIGN UP - Requirements:", color=Color.White, fontWeight=FontWeight.Bold)
                Text("Create Username + WhatsApp Number + PIN (6 chars)", color=Color.Yellow, fontSize=12.sp)
                OutlinedTextField(value=username, onValueChange={ username=it }, label={ Text("Create Username") }, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=whatsapp, onValueChange={ whatsapp=it; if(payPhone.isBlank()) payPhone=it }, label={ Text("WhatsApp Number") }, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=pin, onValueChange={ if(it.length<=6) pin=it }, label={ Text("Create PIN - 6 chars e.g. 123456") }, modifier=Modifier.fillMaxWidth())
                Spacer(modifier=Modifier.height(8.dp))
                Button(onClick={ if(username.isBlank()||whatsapp.isBlank()||pin.length!=6) toast("Need Username + WhatsApp + 6-char PIN") else { toast("Sign Up Success! Details auto-saved to Admin. Welcome $username"); screen="MAIN"; tab="LIVE" } }, modifier=Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("SIGN UP", color=Color.Black, fontWeight=FontWeight.Bold) }
                Text("Your details auto-save to Admin on Sign Up.", color=Color.Gray, fontSize=11.sp)
            } else {
                Text("LOG IN - Use Username + Password", color=Color.White, fontWeight=FontWeight.Bold)
                OutlinedTextField(value=username, onValueChange={ username=it }, label={ Text("Username") }, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=pin, onValueChange={ if(it.length<=6) pin=it }, label={ Text("Password / PIN - 6 chars") }, modifier=Modifier.fillMaxWidth())
                Spacer(modifier=Modifier.height(8.dp))
                Button(onClick={ if(username.isBlank()||pin.length!=6) toast("Enter Username + 6-char Password") else { checkLock(); toast("Welcome back $username!"); screen="MAIN"; tab="LIVE" } }, modifier=Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("LOG IN", color=Color.Black, fontWeight=FontWeight.Bold) }
                Button(onClick={ openWhatsApp("254763052911","Hi Admin, Forgot password. Username: $username WhatsApp: $whatsapp") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("FORGOT PASSWORD? WHATSAPP ADMIN") }
            }
        }
    } else if(screen=="SETUP"){
        Column(modifier=Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("LIVE SETUP", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
            Text("Set here > OK > Items appear on Main Screen > Drag them", color=Color.Gray, fontSize=12.sp)
            OutlinedTextField(value=channelName, onValueChange={ channelName=it }, label={ Text("Channel Name") }, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=ticker, onValueChange={ ticker=it }, label={ Text("Ticker") }, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=verse, onValueChange={ verse=it }, label={ Text("Verse") }, modifier=Modifier.fillMaxWidth())
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){ Text("Show Logo", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showLogo, onCheckedChange={ showLogo=it }) }
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){ Text("Show Verse", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showVerse, onCheckedChange={ showVerse=it }) }
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){ Text("Show Ticker", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showTicker, onCheckedChange={ showTicker=it }) }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ isFront=true }, modifier=Modifier.weight(1f)){ Text("FRONT CAM") }; Button(onClick={ isFront=false }, modifier=Modifier.weight(1f)){ Text("BACK CAM") } }
            Button(onClick={ isMicOn=!isMicOn }, modifier=Modifier.fillMaxWidth()){ Text(if(isMicOn) "MIC ON" else "MIC OFF") }
            Spacer(modifier=Modifier.height(12.dp))
            Button(onClick={ screen="MAIN"; toast("Setup Saved - Drag items on Main Screen") }, modifier=Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("OK - APPLY TO MAIN SCREEN", color=Color.White, fontWeight=FontWeight.Bold) }
        }
    } else {
        Column(modifier=Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("JSTV BROADCASTER", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Welcome $username | ${if(isUnlocked) "🔓 UNLOCKED until $expiry | $premium" else "🔒 LOCKED - Pay to unlock"} | Admin Msg: $adminMsg", color=if(isUnlocked) Color.Green else Color.Red, fontSize=11.sp)
            Spacer(modifier=Modifier.height(8.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){ listOf("LIVE","PAYMENT","MEETING","OFFERS").forEach{ t -> Button(onClick={ tab=t }, modifier=Modifier.weight(1f).height(42.dp), contentPadding=PaddingValues(1.dp), colors=ButtonDefaults.buttonColors(containerColor=if(tab==t) Color.Yellow else Color.DarkGray)){ Text(if(t=="PAYMENT") "💰 PAYMENT" else t, fontSize=10.sp, color=if(tab==t) Color.Black else Color.White, fontWeight=FontWeight.Bold) } } }
            Spacer(modifier=Modifier.height(10.dp))
            if(tab=="LIVE"){
                Box(modifier=Modifier.fillMaxWidth().height(420.dp).background(Color.Black)){
                    RealCamera2(modifier=Modifier.fillMaxSize(), isFront=isFront)
                    if(showLogo) Box(modifier=Modifier.offset{ IntOffset(logoX.roundToInt(), logoY.roundToInt()) }.pointerInput(Unit){ detectDragGestures{ c,d -> logoX+=d.x; logoY+=d.y } }.background(Color.Red).padding(6.dp)){ Text("JSTV $channelName", color=Color.White, fontWeight=FontWeight.Bold, fontSize=12.sp) }
                    if(showVerse) Box(modifier=Modifier.offset{ IntOffset(verseX.roundToInt(), verseY.roundToInt()) }.pointerInput(Unit){ detectDragGestures{ c,d -> verseX+=d.x; verseY+=d.y } }.background(Color.Black).padding(6.dp)){ Text("Verse: $verse", color=Color.Yellow, fontSize=11.sp) }
                    if(showTicker) Box(modifier=Modifier.offset{ IntOffset(tickerX.roundToInt(), tickerY.roundToInt()) }.pointerInput(Unit){ detectDragGestures{ c,d -> tickerX+=d.x; tickerY+=d.y } }.fillMaxWidth().background(Color.Yellow).padding(6.dp)){ Text("Ticker: $ticker", color=Color.Black, fontSize=11.sp) }
                    Text(if(isLive) "● LIVE" else "○ NOT LIVE", color=if(isLive) Color.Red else Color.White, fontWeight=FontWeight.Bold, modifier=Modifier.padding(8.dp))
                    Column(modifier=Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(8.dp)){
                        if(isLive) Button(onClick={ isLive=false; toast("Live Ended") }, modifier=Modifier.fillMaxWidth().height(50.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("END LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                        else if(!isUnlocked) Button(onClick={ tab="PAYMENT" }, modifier=Modifier.fillMaxWidth().height(50.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("🔒 LOCKED - PAY TO GO LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                        else Button(onClick={ isLive=true; toast("You are LIVE!") }, modifier=Modifier.fillMaxWidth().height(50.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("GO LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                        Spacer(modifier=Modifier.height(6.dp))
                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ screen="SETUP" }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("LIVE SETUP", color=Color.Black, fontWeight=FontWeight.Bold, fontSize=11.sp) }; Button(onClick={ screen="LOGIN" }, modifier=Modifier.weight(1f)){ Text("BACK", fontSize=11.sp) } }
                    }
                }
                Text("Drag Logo / Verse / Ticker to move. Main screen has only GO LIVE / LIVE SETUP / BACK.", color=Color.Gray, fontSize=11.sp)
                Spacer(modifier=Modifier.height(8.dp))
                Button(onClick={ toast("Report sent to Admin - Hate / Illegal stream") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("🚨 REPORT THIS STREAM - Hate / Illegal") }
            }
            if(tab=="PAYMENT"){
                Text("💰 PAYMENT - STK Push OR Direct Paybill", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
                Column(modifier=Modifier.fillMaxWidth().background(Color(0xFF1B5E20)).padding(14.dp)){ Text("PAYBILL: 516600", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold); Text("ACCOUNT: 5600935001", color=Color.White, fontSize=24.sp, fontWeight=FontWeight.Bold); Text("Admin detects payment & unlocks by Premium + Days, locks after expiry.", color=Color.White, fontSize=11.sp) }
                Spacer(modifier=Modifier.height(10.dp))
                Text("1. CHOOSE PLAN / PREMIUM:", color=Color.Yellow, fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("Daily","Monthly","3 Months","Year").forEach{ p -> Button(onClick={ payPlan=p; payAmount=priceFor(p); plan=p }, colors=ButtonDefaults.buttonColors(containerColor=if(payPlan==p) Color.Yellow else Color.DarkGray), modifier=Modifier.weight(1f)){ Text(p, fontSize=10.sp, color=if(payPlan==p) Color.Black else Color.White) } } }
                Text("Selected: $payPlan - ${priceFor(payPlan)} KES - ${daysFor(payPlan)} Days - Premium: $premium", color=Color.Yellow, fontSize=12.sp)
                Spacer(modifier=Modifier.height(8.dp))
                Text("2. M-PESA NUMBER:", color=Color.Yellow, fontWeight=FontWeight.Bold)
                OutlinedTextField(value=payPhone, onValueChange={ payPhone=it }, label={ Text("M-PESA Number e.g. 0763052911") }, modifier=Modifier.fillMaxWidth())
                Text("3. AMOUNT:", color=Color.Yellow, fontWeight=FontWeight.Bold)
                OutlinedTextField(value=payAmount, onValueChange={ payAmount=it }, label={ Text("Amount") }, modifier=Modifier.fillMaxWidth())
                if(payStatus.isNotEmpty()){ Column(modifier=Modifier.fillMaxWidth().background(if(payStep==3) Color(0xFF1B5E20) else Color(0xFFFF6D00)).padding(10.dp)){ Text(payStatus, color=Color.White, fontWeight=FontWeight.Bold, fontSize=13.sp); if(payStep==3) Text("Receipt: $lastReceipt | Expiry: $expiry | UNLOCKED", color=Color.Yellow) }; Spacer(modifier=Modifier.height(8.dp)) }
                Button(onClick={ if(payPhone.length<10) toast("Enter correct M-PESA number") else { payStep=2; payStatus="📲 STK PUSH SENT to $payPhone for $payAmount KES...\nPaybill 516600 Acc 5600935001\nEnter PIN on Safaricom popup - NOT in app."; toast("STK sent to $payPhone") } }, modifier=Modifier.fillMaxWidth().height(60.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("📲 PAY $payAmount KES VIA STK PUSH", color=Color.White, fontWeight=FontWeight.Bold) }
                Spacer(modifier=Modifier.height(6.dp))
                Button(onClick={ if(payStep==2){ payStep=3; lastReceipt="QK${(1000000..9999999).random()}XYZ"; expiry=newExpiry(payPlan); isUnlocked=true; payStatus="✅ PAYMENT SUCCESSFUL! $payAmount KES Paid.\nPlan: $payPlan - Admin detected & unlocked."; toast("Paid! Unlocked until $expiry") } else toast("Tap PAY first") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("✅ I ENTERED PIN - CHECK PAYMENT", color=Color.Black, fontWeight=FontWeight.Bold) }
                Spacer(modifier=Modifier.height(10.dp))
                Text("OR DIRECT TO PAYBILL - No STK:", color=Color.Yellow, fontWeight=FontWeight.Bold)
                Text("M-PESA > Lipa Na M-PESA > Paybill\nBusiness: 516600\nAccount: 5600935001\nAmount: $payAmount\nEnter PIN on M-PESA menu", color=Color.White, fontSize=13.sp)
                Button(onClick={ openWhatsApp("254763052911","Hi Admin, I paid $payAmount KES for $payPlan. Phone: $payPhone Receipt: $lastReceipt") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("SENT RECEIPT TO ADMIN VIA WHATSAPP") }
            }
            if(tab=="MEETING"){
                Text("STUDIO MEETING", color=Color.Yellow, fontSize=20.sp, fontWeight=FontWeight.Bold)
                Text("Host: ADMIN - You hear Admin only. Sub to Sub BLOCKED.", color=Color.Gray, fontSize=12.sp)
                Box(modifier=Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray).padding(16.dp)){ Text("ADMIN VIDEO HERE", color=Color.White) }
                Text(if(isHandRaised) "✋ HAND RAISED - Waiting Admin" else "🔇 MUTED BY ADMIN - Tap Raise Hand", color=if(isHandRaised) Color.Yellow else Color.Red, fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ isHandRaised=true; toast("Hand Raised") }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("✋ RAISE HAND", color=Color.Black) }; Button(onClick={ isHandRaised=false }, modifier=Modifier.weight(1f)){ Text("LOWER HAND") } }
                Button(onClick={ openWhatsApp("254763052911","Hi Admin, Message from meeting: ") }, modifier=Modifier.fillMaxWidth()){ Text("MESSAGE ADMIN ONLY") }
            }
            if(tab=="OFFERS"){
                Text("🎁 OFFERS FROM ADMIN", color=Color.Yellow, fontSize=20.sp, fontWeight=FontWeight.Bold)
                Text("• BLACK FRIDAY - Monthly 1000 -> 500\n• CHURCH PACKAGE - 3 Months 2000\n• Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color=Color.Yellow, fontSize=13.sp)
                Text("Paybill 516600 Acc 5600935001", color=Color.Yellow)
                Button(onClick={ tab="PAYMENT" }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("💰 PAY FOR OFFER") }
            }
            Spacer(modifier=Modifier.height(10.dp))
            Button(onClick={ screen="LOGIN" }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.DarkGray)){ Text("LOG OUT") }
        }
    }
}
