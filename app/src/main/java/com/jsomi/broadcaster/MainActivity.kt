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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val DB_URL = "https://jstv-studio-default-rtdb.firebaseio.com"
const val ADMIN_WHATSAPP = "0763052911"
fun fbGet(path: String, onDone: (String)->Unit){ Thread{ try{ val c=URL("$DB_URL/$path.json").openConnection() as HttpURLConnection; c.connectTimeout=8000; onDone(c.inputStream.bufferedReader().readText()) }catch(e:Exception){ onDone("") } }.start() }
fun fbPut(path: String, json: String){ Thread{ try{ val c=URL("$DB_URL/$path.json").openConnection() as HttpURLConnection; c.requestMethod="PUT"; c.doOutput=true; c.outputStream.write(json.toByteArray()); c.inputStream.bufferedReader().readText() }catch(e:Exception){} }.start() }
val BIBLE = listOf("John 3:16 - For God so loved the world...","Psalm 23:1 - The LORD is my shepherd, I lack nothing.","Jeremiah 29:11 - For I know the plans I have for you...","Philippians 4:13 - I can do all things through Christ...","Romans 8:28 - All things work together for good...","Isaiah 41:10 - Do not fear, for I am with you...","Matthew 11:28 - Come to me, all who are weary...","Psalm 46:1 - God is our refuge and strength...","Proverbs 3:5 - Trust in the LORD with all your heart...","Joshua 1:9 - Be strong and courageous...","2 Timothy 1:7 - God has not given us a spirit of fear...","Psalm 121:1 - I lift my eyes to the mountains...","Matthew 5:14 - You are the light of the world...","Romans 10:9 - If you confess with your mouth...","John 14:6 - I am the way, the truth, and the life...","Psalm 100:4 - Enter His gates with thanksgiving...","Galatians 5:22 - The fruit of the Spirit is love...","Hebrews 11:1 - Faith is confidence in what we hope for...","1 Corinthians 13:4 - Love is patient, love is kind...","Revelation 21:4 - He will wipe every tear...")
class MainActivity : ComponentActivity(){ override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); setContent{ MaterialTheme{ BroadcasterFinal() } } } }
@Composable fun RealCam(modifier: Modifier=Modifier, isFront: Boolean){ val lc=LocalLifecycleOwner.current; AndroidView(factory={ c-> val pv=PreviewView(c); try{ val f=ProcessCameraProvider.getInstance(c); f.addListener({ try{ val p=f.get(); val pr=Preview.Builder().build().also{it.setSurfaceProvider(pv.surfaceProvider)}; p.unbindAll(); p.bindToLifecycle(lc, if(isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, pr)}catch(e:Exception){} }, ContextCompat.getMainExecutor(c))}catch(e:Exception){}; pv }, modifier=modifier) }
// VISIBLE TYPING - WHITE BOX, BLACK BIG TEXT, YELLOW BORDER
@Composable fun VisibleField(value: String, onChange: (String)->Unit, label: String, modifier: Modifier=Modifier, minLines: Int=1){
    OutlinedTextField(value=value, onValueChange=onChange, label={Text(label, color=Color.Black, fontWeight=FontWeight.Bold)}, placeholder={Text(label, color=Color.DarkGray)}, textStyle=androidx.compose.ui.text.TextStyle(color=Color.Black, fontSize=18.sp, fontWeight=FontWeight.Bold), modifier=modifier.background(Color.White, RoundedCornerShape(8.dp)).border(3.dp, Color.Yellow, RoundedCornerShape(8.dp)), minLines=minLines, colors=OutlinedTextFieldDefaults.colors(focusedContainerColor=Color.White, unfocusedContainerColor=Color.White, focusedTextColor=Color.Black, unfocusedTextColor=Color.Black, cursorColor=Color.Black, focusedBorderColor=Color.Yellow, unfocusedBorderColor=Color.Yellow, focusedLabelColor=Color.Black, unfocusedLabelColor=Color.Black, focusedPlaceholderColor=Color.DarkGray, unfocusedPlaceholderColor=Color.DarkGray))
}

@Composable fun BroadcasterFinal(){
    val ctx=LocalContext.current
    var hasPerm by remember{ mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
    val permLauncher=androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ r-> hasPerm=r.values.all{it} }
    var screen by remember{ mutableStateOf("ROLES") }
    var role by remember{ mutableStateOf("") }
    var roomCode by remember{ mutableStateOf("JSTV-6638") }
    var username by remember{ mutableStateOf("")}; var whatsapp by remember{ mutableStateOf("")}; var pin by remember{ mutableStateOf("")}
    var myKey by remember{ mutableStateOf("")}; var myPlan by remember{ mutableStateOf("Monthly")}; var myExpiry by remember{ mutableStateOf("")}; var isUnlocked by remember{ mutableStateOf(false)}; var isBanned by remember{ mutableStateOf(false)}
    var adminMsg by remember{ mutableStateOf("")}; var offerMsg by remember{ mutableStateOf("")}; var ratesText by remember{ mutableStateOf("Daily 200 KES | Monthly 1000 KES | 3 Months 2500 KES | Year 8000 KES")}
    var isLive by remember{ mutableStateOf(false)}; var isFront by remember{ mutableStateOf(true)}; var isMicOn by remember{ mutableStateOf(true)}
    var channelName by remember{ mutableStateOf("JSTV")}; var ticker by remember{ mutableStateOf("Welcome to JSTV")}; var verse by remember{ mutableStateOf("John 3:16")}
    var showTicker by remember{ mutableStateOf(true)}; var showVerse by remember{ mutableStateOf(true)}; var showLogo by remember{ mutableStateOf(true)}; var showBible by remember{ mutableStateOf(false)}; var bibleText by remember{ mutableStateOf("")}
    var logoX by remember{ mutableStateOf(20f)}; var logoY by remember{ mutableStateOf(20f)}; var tickerX by remember{ mutableStateOf(0f)}; var tickerY by remember{ mutableStateOf(400f)}; var verseX by remember{ mutableStateOf(20f)}; var verseY by remember{ mutableStateOf(300f)}; var bibleX by remember{ mutableStateOf(20f)}; var bibleY by remember{ mutableStateOf(200f)}
    var reportText by remember{ mutableStateOf("")}; var remoteMsg by remember{ mutableStateOf("")}; var searchBible by remember{ mutableStateOf("")}
    var chatInput by remember{ mutableStateOf("") }
    var chatDisplay by remember{ mutableStateOf("No messages yet. Type below to talk to Admin.") }
    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun openWhatsApp(num:String,msg:String){ val n=if(num.startsWith("0")) "254"+num.substring(1) else num.replace("+",""); try{ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$n?text="+Uri.encode(msg)))) }catch(e:Exception){ toast("WhatsApp not found") } }
    fun checkSync(){
        fbGet("adminMessage"){ r-> if(r.isNotBlank() && r!="null") adminMsg=r.replace("\"","") }
        fbGet("offer"){ r-> if(r.isNotBlank() && r!="null") offerMsg=r.replace("\"","") }
        fbGet("rates"){ r-> if(r.isNotBlank() && r!="null") ratesText=r.replace("\"","").replace("|","\n") }
        fbGet("chat"){ r-> if(r.isNotBlank() && r!="null") chatDisplay=r.replace("\"","") }
        fbGet("bibleDisplay"){ r-> if(r.isNotBlank() && r!="null" && r!="\"\""){ bibleText=r.replace("\"",""); if(bibleText.isNotBlank()){ showBible=true; verse=bibleText } } }
        fbGet("liveControl"){ r-> if(r.contains("STOP")){ isLive=false }; if(r.contains("MUTE")){ isMicOn=false }; if(r.contains("BAN") && r.contains(username)){ isBanned=true }; if(r.contains("BIBLE_CLEAR")){ showBible=false; bibleText="" } }
        if(myKey.isNotBlank()){ fbGet("users/$myKey"){ r-> try{ if(r.isNotBlank() && r!="null"){ val o=JSONObject(r); isUnlocked=o.optBoolean("isUnlocked"); isBanned=o.optBoolean("banned"); myExpiry=o.optString("expiry"); myPlan=o.optString("plan") } }catch(e:Exception){} } }
    }
    LaunchedEffect(screen){ if(screen=="DASH" || screen=="LIVE" || screen=="TV" || screen=="REMOTE") checkSync() }
    @Composable fun TypingBox(){
        Column(Modifier.fillMaxWidth().background(Color.Yellow).padding(10.dp)){
            Text("💬 LIVE CHAT - TYPE HERE - ADMIN WILL SEE IT", color=Color.Black, fontWeight=FontWeight.Bold, fontSize=14.sp)
            Box(Modifier.fillMaxWidth().background(Color.White).padding(10.dp)){ Text(chatDisplay, color=Color.Black, fontSize=16.sp, fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){
                VisibleField(value=chatInput, onChange={chatInput=it}, label="Type message here...", modifier=Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                Button(onClick={ if(chatInput.isBlank()) toast("Type message first") else { val full="$username: $chatInput"; fbPut("chat", JSONObject.quote(full)); chatDisplay=full; chatInput=""; toast("Sent to Admin!") } }, colors=ButtonDefaults.buttonColors(containerColor=Color.Green), modifier=Modifier.height(56.dp)){ Text("SEND", color=Color.White, fontWeight=FontWeight.Bold, fontSize=16.sp) }
            }
            Button(onClick={ checkSync(); toast("Chat Refreshed") }, Modifier.fillMaxWidth()){ Text("REFRESH CHAT") }
        }
    }
    if(screen=="ROLES"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
            Box(Modifier.fillMaxWidth().background(Color(0xFF00BCD4)).padding(16.dp), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("JSTV", color=Color.Black, fontSize=42.sp, fontWeight=FontWeight.Bold); Text("BROADCASTER STUDIO", color=Color.Black, fontSize=18.sp, fontWeight=FontWeight.Bold); Text("CYAN + GREEN", color=Color.Black, fontSize=12.sp) } }
            Text("CYAN + GREEN = BROADCASTER STUDIO", color=Color.Cyan, fontWeight=FontWeight.Bold, fontSize=12.sp)
            Text("Time: ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())}", color=Color.Gray, fontSize=12.sp)
            Text("Choose Your Phone Role:", color=Color.White, fontSize=18.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick={ role="MAIN"; screen="LOGIN" }, Modifier.fillMaxWidth().height(72.dp), shape=androidx.compose.foundation.shape.RoundedCornerShape(50), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00BCD4))){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("MAIN STUDIO", color=Color.Black, fontSize=22.sp, fontWeight=FontWeight.Bold); Text("Goes LIVE - Controlled by ADMIN", color=Color.Black, fontSize=11.sp) } }
            Spacer(Modifier.height(8.dp))
            Button(onClick={ role="REMOTE"; screen="LOGIN" }, Modifier.fillMaxWidth().height(72.dp), shape=androidx.compose.foundation.shape.RoundedCornerShape(50), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00FF00))){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("REMOTE CONTROL", color=Color.Black, fontSize=22.sp, fontWeight=FontWeight.Bold); Text("Control Main From Far", color=Color.Black, fontSize=11.sp) } }
            Spacer(Modifier.height(8.dp))
            Button(onClick={ role="WIRELESS"; screen="LOGIN" }, Modifier.fillMaxWidth().height(72.dp), shape=androidx.compose.foundation.shape.RoundedCornerShape(50), colors=ButtonDefaults.buttonColors(containerColor=Color.White)){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("WIRELESS CAMERA", color=Color.Black, fontSize=22.sp, fontWeight=FontWeight.Bold); Text("2nd Phone As Camera", color=Color.Black, fontSize=11.sp) } }
            Spacer(Modifier.height(8.dp))
            Button(onClick={ role="TV"; screen="TV" }, Modifier.fillMaxWidth().height(72.dp), shape=androidx.compose.foundation.shape.RoundedCornerShape(50), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("TV / CHURCH SCREEN", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("Big Screen For Church - HDMI / Cast", color=Color.Black, fontSize=11.sp) } }
            Spacer(Modifier.height(12.dp))
            Text("LINK TO JSTV STUDIO ADMIN (RED LOGO)", color=Color.Yellow, fontWeight=FontWeight.Bold, fontSize=13.sp)
            VisibleField(value=roomCode, onChange={roomCode=it}, label="Room Code from ADMIN - e.g. JSTV-2026", modifier=Modifier.fillMaxWidth())
            Button(onClick={ toast("Linked - $roomCode"); fbPut("roomCode", JSONObject.quote(roomCode)) }, Modifier.fillMaxWidth().height(48.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00BCD4))){ Text("LINKED - $roomCode", color=Color.Black, fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Button(onClick={ openWhatsApp(ADMIN_WHATSAPP, "Hi Admin, I need JSTV Room Code") }, colors=ButtonDefaults.buttonColors(containerColor=Color.DarkGray)){ Text("WHATSAPP ADMIN - $ADMIN_WHATSAPP", color=Color.White) }
        }
    }
    if(screen=="LOGIN"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
            Text("JSTV BROADCASTER", color=Color.Yellow, fontSize=26.sp, fontWeight=FontWeight.Bold)
            Text("Role: $role | Room: $roomCode", color=Color.Cyan, fontWeight=FontWeight.Bold)
            Text("Sign Up = Create User + WhatsApp + PIN\nSign In = Username + PIN", color=Color.White, fontSize=12.sp)
            if(adminMsg.isNotBlank()) Text("ADMIN: $adminMsg", color=Color.Cyan, fontWeight=FontWeight.Bold)
            if(offerMsg.isNotBlank()) Text("OFFER: $offerMsg", color=Color.Green, fontWeight=FontWeight.Bold)
            VisibleField(value=username, onChange={username=it}, label="Username - e.g. elinah", modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            VisibleField(value=whatsapp, onChange={whatsapp=it}, label="WhatsApp e.g. 0716...", modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            VisibleField(value=pin, onChange={pin=it}, label="PIN / Password", modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick={ if(username.isBlank() || whatsapp.isBlank() || pin.length<4) toast("Enter Username + WhatsApp + PIN 4+") else { val key=username.lowercase().replace(" ","_"); val j=JSONObject(); j.put("username",username); j.put("whatsapp",whatsapp); j.put("pin",pin); j.put("plan",myPlan); j.put("amount",1000); j.put("expiry","Not set yet"); j.put("isUnlocked",false); j.put("receipt",""); j.put("banned",false); j.put("muted",false); j.put("role",role); j.put("room",roomCode); fbPut("users/$key", j.toString()); myKey=key; toast("Signed Up! Sent to Admin."); screen="WAITING" } }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("SIGN UP - CREATE ACCOUNT", color=Color.White, fontWeight=FontWeight.Bold) }
            Button(onClick={ if(username.isBlank() || pin.isBlank()) toast("Enter Username + PIN") else { val key=username.lowercase().replace(" ","_"); fbGet("users/$key"){ r-> if(r.isBlank() || r=="null") toast("User not found - Sign Up first") else { try{ val o=JSONObject(r); if(o.optString("pin")==pin){ myKey=key; whatsapp=o.optString("whatsapp"); myPlan=o.optString("plan"); myExpiry=o.optString("expiry"); isUnlocked=o.optBoolean("isUnlocked"); isBanned=o.optBoolean("banned"); if(isBanned) toast("BANNED by Admin") else if(!isUnlocked) screen="WAITING" else { if(role=="REMOTE") screen="REMOTE" else if(role=="WIRELESS") screen="WIRELESS" else screen="DASH" } } else toast("Wrong PIN") }catch(e:Exception){ toast("Login error") } } } } }, Modifier.fillMaxWidth().height(54.dp)){ Text("SIGN IN - USERNAME + PIN", fontWeight=FontWeight.Bold) }
            Button(onClick={ screen="ROLES" }, Modifier.fillMaxWidth()){ Text("BACK TO ROLES") }
            Text("RATES:\n$ratesText", color=Color.Yellow, fontSize=12.sp)
            Text("Pay: Paybill 516600 Acc 5600935001", color=Color.Gray, fontSize=11.sp)
        }
    }
    if(screen=="WAITING"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
            Text("WAITING FOR ADMIN", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Hi $username\nRole: $role\nYour details saved in Admin.\nPay then send Receipt.", color=Color.White)
            Button(onClick={ checkSync(); if(isUnlocked && !isBanned){ if(role=="REMOTE") screen="REMOTE" else if(role=="WIRELESS") screen="WIRELESS" else screen="DASH" } else toast("Still locked") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("CHECK IF ADMIN UNLOCKED ME") }
            Button(onClick={ screen="LOGIN" }, Modifier.fillMaxWidth()){ Text("BACK") }
        }
    }
    if(screen=="DASH"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("Welcome $username - $role", color=Color.Yellow, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Text("Status: ${if(isUnlocked)"UNLOCKED until $myExpiry" else "LOCKED"} | Plan: $myPlan | Room: $roomCode", color=if(isUnlocked) Color.Green else Color.Red)
            if(adminMsg.isNotBlank()) Text("ADMIN: $adminMsg", color=Color.Cyan, fontWeight=FontWeight.Bold)
            if(showBible && bibleText.isNotBlank()) Text("BIBLE ON MAIN: $bibleText", color=Color.Yellow, fontWeight=FontWeight.Bold)
            Button(onClick={ screen="LIVE" }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("GO TO LIVE STUDIO", color=Color.White, fontWeight=FontWeight.Bold) }
            Button(onClick={ screen="TV" }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("TV / CHURCH BIG SCREEN", color=Color.Black, fontWeight=FontWeight.Bold) }
            Button(onClick={ screen="REMOTE" }, Modifier.fillMaxWidth()){ Text("OPEN REMOTE CONTROL") }
            TypingBox()
            VisibleField(value=reportText, onChange={reportText=it}, label="Report hate / illegal stream...", modifier=Modifier.fillMaxWidth(), minLines=2)
            Button(onClick={ if(reportText.isNotBlank()){ fbPut("alerts", JSONObject.quote("REPORT from $username: $reportText")); toast("Report sent to Admin"); reportText="" } }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("REPORT TO ADMIN", color=Color.Black) }
            Button(onClick={ screen="ROLES" }, Modifier.fillMaxWidth()){ Text("LOG OUT / CHANGE ROLE") }
        }
    }
    if(screen=="REMOTE"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("REMOTE CONTROL", color=Color.Green, fontSize=26.sp, fontWeight=FontWeight.Bold)
            Text("Controlling MAIN STUDIO - Room: $roomCode", color=Color.White)
            if(adminMsg.isNotBlank()) Text("ADMIN: $adminMsg", color=Color.Cyan)
            TypingBox()
            Button(onClick={ isLive=true; fbPut("liveControl","{\"action\":\"LIVE\"}"); toast("Sent GO LIVE to Main") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("GO LIVE - MAIN", color=Color.White, fontWeight=FontWeight.Bold) }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ fbPut("liveControl","{\"action\":\"MUTE\"}"); toast("Mute sent") }, Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("MUTE", color=Color.Black) }; Button(onClick={ fbPut("liveControl","{\"action\":\"STOP\"}"); isLive=false; toast("Stop sent") }, Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("STOP LIVE") } }
            Button(onClick={ isFront=!isFront; fbPut("liveControl","{\"action\":\"SWITCH_CAM\"}"); toast("Camera switched") }, Modifier.fillMaxWidth()){ Text("SWITCH CAMERA - FRONT / BACK") }
            Spacer(Modifier.height(12.dp))
            Text("BIBLE - TAP TO SHOW ON MAIN STUDIO:", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold)
            VisibleField(value=searchBible, onChange={searchBible=it}, label="Search Bible e.g. John, Psalm...", modifier=Modifier.fillMaxWidth())
            BIBLE.filter{ searchBible.isBlank() || it.contains(searchBible, ignoreCase=true) }.forEach{ v->
                Column(Modifier.fillMaxWidth().background(Color.DarkGray).padding(10.dp)){ Text(v, color=Color.White, fontSize=13.sp); Button(onClick={ bibleText=v; showBible=true; fbPut("bibleDisplay", JSONObject.quote(v)); toast("Bible showing on MAIN + TV!") }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("SHOW THIS ON MAIN STUDIO + TV") } }; Spacer(Modifier.height(6.dp))
            }
            Button(onClick={ showBible=false; bibleText=""; fbPut("bibleDisplay", JSONObject.quote("")); fbPut("liveControl","{\"action\":\"BIBLE_CLEAR\"}"); toast("Bible cleared from Main") }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("CLEAR BIBLE FROM MAIN") }
            Spacer(Modifier.height(10.dp))
            Text("Send Message to Main:", color=Color.Cyan, fontWeight=FontWeight.Bold)
            VisibleField(value=remoteMsg, onChange={remoteMsg=it}, label="Ticker / Message...", modifier=Modifier.fillMaxWidth())
            Button(onClick={ if(remoteMsg.isNotBlank()){ ticker=remoteMsg; fbPut("adminMessage", JSONObject.quote(remoteMsg)); toast("Message sent to Main"); remoteMsg="" } }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Cyan)){ Text("SEND TO MAIN STUDIO") }
            Button(onClick={ screen="TV" }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("OPEN TV / CHURCH SCREEN", color=Color.Black) }
            Button(onClick={ screen="LIVE" }, Modifier.fillMaxWidth()){ Text("GO TO MAIN STUDIO") }
            Button(onClick={ screen="ROLES" }, Modifier.fillMaxWidth()){ Text("BACK TO ROLES") }
        }
    }
    if(screen=="WIRELESS"){
        Box(Modifier.fillMaxSize().background(Color.Black)){
            if(hasPerm) RealCam(Modifier.fillMaxSize(), isFront) else Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }){ Text("ALLOW CAMERA") } }
            Column(Modifier.align(Alignment.TopCenter).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("WIRELESS CAMERA", color=Color.White, fontWeight=FontWeight.Bold, fontSize=20.sp); Text("Room: $roomCode | This phone is Camera 2", color=Color.Yellow); Text("Point this phone at Pastor / Stage", color=Color.White, fontSize=12.sp) }
            Column(Modifier.align(Alignment.BottomCenter).padding(12.dp)){ Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={isFront=!isFront}, Modifier.weight(1f)){ Text("SWITCH CAM") }; Button(onClick={ screen="ROLES" }, Modifier.weight(1f)){ Text("BACK") } } }
        }
    }
    if(screen=="TV"){
        Box(Modifier.fillMaxSize().background(Color.Black)){
            if(hasPerm && isLive) RealCam(Modifier.fillMaxSize(), isFront)
            else Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("JSTV - CHURCH TV", color=Color.Yellow, fontSize=30.sp, fontWeight=FontWeight.Bold); Text("Connect this phone to TV via HDMI / USB-C / Cast", color=Color.White, fontSize=13.sp, textAlign=TextAlign.Center); Text("Or Cast: Swipe Down > Screen Cast > Choose TV", color=Color.Cyan, fontSize=12.sp, textAlign=TextAlign.Center); Spacer(Modifier.height(10.dp)); Button(onClick={ try{ ctx.startActivity(Intent("android.settings.CAST_SETTINGS")) }catch(e:Exception){ toast("Open Settings > Connected Devices > Cast") } }, colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("OPEN CAST TO TV", color=Color.Black, fontWeight=FontWeight.Bold) } } }
            Column(Modifier.align(Alignment.TopCenter).padding(10.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("JSTV LIVE", color=Color.Red, fontSize=28.sp, fontWeight=FontWeight.Bold); if(adminMsg.isNotBlank()) Text(adminMsg, color=Color.White, fontSize=20.sp, textAlign=TextAlign.Center) }
            if(showBible && bibleText.isNotBlank()) Box(Modifier.align(Alignment.Center).fillMaxWidth().padding(20.dp).background(Color.Black).padding(20.dp), contentAlignment=Alignment.Center){ Text("BIBLE: $bibleText", color=Color.Yellow, fontSize=28.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center) }
            if(showTicker) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Yellow).padding(12.dp), contentAlignment=Alignment.Center){ Text(ticker, color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center) }
            Column(Modifier.align(Alignment.BottomEnd).padding(10.dp)){ Button(onClick={ checkSync(); toast("TV Refreshed") }){ Text("REFRESH") }; Button(onClick={ screen="ROLES" }){ Text("EXIT TV MODE") } }
        }
    }
    if(screen=="LIVE"){
        if(isBanned){ Column(Modifier.fillMaxSize().background(Color.Red).padding(20.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("BANNED BY ADMIN", color=Color.White, fontWeight=FontWeight.Bold); Button(onClick={screen="ROLES"}){ Text("BACK") } } }
        else Box(Modifier.fillMaxSize().background(Color.Black)){
            if(hasPerm) RealCam(Modifier.fillMaxSize(), isFront) else Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment=Alignment.Center){ Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }){ Text("ALLOW CAMERA + MIC") } }
            if(hasPerm){
                if(showLogo) Box(Modifier.offset{IntOffset(logoX.roundToInt(), logoY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> logoX+=d.x; logoY+=d.y}}.background(Color.Red).padding(8.dp)){ Text("JSTV $channelName", color=Color.White, fontWeight=FontWeight.Bold) }
                if(showVerse) Box(Modifier.offset{IntOffset(verseX.roundToInt(), verseY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> verseX+=d.x; verseY+=d.y}}.background(Color.Black).padding(8.dp)){ Text("Verse: $verse", color=Color.Yellow) }
                if(showTicker) Box(Modifier.offset{IntOffset(tickerX.roundToInt(), tickerY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> tickerX+=d.x; tickerY+=d.y}}.fillMaxWidth().background(Color.Yellow).padding(8.dp)){ Text("Ticker: $ticker", color=Color.Black) }
                if(showBible && bibleText.isNotBlank()) Box(Modifier.offset{IntOffset(bibleX.roundToInt(), bibleY.roundToInt())}.pointerInput(Unit){detectDragGestures{_,d-> bibleX+=d.x; bibleY+=d.y}}.background(Color.White).padding(12.dp)){ Text("BIBLE: $bibleText", color=Color.Black, fontWeight=FontWeight.Bold, fontSize=14.sp) }
            }
            Column(Modifier.align(Alignment.TopEnd).padding(10.dp)){ Text(if(isLive)"LIVE" else "NOT LIVE", color=if(isLive) Color.Red else Color.White, fontWeight=FontWeight.Bold); Text("Mic: ${if(isMicOn)"ON" else "MUTED"} | Room: $roomCode", color=Color.White, fontSize=11.sp) }
            Column(Modifier.align(Alignment.BottomCenter).padding(12.dp)){
                Box(Modifier.fillMaxWidth().background(Color.Yellow).padding(8.dp)){ Column{ Text("💬 CHAT: $chatDisplay", color=Color.Black, fontSize=14.sp, fontWeight=FontWeight.Bold); Row(verticalAlignment=Alignment.CenterVertically){ VisibleField(value=chatInput, onChange={chatInput=it}, label="Type here...", modifier=Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); Button(onClick={ if(chatInput.isNotBlank()){ val full="$username: $chatInput"; fbPut("chat", JSONObject.quote(full)); chatDisplay=full; chatInput=""; toast("Sent!") } }){ Text("SEND") } } } }
                if(isLive) Button(onClick={ isLive=false; toast("Live Ended") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("END LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                else Button(onClick={ if(!isUnlocked){ toast("LOCKED - Wait for Admin"); screen="WAITING" } else if(!hasPerm) permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) else { isLive=true; checkSync(); toast("You are LIVE!") } }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("GO LIVE", color=Color.White, fontWeight=FontWeight.Bold) }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={screen="SETUP"}, Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("LIVE SETUP", color=Color.Black, fontWeight=FontWeight.Bold) }; Button(onClick={screen="DASH"}, Modifier.weight(1f)){ Text("BACK") } }
            }
        }
    }
    if(screen=="SETUP"){
        Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())){
            Text("LIVE SETUP", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
            VisibleField(value=channelName, onChange={channelName=it}, label="Channel Name", modifier=Modifier.fillMaxWidth())
            VisibleField(value=ticker, onChange={ticker=it}, label="Ticker", modifier=Modifier.fillMaxWidth())
            VisibleField(value=verse, onChange={verse=it}, label="Verse", modifier=Modifier.fillMaxWidth())
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Logo", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showLogo, onCheckedChange={showLogo=it}) }
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Verse", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showVerse, onCheckedChange={showVerse=it}) }
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Ticker", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showTicker, onCheckedChange={showTicker=it}) }
            Row(verticalAlignment=Alignment.CenterVertically){ Text("Show Bible (from Remote)", color=Color.White, modifier=Modifier.weight(1f)); Switch(checked=showBible, onCheckedChange={showBible=it}) }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={isFront=true}, Modifier.weight(1f)){ Text("FRONT CAM") }; Button(onClick={isFront=false}, Modifier.weight(1f)){ Text("BACK CAM") } }
            Button(onClick={isMicOn=!isMicOn}, Modifier.fillMaxWidth()){ Text(if(isMicOn)"MIC ON" else "MIC MUTED") }
            Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("ALLOW CAMERA + MIC") }
            Button(onClick={ screen="LIVE"; toast("Setup Saved - Drag items!") }, Modifier.fillMaxWidth().height(54.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Green)){ Text("OK - APPLY TO MAIN SCREEN", color=Color.White, fontWeight=FontWeight.Bold) }
            Button(onClick={ screen="LIVE" }, Modifier.fillMaxWidth()){ Text("BACK") }
        }
    }
}
