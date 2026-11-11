package com.jsomi.broadcaster
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { BroadcasterApp() } } }
}
@Composable
fun BroadcasterApp(){
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("HOME") }
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
    var roomCode by remember { mutableStateOf("") }
    var linked by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("MAIN CAMERA") }
    var cameraSide by remember { mutableStateOf("BACK") }
    var remoteLink by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("YouTube") }
    var rtmpUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2") }
    var streamKey by remember { mutableStateOf("") }
    var viewers by remember { mutableStateOf(0) }
    var likes by remember { mutableStateOf(0) }
    var chatMsg by remember { mutableStateOf("") }
    var chatList by remember { mutableStateOf(listOf("Welcome to JSTV!")) }
    var permMsg by remember { mutableStateOf("Tap ALLOW to enable Camera + Mic") }
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ res -> permMsg = if(res.values.all{ it }) "✅ ALL PERMISSIONS GRANTED" else "⚠️ Allow permissions to Go Live" }
    fun hasPerm(p:String)=ContextCompat.checkSelfPermission(ctx,p)==PackageManager.PERMISSION_GRANTED
    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun wa(msg:String){ val i=Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text="+Uri.encode(msg))); try{ctx.startActivity(i)}catch(e:Exception){} }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
        Text("JSTV", color=Color(0xFF00E5FF), fontSize=42.sp, fontWeight=FontWeight.Bold)
        Text("BROADCASTER", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold)
        Text("Create Your Own Live Channel", color=Color.Gray, fontSize=12.sp)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().background(Color(0xFF222222)).padding(10.dp)){
            Column{
                Text("📷 PERMISSIONS: Camera + Mic", color=Color.Yellow, fontSize=12.sp, fontWeight=FontWeight.Bold)
                Text(permMsg, color=Color.White, fontSize=11.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                    Button(onClick={ permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))){ Text("ALLOW CAMERA + MIC", color=Color.Black, fontSize=11.sp) }
                    Button(onClick={ ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null))) }){ Text("SETTINGS", fontSize=11.sp) }
                }
                Text("Camera: ${if(hasPerm(Manifest.permission.CAMERA)) "✅" else "❌"}  Mic: ${if(hasPerm(Manifest.permission.RECORD_AUDIO)) "✅" else "❌"}", color=Color.Gray, fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("HOME","SETUP","CHANNELS","HELP").forEach{ t -> Button(onClick={screen=t}, modifier=Modifier.weight(1f).height(44.dp), contentPadding=PaddingValues(0.dp), colors=ButtonDefaults.buttonColors(containerColor=if(screen==t) Color(0xFF00E5FF) else Color(0xFF444444))){ Text(t, color=if(screen==t) Color.Black else Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold) } }
        }
        Spacer(Modifier.height(12.dp))
        if(screen=="HOME"){
            Text("SOURCE - MAIN / REMOTE / CAMERA", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf("MAIN CAMERA","REMOTE","SCREEN").forEach{ s -> Button(onClick={source=s}, modifier=Modifier.weight(1f).height(44.dp), contentPadding=PaddingValues(0.dp), colors=ButtonDefaults.buttonColors(containerColor=if(source==s) Color.Yellow else Color(0xFF444444))){ Text(s, color=if(source==s) Color.Black else Color.White, fontSize=11.sp, fontWeight=FontWeight.Bold) } }
            }
            Spacer(Modifier.height(6.dp))
            if(source=="MAIN CAMERA"){
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("BACK","FRONT").forEach{ c -> Button(onClick={cameraSide=c}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraSide==c) Color(0xFF00E5FF) else Color(0xFF444444))){ Text("$c CAMERA", color=if(cameraSide==c) Color.Black else Color.White, fontSize=11.sp) } } }
                Text("Using: Phone $cameraSide Camera - Main Studio", color=Color.Gray, fontSize=11.sp)
            }
            if(source=="REMOTE"){ OutlinedTextField(value=remoteLink, onValueChange={remoteLink=it}, label={Text("Remote Guest Link")}, modifier=Modifier.fillMaxWidth()); Button(onClick={ wa("Join my JSTV Live as Remote Guest: $remoteLink") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("SHARE REMOTE LINK VIA WHATSAPP") } }
            if(source=="SCREEN"){ Text("Screen Share - Show phone screen on Live", color=Color.Gray, fontSize=11.sp) }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF333333)), contentAlignment=Alignment.Center){
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Text(if(isLive) "● ON AIR - LIVE" else "PREVIEW - $source", color=if(isLive) Color.Red else Color.White, fontSize=17.sp, fontWeight=FontWeight.Bold)
                    Text("Channel: $channel", color=Color.White, fontSize=14.sp)
                    Text("Scene: $scene | Source: $source", color=Color.White, fontSize=12.sp)
                    Text("Camera: ${if(cameraOn) "ON ($cameraSide)" else "OFF"} | Mic: ${if(micOn) "ON" else "OFF"}", color=Color.Gray, fontSize=12.sp)
                    Text("Quality: $quality | To: $destination", color=Color.Gray, fontSize=11.sp)
                    if(isLive) Text("👁 $viewers watching  ❤️ $likes likes", color=Color.Yellow, fontSize=12.sp)
                    Text("Ticker: $ticker", color=Color.Yellow, fontSize=11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Stream Title", color=Color.White, fontSize=13.sp, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=streamTitle, onValueChange={streamTitle=it}, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={cameraOn=!cameraOn}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraOn) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(cameraOn) "CAMERA ON" else "CAMERA OFF", fontSize=12.sp) }
                Button(onClick={micOn=!micOn}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(micOn) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(micOn) "MIC ON" else "MIC OFF", fontSize=12.sp) }
            }
            Spacer(Modifier.height(10.dp))
            Text("LINK TO STUDIO ADMIN (RED LOGO)", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold)
            OutlinedTextField(value=roomCode, onValueChange={roomCode=it.uppercase()}, label={Text("Room Code - e.g. JSTV-6638")}, modifier=Modifier.fillMaxWidth())
            Button(onClick={ if(roomCode.startsWith("JSTV-")){ linked=true; toast("LINKED - $roomCode") } else toast("Enter Room Code from ADMIN") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))){ Text(if(linked) "LINKED - $roomCode" else "LINK TO STUDIO ADMIN", color=Color.Black, fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Button(onClick={
                if(!linked){ toast("Link to ADMIN first"); return@Button }
                if(streamKey.isBlank()){ toast("Enter Stream Key in SETUP"); screen="SETUP"; return@Button }
                if(isLive){ isLive=false; toast("Stopped") }
                else if(isPremium){ isLive=true; viewers=12; toast("You Are LIVE - $channel") }
                else showPremium=true
            }, modifier=Modifier.fillMaxWidth().height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=if(isLive) Color.Red else Color(0xFF00E600))){ Text(if(isLive) "STOP LIVE" else "GO LIVE - PREMIUM", color=Color.White, fontSize=17.sp, fontWeight=FontWeight.Bold) }
            Text("Preview is free. GO LIVE needs Premium Code from ADMIN.", color=Color.Gray, fontSize=11.sp)
            if(isLive){
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()){ OutlinedTextField(value=chatMsg, onValueChange={chatMsg=it}, label={Text("Chat...")}, modifier=Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); Button(onClick={ if(chatMsg.isNotBlank()){ chatList=chatList+("You: $chatMsg"); chatMsg="" } }){ Text("SEND") } }
                Button(onClick={likes++}, modifier=Modifier.fillMaxWidth()){ Text("❤️ LIKE - $likes") }
                chatList.takeLast(5).forEach{ Text(it, color=Color.White, fontSize=12.sp) }
            }
        }
        if(screen=="SETUP"){
            Text("SETUP - YOUR STUDIO + STREAMING", color=Color.Yellow, fontSize=17.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("1) WHERE TO STREAM TO? - 3 URL + CUSTOM", color=Color.White, fontSize=13.sp, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf("YouTube","Facebook","TikTok","Custom").forEach{ d -> Button(onClick={ destination=d; rtmpUrl=when(d){ "YouTube"->"rtmp://a.rtmp.youtube.com/live2"; "Facebook"->"rtmps://live-api-s.facebook.com:443/rtmp/"; "TikTok"->"rtmp://push.tiktok.com/live/"; else->"" } }, modifier=Modifier.weight(1f).height(44.dp), contentPadding=PaddingValues(0.dp), colors=ButtonDefaults.buttonColors(containerColor=if(destination==d) Color.Yellow else Color(0xFF444444))){ Text(d, color=if(destination==d) Color.Black else Color.White, fontSize=11.sp) } }
            }
            Spacer(Modifier.height(6.dp))
            Text("YouTube: rtmp://a.rtmp.youtube.com/live2", color=Color.Gray, fontSize=10.sp, modifier=Modifier.fillMaxWidth())
            Text("Facebook: rtmps://live-api-s.facebook.com:443/rtmp/", color=Color.Gray, fontSize=10.sp, modifier=Modifier.fillMaxWidth())
            Text("TikTok: rtmp://push.tiktok.com/live/", color=Color.Gray, fontSize=10.sp, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("2) RTMP URL", color=Color.White, fontSize=13.sp, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=rtmpUrl, onValueChange={rtmpUrl=it}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("3) Stream Key - From $destination", color=Color.White, fontSize=13.sp, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=streamKey, onValueChange={streamKey=it}, label={Text("Paste Stream Key here")}, modifier=Modifier.fillMaxWidth())
            Text("YouTube: YouTube Studio > Go Live > Stream Key | Facebook: Live Producer > Stream Key | TikTok: LIVE Center > Stream Key", color=Color.Gray, fontSize=10.sp)
            Spacer(Modifier.height(10.dp))
            Text("Video Quality", color=Color.White, fontSize=14.sp, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("SD 480p","HD 720p","FHD 1080p").forEach{ q -> Button(onClick={quality=q}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(quality==q) Color.Yellow else Color(0xFF444444))){ Text(q, color=if(quality==q) Color.Black else Color.White, fontSize=11.sp) } } }
            Spacer(Modifier.height(10.dp))
            Text("Scene", color=Color.White, fontSize=14.sp, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("Studio","Outdoor","Church").forEach{ s -> Button(onClick={scene=s}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(scene==s) Color.Yellow else Color(0xFF444444))){ Text(s, color=if(scene==s) Color.Black else Color.White) } } }
            Spacer(Modifier.height(10.dp))
            Text("Ticker Text", color=Color.White, fontSize=14.sp, modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=ticker, onValueChange={ticker=it}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick={ screen="HOME"; toast("Saved - $destination") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("SAVE & BACK TO HOME", color=Color.Black) }
        }
        if(screen=="CHANNELS"){
            Text("YOUR CHANNELS", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold)
            listOf("JSTV Main","JSTV Sports","JSTV Music","JSTV News","JSTV Church","My Channel").forEach{ c -> Spacer(Modifier.height(6.dp)); Button(onClick={ channel=c; screen="HOME" }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(channel==c) Color.Yellow else Color(0xFF444444))){ Text(c, color=if(channel==c) Color.Black else Color.White) } }
        }
        if(screen=="HELP"){
            Text("HELP & CONTACT - ABOUT APP & BROADCASTING", color=Color.Yellow, fontSize=16.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("JSTV BROADCASTER lets you create your own Live Channel.", color=Color.White, fontSize=13.sp)
            Text("1. Allow Camera + Mic  2. Link to ADMIN  3. SETUP: Choose YouTube/Facebook/TikTok/Custom + Paste Stream Key  4. Tap GO LIVE - Premium Code needed", color=Color.Gray, fontSize=12.sp)
            Spacer(Modifier.height(8.dp))
            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color=Color.White, fontSize=13.sp)
            Text("Paybill 516600 Acc 5600935001", color=Color.White, fontSize=13.sp)
            Text("Ask ADMIN for OFFER - Free Trial / 50% Off / Special Offer", color=Color.Yellow, fontSize=12.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick={ wa("Hi JSTV, I want Premium Code / OFFER to Go Live. I have paid.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP US - 0763052911", fontWeight=FontWeight.Bold) }
        }
        if(showPremium){
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().background(Color(0xFF444444)).padding(16.dp)){
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Text("GO LIVE - PREMIUM REQUIRED", color=Color.Yellow, fontSize=16.sp, fontWeight=FontWeight.Bold)
                    Text("No Demo Live. Enter Premium Code OR Offer Code from ADMIN.", color=Color.White, fontSize=13.sp)
                    OutlinedTextField(value=code, onValueChange={code=it.uppercase()}, label={Text("Enter Code - e.g. JSTV-200-XXXX or JSTV-OFFER-XXXX")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={ if(code.startsWith("JSTV-")){ isPremium=true; showPremium=false; isLive=true; viewers=12; toast("Premium Unlocked - LIVE!") } else toast("Wrong Code - WhatsApp ADMIN for OFFER") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("UNLOCK & GO LIVE", color=Color.Black, fontWeight=FontWeight.Bold) }
                    Text("Daily 200 | Monthly 1000 | Paybill 516600 Acc 5600935001", color=Color.White, fontSize=11.sp)
                    Button(onClick={ wa("Hi JSTV, I want Premium Code / OFFER to Go Live.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP FOR CODE / OFFER - 0763052911") }
                    Button(onClick={ showPremium=false }, modifier=Modifier.fillMaxWidth()){ Text("CANCEL") }
                }
            }
        }
    }
}
