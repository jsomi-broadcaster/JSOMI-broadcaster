package com.jsomi.broadcaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// JSTV BROADCASTER - SUBSCRIBER APP - LOGIN REQUIRED
// WhatsApp: 0763052911 | Paybill 516600 Acc 5600935001
// Rates: Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BroadcasterApp() } }
    }
}

@Composable
fun BroadcasterApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("PERMISSIONS") }

    var isLoggedIn by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(true) }
    var signWhats by remember { mutableStateOf("") }
    var signUser by remember { mutableStateOf("") }
    var signPass by remember { mutableStateOf("") }
    var loginUser by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var savedUser by remember { mutableStateOf("") }
    var savedPass by remember { mutableStateOf("") }
    var savedWhats by remember { mutableStateOf("") }

    var channel by remember { mutableStateOf("JSTV Main") }
    var scene by remember { mutableStateOf("Studio") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Your Voice, Your Channel") }
    var cameraOn by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("HD 720p") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var streamTitle by remember { mutableStateOf("My Live Show") }
    var showOffers by remember { mutableStateOf(false) }

    fun toast(m: String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(msg: String){
        try{ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text="+Uri.encode(msg)))) }catch(e:Exception){ toast("WhatsApp not found") }
    }
    fun tryGoLive(){
        if(isPremium){ isLive=true; toast("You Are LIVE - $channel") }
        else { showPremium=true }
    }

    Column(modifier=Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){

        if(screen=="PERMISSIONS"){
            Text("JSTV", color=Color.Black, fontSize=42.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth().background(Color.Cyan).padding(16.dp))
            Text("BROADCASTER STUDIO", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold)
            Spacer(modifier=Modifier.height(16.dp))
            Text("🔐 PERMISSIONS", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold)
            Text("📷 Camera: ✅\n🔊 Audio: ✅\n📁 File: ✅\nAllow all 5", color=Color.White, fontSize=14.sp)
            Spacer(modifier=Modifier.height(12.dp))
            Button(onClick={ toast("Permissions Granted") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Cyan)){ Text("ALLOW ALL PERMISSIONS", color=Color.Black) }
            Button(onClick={ toast("Open Settings") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7C4DFF))){ Text("OPEN SETTINGS") }
            Button(onClick={ screen="AUTH" }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("CONTINUE → CHOOSE ROLE") }
            return@Column
        }

        if(screen=="AUTH" && !isLoggedIn){
            Text("JSTV BROADCASTER", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Create Your Own Live Channel - Free To Try", color=Color.Gray, fontSize=12.sp)
            Spacer(modifier=Modifier.height(8.dp))
            Box(modifier=Modifier.fillMaxWidth().background(Color(0xFF1B5E20)).padding(10.dp)){
                Column{ Text("PAYBILL: 516600", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold); Text("ACCOUNT: 5600935001", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold); Text("WhatsApp: 0763052911", color=Color.White, fontSize=12.sp) }
            }
            Spacer(modifier=Modifier.height(12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={isSignUp=true}, colors=ButtonDefaults.buttonColors(containerColor=if(isSignUp) Color.Yellow else Color.DarkGray)){ Text("SIGN UP", color=if(isSignUp) Color.Black else Color.White) }
                Button(onClick={isSignUp=false}, colors=ButtonDefaults.buttonColors(containerColor=if(!isSignUp) Color.Yellow else Color.DarkGray)){ Text("LOG IN", color=if(!isSignUp) Color.Black else Color.White) }
            }
            Spacer(modifier=Modifier.height(12.dp))
            if(isSignUp){
                Text("SIGN UP - Admin will save your login", color=Color.White, fontWeight=FontWeight.Bold)
                OutlinedTextField(value=signWhats, onValueChange={signWhats=it}, label={Text("WhatsApp Number - e.g. 0763052911")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=signUser, onValueChange={signUser=it}, label={Text("Username")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=signPass, onValueChange={ if(it.length<=6) signPass=it }, label={Text("Password - 6 chars")}, modifier=Modifier.fillMaxWidth())
                Text("Password must be 6 - Letters, Digits, or any case - e.g. Ab1234, Xy9876", color=Color.Gray, fontSize=11.sp)
                Text("Characters: ${signPass.length}/6", color=Color.Yellow, fontSize=12.sp)
                Button(onClick={
                    if(signWhats.isBlank()||signUser.isBlank()||signPass.length!=6){ toast("WhatsApp + Username + 6-char Password required") }
                    else { savedUser=signUser; savedPass=signPass; savedWhats=signWhats; isLoggedIn=true; screen="HOME"; toast("Welcome $signUser! Your login saved in Admin App"); openWhatsApp("Hi JSTV, I signed up. Username: $signUser WhatsApp: $signWhats - Please send me Premium Code. I will pay Paybill 516600 Acc 5600935001") }
                }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("SIGN UP & CONTINUE") }
                Text("By signing up, Admin saves your Username + Password.", color=Color.Gray, fontSize=11.sp)
            } else {
                Text("LOG IN - Use Username + Password", color=Color.White, fontWeight=FontWeight.Bold)
                OutlinedTextField(value=loginUser, onValueChange={loginUser=it}, label={Text("Username")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(value=loginPass, onValueChange={ if(it.length<=6) loginPass=it }, label={Text("Password - 6 chars")}, modifier=Modifier.fillMaxWidth())
                Button(onClick={
                    if(loginUser.isBlank()||loginPass.length!=6){ toast("Enter Username + 6-char Password") }
                    else if(savedUser.isNotEmpty() && (loginUser!=savedUser || loginPass!=savedPass)){ toast("Wrong Username or Password - Check with Admin on WhatsApp") }
                    else { isLoggedIn=true; screen="HOME"; toast("Welcome back $loginUser!") }
                }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("LOG IN", color=Color.Black) }
                Button(onClick={ openWhatsApp("Hi JSTV, I forgot my password. Username: $loginUser - Paybill 516600 Acc 5600935001") }, modifier=Modifier.fillMaxWidth()){ Text("FORGOT PASSWORD? WHATSAPP ADMIN") }
            }
            Spacer(modifier=Modifier.height(10.dp))
            Button(onClick={ showOffers=!showOffers }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7C4DFF))){ Text("🎁 VIEW OFFERS / PROMOTIONS") }
            if(showOffers){
                Text("OFFERS FROM ADMIN:\n• BLACK FRIDAY - Monthly 1000 -> 500\n• CHURCH PACKAGE - 3 Months 2000\n• Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000\nPaybill 516600 Acc 5600935001", color=Color.Yellow, fontSize=13.sp)
            }
            return@Column
        }

        Text("JSTV", color=Color.Yellow, fontSize=42.sp, fontWeight=FontWeight.Bold)
        Text("BROADCASTER", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold)
        Text("Logged in as: $savedUser • $savedWhats • ${if(isPremium) "Premium ✅" else "Free Trial"}", color=Color.Gray, fontSize=11.sp)
        Spacer(modifier=Modifier.height(8.dp))
        Button(onClick={ isLoggedIn=false; screen="AUTH"; toast("Logged Out") }){ Text("LOG OUT") }
        Spacer(modifier=Modifier.height(8.dp))
        Box(modifier=Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment=androidx.compose.ui.Alignment.Center){
            Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally){
                Text(if(isLive) "ON AIR - LIVE" else "PREVIEW", color=if(isLive) Color.Red else Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold)
                Text("Channel: $channel", color=Color.White)
                Text("Scene: $scene", color=Color.White)
                Text("Camera: ${if(cameraOn) "ON" else "OFF"} | Mic: ${if(micOn) "ON" else "OFF"}", color=Color.Gray, fontSize=12.sp)
                Text("Admin can control this from Studio Admin", color=Color.Yellow, fontSize=11.sp)
            }
        }
        Spacer(modifier=Modifier.height(12.dp))
        Text("Stream Title", color=Color.White)
        OutlinedTextField(value=streamTitle, onValueChange={streamTitle=it}, label={Text("e.g. My Live Show")}, modifier=Modifier.fillMaxWidth())
        Spacer(modifier=Modifier.height(8.dp))
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
            Button(onClick={cameraOn=!cameraOn}, colors=ButtonDefaults.buttonColors(containerColor=if(cameraOn) Color.Green else Color.DarkGray)){ Text(if(cameraOn) "CAMERA ON" else "CAMERA OFF") }
            Button(onClick={micOn=!micOn}, colors=ButtonDefaults.buttonColors(containerColor=if(micOn) Color.Green else Color.DarkGray)){ Text(if(micOn) "MIC ON" else "MIC OFF") }
        }
        Spacer(modifier=Modifier.height(8.dp))
        Text("Ticker: $ticker", color=Color.Yellow, fontSize=12.sp)
        Spacer(modifier=Modifier.height(8.dp))
        Button(onClick={ if(isLive){ isLive=false; toast("Broadcast Stopped") } else { tryGoLive() } }, modifier=Modifier.fillMaxWidth().height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=if(isLive) Color.Red else Color.Green)){ Text(if(isLive) "STOP LIVE" else "GO LIVE", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold) }
        Text("Try all features free. Go Live unlocks with Premium Code from Admin.", color=Color.Gray, fontSize=11.sp)
        if(isPremium) Text("Premium Active - Enjoy", color=Color.Green)
        Spacer(modifier=Modifier.height(12.dp))
        Text("Video Quality", color=Color.White)
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
            listOf("SD 480p","HD 720p","FHD 1080p").forEach{ q -> Button(onClick={quality=q}, colors=ButtonDefaults.buttonColors(containerColor=if(quality==q) Color.Yellow else Color.DarkGray)){ Text(q, fontSize=10.sp, color=if(quality==q) Color.Black else Color.White) } }
        }
        Text("Selected: $quality", color=Color.Yellow)
        Spacer(modifier=Modifier.height(8.dp))
        Text("Scene", color=Color.White)
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
            listOf("Studio","Outdoor","Church").forEach{ s -> Button(onClick={scene=s}, colors=ButtonDefaults.buttonColors(containerColor=if(scene==s) Color.Yellow else Color.DarkGray)){ Text(s, color=if(scene==s) Color.Black else Color.White) } }
        }
        Spacer(modifier=Modifier.height(8.dp))
        Text("Ticker Text", color=Color.White)
        OutlinedTextField(value=ticker, onValueChange={ticker=it}, label={Text("Ticker - Shows on Live")}, modifier=Modifier.fillMaxWidth())
        Spacer(modifier=Modifier.height(8.dp))
        Text("YOUR CHANNELS", color=Color.Yellow, fontWeight=FontWeight.Bold)
        listOf("JSTV Main","JSTV Sports","JSTV Music","JSTV News","JSTV Church","My Channel").forEach{ c ->
            Button(onClick={channel=c; toast("Channel: $c")}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(channel==c) Color.Yellow else Color.DarkGray)){ Text(c, color=if(channel==c) Color.Black else Color.White) }
            Spacer(modifier=Modifier.height(4.dp))
        }
        Spacer(modifier=Modifier.height(12.dp))
        Text("HELP & CONTACT", color=Color.Yellow, fontWeight=FontWeight.Bold)
        Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000\nPaybill 516600 Acc 5600935001", color=Color.White, fontSize=13.sp)
        Button(onClick={ openWhatsApp("Hi JSTV, I want Premium Code to Go Live. Username: $savedUser - I will pay Paybill 516600 Acc 5600935001") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP US - 0763052911", fontWeight=FontWeight.Bold) }

        if(showPremium){
            Spacer(modifier=Modifier.height(20.dp))
            Box(modifier=Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)){
                Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally){
                    Text("GO LIVE - PREMIUM REQUIRED", color=Color.Yellow, fontWeight=FontWeight.Bold)
                    Spacer(modifier=Modifier.height(8.dp))
                    Box(modifier=Modifier.fillMaxWidth().background(Color(0xFF1B5E20)).padding(12.dp)){
                        Column(horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally){
                            Text("PAY WITH M-PESA", color=Color.White, fontSize=13.sp, fontWeight=FontWeight.Bold)
                            Text("PAYBILL: 516600", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
                            Text("ACCOUNT: 5600935001", color=Color.White, fontSize=22.sp, fontWeight=FontWeight.Bold)
                            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color=Color.White, fontSize=11.sp)
                        }
                    }
                    Spacer(modifier=Modifier.height(8.dp))
                    Text("1. Pay to Paybill 516600 Acc 5600935001\n2. Send M-PESA SMS to WhatsApp 0763052911\n3. Admin will send you Code\n4. Enter Code below", color=Color.White, fontSize=12.sp)
                    Spacer(modifier=Modifier.height(8.dp))
                    Button(onClick={ openWhatsApp("Hi JSTV, I want to pay.\nPaybill: 516600\nAccount: 5600935001\nUsername: $savedUser\nWhatsApp: $savedWhats\nPlease confirm.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP - SEND PAYMENT - 0763052911") }
                    OutlinedTextField(value=code, onValueChange={code=it.uppercase()}, label={Text("Enter Code - e.g. JSTV-1000-XXXX")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={
                        if(code.startsWith("JSTV-")){ isPremium=true; showPremium=false; isLive=true; toast("Premium Unlocked - You Are LIVE!") }
                        else toast("Wrong Code - WhatsApp Admin")
                    }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("UNLOCK & GO LIVE", color=Color.Black, fontWeight=FontWeight.Bold) }
                    Button(onClick={ showPremium=false }, modifier=Modifier.fillMaxWidth()){ Text("CANCEL - TRY LATER") }
                }
            }
        }
    }
}
