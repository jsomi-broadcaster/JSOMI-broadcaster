package com.jsomi.broadcaster

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

const val DB_URL = "https://jstv-studio-default-rtdb.firebaseio.com"
fun fbGet(path: String, onDone: (String)->Unit){ Thread{ try{ val c=URL("$DB_URL/$path.json").openConnection() as HttpURLConnection; c.connectTimeout=8000; onDone(c.inputStream.bufferedReader().readText()) }catch(e:Exception){ onDone("") } }.start() }
fun fbPut(path: String, json: String){ Thread{ try{ val c=URL("$DB_URL/$path.json").openConnection() as HttpURLConnection; c.requestMethod="PUT"; c.doOutput=true; c.outputStream.write(json.toByteArray()); c.inputStream.bufferedReader().readText() }catch(e:Exception){} }.start() }

class MainActivity : ComponentActivity(){ override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); setContent{ MaterialTheme{ BroadcasterApp() } } } }
@Composable fun RealCam(modifier: Modifier=Modifier, isFront: Boolean){ val lc=LocalLifecycleOwner.current; AndroidView(factory={ c-> val pv=PreviewView(c); try{ val f=ProcessCameraProvider.getInstance(c); f.addListener({ try{ val p=f.get(); val pr=Preview.Builder().build().also{it.setSurfaceProvider(pv.surfaceProvider)}; p.unbindAll(); p.bindToLifecycle(lc, if(isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, pr)}catch(e:Exception){} }, ContextCompat.getMainExecutor(c))}catch(e:Exception){}; pv }, modifier=modifier) }

@Composable fun BroadcasterApp(){
    val ctx=LocalContext.current
    var hasPerm by remember{ mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
    val permLauncher=androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ r-> hasPerm=r.values.all{it} }
    var screen by remember{ mutableStateOf("LOGIN") }
    var username by remember{ mutableStateOf("")}; var whatsapp by remember{ mutableStateOf("")}; var pin by remember{ mutableStateOf("")}
    var myKey by remember{ mutableStateOf("")}; var myPlan by remember{ mutableStateOf("Monthly")}; var myExpiry by remember{ mutableStateOf("")}; var isUnlocked by remember{ mutableStateOf(false)}; var isBanned by remember{ mutableStateOf(false)}
    var adminMsg by remember{ mutableStateOf("")}; var offerMsg by remember{ mutableStateOf("")}; var ratesText by remember{ mutableStateOf("Daily 200 KES | Monthly 1000 KES | 3 Months 2500 KES | Year 8000 KES")}
    var isLive by remember{ mutableStateOf(false)}; var isFront by remember{ mutableStateOf(true)}; var isMicOn by remember{ mutableStateOf(true)}
    var channelName by remember{ mutableStateOf("JSTV")}; var ticker by remember{ mutableStateOf("Welcome to JSTV")}; var verse by remember{ mutableStateOf("John 3:16")}
    var showTicker by remember{ mutableStateOf(true)}; var showVerse by remember{ mutableStateOf(true)}; var showLogo by remember{ mutableStateOf(true)}
    var logoX by remember{ mutableStateOf(20f)}; var logoY by remember{ mutableStateOf(20f)}; var tickerX by remember{ mutableStateOf(0f)}; var tickerY by remember{ mutableStateOf(400f)}; var verseX by remember{ mutableStateOf(20f)}; var verseY by remember{ mutableStateOf(300f)}
    var reportText by remember{ mutableStateOf("")}
    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun checkAdmin(){
        fbGet("adminMessage"){ r-> if(r.isNotBlank() && r!="null") adminMsg=r.replace("\"","") }
        fbGet("offer"){ r-> if(r.isNotBlank() && r!="null") offerMsg=r.replace("\"","") }
        fbGet("rates"){ r-> if(r.isNotBlank() && r!="null") ratesText=r.replace("\"","").replace("|","\n") }
        fbGet("liveControl"){ r-> if(r.contains("STOP")){ isLive=false; toast("Admin STOPPED Live") } ; if(r.contains("MUTE")){ isMicOn=false; toast("Admin MUTED you") }; if(r.contains("BAN") && r.contains(username)){ isBanned=true; toast("You are BANNED by Admin") } }
        if(myKey.isNotBlank()){ fbGet("users/$myKey"){ r-> try{ if(r.isNotBlank() && r!="null"){ val o=JSONObject(r); isUnlocked=o.optBoolean("isUnlocked"); isBanned=o.optBoolean("banned"); myExpiry=o.optString("expiry"); myPlan=o.optString("plan") } }catch(e:Exception){} } }
    }
    LaunchedEffect(screen){ if(screen=="DASH" || screen=="LIVE"){ checkAdmin() } }

    if(screen=="LOGIN"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
            Text("JSTV BROADCASTER", color=Color.Yellow, fontSize=26.sp, fontWeight=FontWeight.Bold)
            Text("Sign Up = Create User + WhatsApp + PIN\nSign In = Username + PIN", color=Color.White, fontSize=12.sp)
            if(adminMsg.isNotBlank()) Text("📩 ADMIN: $adminMsg", color=Color.Cyan, fontWeight=FontWeight.Bold)
            if(offerMsg.isNotBlank()) Text("💰 OFFER: $offerMsg", color=Color.Green, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value=username, onValueChange={username=it}, label={Text("Username")}, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=whatsapp, onValueChange={whatsapp=it}, label={Text("WhatsApp Number e.g. 0712345678")}, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=pin, onValueChange={pin=it}, label={Text("PIN / Password")}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick={
                if(username.isBlank() || whatsapp.isBlank() || pin.length<4) toast("Enter Username + WhatsApp + PIN 4+ digits")
                else { // SIGN UP - AUTO SAVE TO ADMIN VIA FIREBASE
                    val key=username.lowercase().replace(" ","_")
                    val j=JSONObject(); j.put("username",username); j.put("whatsapp",whatsapp); j.put("pin",pin); j.put("plan",myPlan); j.put("amount",1000); j.put("expiry","Not set yet"); j.put("isUnlocked",false); j.put("receipt",""); j.put("banned",false); j.put("muted",false)
                    fbPut("users/$key", j.toString()); myKey=key; toast("Signed Up! Details sent to Admin. Wait for Admin to Unlock."); screen="WAITING"
                }
            }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("SIGN UP - CREATE ACCOUNT", color=Color.White, fontWeight=FontWeight.Bold) }
            Button(onClick={
                if(username.isBlank() || pin.isBlank()) toast("Enter Username + PIN")
                else { val key=username.lowercase().replace(" ","_"); fbGet("users/$key"){ r-> if(r.isBlank() || r=="null") toast("User not found - Sign Up first") else { try{ val o=JSONObject(r); if(o.optString("pin")==pin){ myKey=key; whatsapp=o.optString("whatsapp"); myPlan=o.optString("plan"); myExpiry=o.optString("expiry"); isUnlocked=o.optBoolean("isUnlocked"); isBanned=o.optBoolean("banned"); if(isBanned) toast("You are BANNED by Admin") else if(!isUnlocked) screen="WAITING" else screen="DASH" } else toast("Wrong PIN") }catch(e:Exception){ toast("Login error") } } } }
            }, Modifier.fillMaxWidth().height(54.dp)){ Text("SIGN IN - USERNAME + PIN", fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Text("RATES:\n$ratesText", color=Color.Yellow, fontSize=12.sp)
            Text("Pay: Paybill 516600 Acc 5600935001\nThen send Receipt to Admin to Unlock", color=Color.Gray, fontSize=11.sp)
        }
    }
    if(screen=="WAITING"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
            Text("WAITING FOR ADMIN", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Hi $username\nYour details are saved in Admin.\nPay then send Receipt to Admin.\nAdmin will Unlock you.", color=Color.White)
            Text("Plan: $myPlan | Expiry: $myExpiry", color=Color.Cyan)
            Button(onClick={ checkAdmin(); if(isUnlocked && !isBanned) screen="DASH" else toast("Still locked - Wait for Admin") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("CHECK IF ADMIN UNLOCKED ME") }
            Button(onClick={ screen="LOGIN" }, Modifier.fillMaxWidth()){ Text("BACK TO LOGIN") }
        }
    }
    if(screen=="DASH"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("Welcome $username", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Status: ${if(isBanned)"BANNED" else if(isUnlocked)"UNLOCKED until $myExpiry" else "LOCKED"} | Plan: $myPlan", color=if(isUnlocked) Color.Green else Color.Red)
            if(adminMsg.isNotBlank()) Text("📩 ADMIN MESSAGE: $adminMsg", color=Color.Cyan, fontWeight=FontWeight.Bold)
            if(offerMsg.isNotBlank()) Text("💰 OFFER: $offerMsg", color=Color.Green, fontWeight=FontWeight.Bold)
            Button(onClick={ checkAdmin(); screen="LIVE" }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("📹 GO TO LIVE STUDIO", color=Color.White, fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value=reportText, onValueChange={reportText=it}, label={Text("Report hate / illegal stream...")}, modifier=Modifier.fillMaxWidth(), minLines=2)
            Button(onClick={ if(reportText.isNotBlank()){ fbPut("alerts", JSONObject.quote("REPORT from $username: $reportText")); fbPut("chat", JSONObject.quote(reportText)); toast("Report sent to Admin"); reportText="" } }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("REPORT TO ADMIN", color=Color.Black) }
            Button(onClick={ screen="LOGIN" }, Modifier.fillMaxWidth()){ Text("LOG OUT") }
        }
    }
    if(screen=="LIVE"){
        if(isBanned){ Column(Modifier.fillMaxSize().background(Color.Red).padding(20.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("YOU ARE BANNED BY ADMIN", color=Color.White, fontWeight=FontWeight.Bold, fontSize=22.sp); Button(onClick={screen="DASH"}){ Text("BACK") } } }
        else Box(Modifier.fillMaxSize().background(Color.Black)){
            if(hasPerm) RealCam(Modifier.fillMaxSize(), isFront) else Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment=Alignment.Center){ Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }){ Text("ALLOW CAMERA + MIC - REAL CAMERA") } }
            if(hasPerm){
                if(showLogo) Box(Modifier.offset{IntOffset(logoX.roundToInt(), logoY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> logoX+=d.x; logoY+=d.y}}.background(Color.Red).padding(8.dp)){ Text("JSTV $channelName", color=Color.White, fontWeight=FontWeight.Bold) }
                if(showVerse) Box(Modifier.offset{IntOffset(verseX.roundToInt(), verseY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> verseX+=d.x; verseY+=d.y}}.background(Color.Black).padding(8.dp)){ Text("Verse: $verse", color=Color.Yellow) }
                if(showTicker) Box(Modifier.offset{IntOffset(tickerX.roundToInt(), tickerY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> tickerX+=d.x; tickerY+=d.y}}.fillMaxWidth().background(Color.Yellow).padding(8.dp)){ Text("Ticker: $ticker", color=Color.Black) }
            }
            Column(Modifier.align(Alignment.TopEnd).padding(10.dp)){ Text(if(isLive)"● LIVE" else "○ NOT LIVE", color=if(isLive) Color.Red else Color.White, fontWeight=FontWeight.Bold); Text("Mic: ${if(isMicOn)"ON" else "MUTED"}", color=Color.White, fontSize=11.sp) }
            Column(Modifier.align(Alignment.BottomCenter).padding(12.dp)){
                if(isLive) Button(onClick={ isLive=false; toast("Live Ended") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("END LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                else Button(onClick={ if(!isUnlocked){ toast("LOCKED - Wait for Admin to Unlock"); screen="WAITING" } else if(!hasPerm) permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) else { isLive=true; checkAdmin(); toast("You are LIVE!") } }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("GO LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={screen="SETUP"}, Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("LIVE SETUP", color=Color.Black, fontWeight=FontWeight.Bold) }; Button(onClick={screen="DASH"}, Modifier.weight(1f)){ Text("BACK") } }
            }
        }
    }
    if(screen=="SETUP"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("LIVE SETUP - ALL SETTINGS HERE", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
            OutlinedTextField(value=channelName, onValueChange={channelName=it}, label={Text("Channel Name")}, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=ticker, onValueChange={ticker=it}, label={Text("Ticker")}, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=verse, onValueChange={verse=it}, label={Text("Verse")}, modifier=Modifier.fillMaxWidth())
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Logo", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showLogo, onCheckedChange={showLogo=it}) }
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Verse", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showVerse, onCheckedChange={showVerse=it}) }
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Ticker", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showTicker, onCheckedChange={showTicker=it}) }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={isFront=true}, Modifier.weight(1f)){ Text("FRONT CAM") }; Button(onClick={isFront=false}, Modifier.weight(1f)){ Text("BACK CAM") } }
            Button(onClick={isMicOn=!isMicOn}, Modifier.fillMaxWidth()){ Text(if(isMicOn)"MIC ON" else "MIC MUTED") }
            Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("ALLOW CAMERA + MIC") }
            Button(onClick={ screen="LIVE"; toast("Setup Saved - Drag items on Main Screen") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("OK - APPLY TO MAIN SCREEN", color=Color.White, fontWeight=FontWeight.Bold) }
            Button(onClick={ screen="LIVE" }, Modifier.fillMaxWidth()){ Text("BACK") }
        }
    }
}
