package com.jsomi.broadcaster
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { BroadcasterApp() } } } }
@Composable
fun BroadcasterApp(){
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var screen by remember { mutableStateOf("PERMISSION") }
    var role by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var linked by remember { mutableStateOf(false) }
    var isLive by remember { mutableStateOf(false) }
    var liveSeconds by remember { mutableStateOf(0) }
    var showPremium by remember { mutableStateOf(false) }
    var isPremium by remember { mutableStateOf(false) }
    var premiumShown by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("MAIN CAMERA") }
    var cameraSide by remember { mutableStateOf("BACK") }
    var showSettings by remember { mutableStateOf(false) }
    var showInbox by remember { mutableStateOf(false) }
    var inboxUnread by remember { mutableStateOf(2) }
    var streamTitle by remember { mutableStateOf("My Live Show") }
    var channel by remember { mutableStateOf("JSTV Main") }
    var scene by remember { mutableStateOf("Studio") }
    var ticker by remember { mutableStateOf("Welcome to JSTV - Your Voice, Your Channel") }
    var showTicker by remember { mutableStateOf(true) }
    var loopTicker by remember { mutableStateOf(true) }
    var quality by remember { mutableStateOf("HD 720p") }
    var fps by remember { mutableStateOf("30 FPS") }
    var flash by remember { mutableStateOf(false) }
    var zoom by remember { mutableStateOf("1x") }
    var beauty by remember { mutableStateOf(false) }
    var stabilization by remember { mutableStateOf(true) }
    var autoFocus by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var cameraOn by remember { mutableStateOf(true) }
    var introVideo by remember { mutableStateOf("No Intro Selected") }
    var playIntro by remember { mutableStateOf(false) }
    var loopIntro by remember { mutableStateOf(false) }
    var verse by remember { mutableStateOf("John 3:16 - For God so loved the world...") }
    var showVerse by remember { mutableStateOf(true) }
    var showLogo by remember { mutableStateOf(true) }
    var logoText by remember { mutableStateOf("JSTV") }
    var displayText by remember { mutableStateOf("LIVE FROM JSTV STUDIO") }
    var showDisplayText by remember { mutableStateOf(true) }
    var saveAfter by remember { mutableStateOf(true) }
    var ytOn by remember { mutableStateOf(true) }
    var fbOn by remember { mutableStateOf(false) }
    var tkOn by remember { mutableStateOf(false) }
    var customOn by remember { mutableStateOf(false) }
    var ytUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2") }
    var fbUrl by remember { mutableStateOf("rtmps://live-api-s.facebook.com:443/rtmp/") }
    var tkUrl by remember { mutableStateOf("rtmp://push.tiktok.com/live/") }
    var customUrl by remember { mutableStateOf("") }
    var ytKey by remember { mutableStateOf("") }
    var fbKey by remember { mutableStateOf("") }
    var tkKey by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var inboxMsgs by remember { mutableStateOf(mutableListOf("👑 ADMIN: Welcome to JSTV! Your Room JSTV-6638 is LINKED ✅" to "10:00 AM","👑 ADMIN: Reminder - GO LIVE needs Premium after 30 sec FREE trial. Get code on WhatsApp 0763052911" to "10:05 AM","👑 ADMIN: New Offer - 50% OFF Monthly! Code in ADMIN Offer tab" to "10:10 AM")) }
    var permMsg by remember { mutableStateOf("Allow all 5") }
    var permTick by remember { mutableStateOf(0) }
    fun hasPerm(p:String)=ContextCompat.checkSelfPermission(ctx,p)==PackageManager.PERMISSION_GRANTED
    fun permList():Array<String>{ val l = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO); if(Build.VERSION.SDK_INT>=33){ l.add(Manifest.permission.READ_MEDIA_VIDEO); l.add(Manifest.permission.READ_MEDIA_IMAGES); l.add(Manifest.permission.READ_MEDIA_AUDIO) } else { l.add(Manifest.permission.READ_EXTERNAL_STORAGE); l.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) }; return l.toTypedArray() }
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ res -> permTick++; permMsg = if(res.values.all{ it }) "✅ ALL GRANTED" else "⚠️ Some denied" }
    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri -> if(uri!=null){ introVideo="Intro: ${uri.lastPathSegment}"; playIntro=true; Toast.makeText(ctx,"Intro Added ✅",Toast.LENGTH_SHORT).show() } }
    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun wa(msg:String){ val i=Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text="+Uri.encode(msg))); try{ctx.startActivity(i)}catch(e:Exception){} }
    val timeNow = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
    LaunchedEffect(isLive){ if(isLive){ while(isLive){ delay(1000); liveSeconds++; if(liveSeconds>=30 && !isPremium && !premiumShown){ premiumShown=true; showPremium=true; toast("30 SEC FREE DONE - Premium Needed") } } } }
    fun liveTime():String{ val h=liveSeconds/3600; val m=(liveSeconds%3600)/60; val s=liveSeconds%60; return String.format("%02d:%02d:%02d", h,m,s) }
    @Composable fun RealCamera(){
        if(!cameraOn || source=="SCREEN"){ Box(Modifier.fillMaxWidth().height(280.dp).background(Color(0xFF222222)), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text(if(source=="SCREEN") "🖥️ SCREEN SHARE" else "📷 CAMERA OFF", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold); Text("Title: $streamTitle", color=Color.White, fontSize=12.sp); Text("Channel: $channel | $scene", color=Color.Gray, fontSize=11.sp) } }; return }
        Box(Modifier.fillMaxWidth().height(280.dp).background(Color.Black)){
            AndroidView(factory={ c -> val pv = PreviewView(c); val pf = ProcessCameraProvider.getInstance(c); pf.addListener({ val p = pf.get(); val pr = Preview.Builder().build().also{ it.setSurfaceProvider(pv.surfaceProvider) }; val sel = if(cameraSide=="BACK") CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA; try{ p.unbindAll(); p.bindToLifecycle(lifecycleOwner, sel, pr) }catch(e:Exception){} }, ContextCompat.getMainExecutor(c)); pv }, modifier=Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize().padding(8.dp)){ Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text(if(isLive) "● LIVE ${liveTime()}" else "○ PREVIEW - NOT LIVE YET", color=if(isLive) Color.Red else Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold); Text(if(isLive && liveSeconds<30) "FREE: ${30-liveSeconds}s LEFT" else if(isLive) "PREMIUM ✅" else "SETUP BEFORE LIVE", color=Color.Yellow, fontSize=11.sp, fontWeight=FontWeight.Bold) }; Spacer(Modifier.weight(1f)); if(showLogo) Text(logoText, color=Color(0xFF00E5FF), fontSize=20.sp, fontWeight=FontWeight.Bold); if(showDisplayText) Text(displayText, color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold); if(showVerse) Text("📖 $verse", color=Color.Yellow, fontSize=11.sp); if(showTicker) Text("↔ $ticker", color=Color(0xFF00E5FF), fontSize=11.sp) }
        }
    }
    @Composable fun RealCameraFullScreen(){
        if(!cameraOn){ Box(Modifier.fillMaxSize().background(Color(0xFF222222)), contentAlignment=Alignment.Center){ Text("📷 CAMERA OFF", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold) }; return }
        AndroidView(factory={ c -> val pv = PreviewView(c); val pf = ProcessCameraProvider.getInstance(c); pf.addListener({ val p = pf.get(); val pr = Preview.Builder().build().also{ it.setSurfaceProvider(pv.surfaceProvider) }; val sel = if(cameraSide=="BACK") CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA; try{ p.unbindAll(); p.bindToLifecycle(lifecycleOwner, sel, pr) }catch(e:Exception){} }, ContextCompat.getMainExecutor(c)); pv }, modifier=Modifier.fillMaxSize())
    }
    // ===== FULL SCREEN LIVE - ONLY VIDEO + END LIVE =====
    if(isLive){
        Box(Modifier.fillMaxSize().background(Color.Black)){
            RealCameraFullScreen()
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.SpaceBetween){ Text("● LIVE ${liveTime()}", color=Color.Red, fontSize=16.sp, fontWeight=FontWeight.Bold, modifier=Modifier.background(Color.Black.copy(alpha=0.5f)).padding(6.dp)); Text(if(liveSeconds<30) "FREE ${30-liveSeconds}s LEFT" else if(isPremium) "PREMIUM ✅" else "PREMIUM NEEDED", color=Color.Yellow, fontSize=12.sp, fontWeight=FontWeight.Bold, modifier=Modifier.background(Color.Black.copy(alpha=0.5f)).padding(6.dp)) }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom=90.dp, start=12.dp, end=12.dp)){ if(showLogo) Text(logoText, color=Color(0xFF00E5FF), fontSize=20.sp, fontWeight=FontWeight.Bold); if(showDisplayText) Text(displayText, color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold); if(showVerse) Text("📖 $verse", color=Color.Yellow, fontSize=11.sp); if(showTicker) Text("↔ $ticker", color=Color(0xFF00E5FF), fontSize=11.sp) }
            Button(onClick={ isLive=false; liveSeconds=0; premiumShown=false; toast(if(saveAfter) "Live Ended - Video Saved ✅" else "Live Ended") }, modifier=Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.Red)){ Text("END LIVE", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold) }
        }
        if(showPremium){ Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.8f)), contentAlignment=Alignment.Center){ Column(Modifier.fillMaxWidth().background(Color(0xFF444444)).padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){ Text("⏰ 30 SEC FREE DONE!", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold); OutlinedTextField(value=code, onValueChange={code=it.uppercase()}, label={Text("Code - JSTV-200-XXXX")}, modifier=Modifier.fillMaxWidth()); Button(onClick={ if(code.startsWith("JSTV-")){ isPremium=true; showPremium=false; toast("Premium ✅ Continue LIVE!") } else toast("Wrong Code") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("UNLOCK & CONTINUE LIVE", color=Color.Black, fontWeight=FontWeight.Bold) }; Button(onClick={ wa("Hi ADMIN, my 30 sec free ended, I want Premium") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP FOR CODE", color=Color.White) }; Button(onClick={ showPremium=false }, modifier=Modifier.fillMaxWidth()){ Text("CLOSE") } } } }
        return
    }
    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
        if(screen=="PERMISSION"){
            Box(Modifier.fillMaxWidth().background(Color(0xFF00CFFF)).padding(20.dp), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("JSTV", color=Color.Black, fontSize=42.sp, fontWeight=FontWeight.Bold); Text("BROADCASTER STUDIO", color=Color.Black, fontSize=18.sp, fontWeight=FontWeight.Bold) } }
            Spacer(Modifier.height(16.dp)); Text("🔐 PERMISSIONS", color=Color.Yellow, fontSize=20.sp, fontWeight=FontWeight.Bold); Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().background(Color(0xFF222222)).padding(16.dp)){ Column{ Text("📷 Camera: ${if(hasPerm(Manifest.permission.CAMERA)) "✅" else "❌"}", color=Color.White, fontSize=13.sp); Spacer(Modifier.height(6.dp)); Text("🔊 Audio: ${if(hasPerm(Manifest.permission.RECORD_AUDIO)) "✅" else "❌"}", color=Color.White, fontSize=13.sp); Spacer(Modifier.height(6.dp)); Text("📁 File: ${if(if(Build.VERSION.SDK_INT>=33) hasPerm(Manifest.permission.READ_MEDIA_VIDEO) else hasPerm(Manifest.permission.READ_EXTERNAL_STORAGE)) "✅" else "❌"}", color=Color.White, fontSize=13.sp); Text(permMsg, color=Color.Yellow, fontSize=12.sp) } }
            Spacer(Modifier.height(16.dp)); Button(onClick={ permLauncher.launch(permList()) }, modifier=Modifier.fillMaxWidth().height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))){ Text("ALLOW ALL PERMISSIONS", color=Color.Black, fontWeight=FontWeight.Bold) }; Spacer(Modifier.height(10.dp)); Button(onClick={ ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null))) }, modifier=Modifier.fillMaxWidth()){ Text("OPEN SETTINGS") }; Spacer(Modifier.height(20.dp)); Button(onClick={ screen="FIRST" }, modifier=Modifier.fillMaxWidth().height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E600))){ Text("CONTINUE → CHOOSE ROLE", color=Color.White, fontWeight=FontWeight.Bold) }
        }
        if(screen=="FIRST"){
            Box(Modifier.fillMaxWidth().border(4.dp, Color(0xFF00FF00)).background(Color(0xFF00CFFF)).padding(20.dp), contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("JSTV", color=Color.Black, fontSize=48.sp, fontWeight=FontWeight.Bold); Text("BROADCASTER STUDIO", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("CYAN + GREEN", color=Color.Black, fontSize=14.sp) } }
            Spacer(Modifier.height(10.dp)); Text("CYAN + GREEN = BROADCASTER STUDIO", color=Color(0xFF00E5FF), fontSize=14.sp, fontWeight=FontWeight.Bold); Spacer(Modifier.height(14.dp)); Text("Time: $timeNow", color=Color.Gray, fontSize=14.sp); Text("Choose Your Phone Role:", color=Color.White, fontSize=20.sp); Spacer(Modifier.height(14.dp))
            Button(onClick={ role="MAIN STUDIO"; screen="MAIN" }, modifier=Modifier.fillMaxWidth().height(72.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00CFFF))){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("MAIN STUDIO", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("Goes LIVE - Controlled by ADMIN", color=Color.Black, fontSize=12.sp) } }; Spacer(Modifier.height(10.dp))
            Button(onClick={ role="REMOTE CONTROL"; screen="REMOTE" }, modifier=Modifier.fillMaxWidth().height(72.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00FF00))){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("REMOTE CONTROL", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("Control Main From Far - FULL LIVE CONTROL", color=Color.Black, fontSize=12.sp) } }; Spacer(Modifier.height(10.dp))
            Button(onClick={ role="WIRELESS CAMERA"; screen="WIRELESS" }, modifier=Modifier.fillMaxWidth().height(72.dp), colors=ButtonDefaults.buttonColors(containerColor=Color.White)){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("WIRELESS CAMERA", color=Color.Black, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("2nd Phone As Camera", color=Color.Black, fontSize=12.sp) } }; Spacer(Modifier.height(20.dp))
            Text("LINK TO JSTV STUDIO ADMIN (RED LOGO)", color=Color.Yellow, fontSize=15.sp, fontWeight=FontWeight.Bold); Spacer(Modifier.height(8.dp)); OutlinedTextField(value=roomCode, onValueChange={roomCode=it.uppercase()}, label={Text("Room Code from ADMIN - e.g. JSTV-2026")}, modifier=Modifier.fillMaxWidth()); Button(onClick={ if(roomCode.startsWith("JSTV-")){ linked=true; toast("LINKED - $roomCode") } else toast("Enter Room Code") }, modifier=Modifier.fillMaxWidth().height(52.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00CFFF))){ Text(if(linked) "LINKED - $roomCode ✅" else "LINK TO STUDIO ADMIN", color=Color.Black, fontWeight=FontWeight.Bold) }; Spacer(Modifier.height(14.dp)); Button(onClick={ wa("Hi ADMIN, I want to link Broadcaster.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF444444))){ Text("WHATSAPP ADMIN - 0763052911", color=Color.White) }
        }
        if(screen=="MAIN" || screen=="REMOTE"){
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text(if(screen=="MAIN") "MAIN STUDIO" else "REMOTE CONTROL", color=if(screen=="MAIN") Color(0xFF00E5FF) else Color(0xFF00FF00), fontSize=19.sp, fontWeight=FontWeight.Bold); Button(onClick={ screen="FIRST" }){ Text("← BACK") } }
            Text("Role: $role | ${if(linked) "LINKED - $roomCode ✅" else "NOT LINKED"}", color=if(linked) Color.Green else Color.Yellow, fontSize=12.sp); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ showInbox=!showInbox; if(showInbox) inboxUnread=0 }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(inboxUnread>0) Color.Yellow else Color(0xFF444444))){ Text(if(inboxUnread>0) "📩 INBOX ($inboxUnread) NEW!" else "📩 INBOX", color=if(inboxUnread>0) Color.Black else Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold) }; Button(onClick={ showSettings=!showSettings }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7B61FF))){ Text(if(showSettings) "❌ CLOSE SETTINGS" else "⚙️ OPEN SETTINGS", color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold) } }; Spacer(Modifier.height(10.dp))
            if(showInbox){ Box(Modifier.fillMaxWidth().background(Color(0xFF222222)).padding(12.dp)){ Column{ Text("📩 INBOX - MESSAGES FROM ADMIN", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold); Spacer(Modifier.height(8.dp)); inboxMsgs.forEach{ (msg, time) -> Box(Modifier.fillMaxWidth().background(Color(0xFF444444)).padding(10.dp)){ Column{ Text(msg, color=Color.White, fontSize=12.sp); Text(time, color=Color.Gray, fontSize=10.sp) } }; Spacer(Modifier.height(6.dp)) }; Button(onClick={ wa("Hi ADMIN, I saw your message in INBOX") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("REPLY TO ADMIN ON WHATSAPP", color=Color.White, fontSize=11.sp) } } }; Spacer(Modifier.height(10.dp)) }
            Text("SOURCE - MAIN / REMOTE / SCREEN", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("MAIN CAMERA","REMOTE","SCREEN").forEach{ s -> Button(onClick={ source=s }, modifier=Modifier.weight(1f).height(44.dp), contentPadding=PaddingValues(2.dp), colors=ButtonDefaults.buttonColors(containerColor=if(source==s) Color.Yellow else Color(0xFF444444))){ Text(s, color=if(source==s) Color.Black else Color.White, fontSize=10.sp, fontWeight=FontWeight.Bold) } } }; Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={cameraSide="BACK"}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraSide=="BACK") Color(0xFF00E5FF) else Color(0xFF444444))){ Text("BACK CAMERA", color=if(cameraSide=="BACK") Color.Black else Color.White, fontSize=11.sp) }; Button(onClick={cameraSide="FRONT"}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraSide=="FRONT") Color(0xFF00E5FF) else Color(0xFF444444))){ Text("FRONT CAMERA", color=if(cameraSide=="FRONT") Color.Black else Color.White, fontSize=11.sp) } }; Spacer(Modifier.height(10.dp))
            RealCamera(); Spacer(Modifier.height(8.dp)); Text("👀 You see REAL camera now - Check before GO LIVE", color=Color.Gray, fontSize=11.sp, textAlign=TextAlign.Center); Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={cameraOn=!cameraOn}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraOn) Color(0xFF00E600) else Color.Red)){ Text(if(cameraOn) "CAMERA ON" else "CAMERA OFF", color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold) }; Button(onClick={micOn=!micOn}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(micOn) Color(0xFF00E600) else Color.Red)){ Text(if(micOn) "MIC ON" else "MIC MUTED", color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold) } }; Spacer(Modifier.height(10.dp))
            Button(onClick={ if(!linked){ toast("Link to ADMIN first"); return@Button }; if(ytKey.isBlank() && fbKey.isBlank() && tkKey.isBlank() && customKey.isBlank()){ toast("Open SETTINGS - Enter at least 1 Stream Key"); showSettings=true; return@Button }; isLive=true; liveSeconds=0; premiumShown=false }, modifier=Modifier.fillMaxWidth().height(60.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E600))){ Text("GO LIVE - 30 SEC FREE", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold) }
            Text("After GO LIVE, FULL SCREEN - Only Video + END LIVE. After 30 sec, Premium will ask.", color=Color.Gray, fontSize=11.sp); Spacer(Modifier.height(12.dp))
            if(showSettings){
                Text("⚙️ ALL SETTINGS - INSIDE", color=Color.Yellow, fontSize=14.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=roomCode, onValueChange={roomCode=it.uppercase()}, label={Text("Room Code")}, modifier=Modifier.fillMaxWidth()); Button(onClick={ if(roomCode.startsWith("JSTV-")){ linked=true; toast("LINKED") } else toast("Enter Room Code") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00CFFF))){ Text(if(linked) "LINKED - $roomCode" else "LINK TO ADMIN", color=Color.Black, fontWeight=FontWeight.Bold) }; Spacer(Modifier.height(10.dp))
                Text("1) TITLE, CHANNEL, SCENE", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=streamTitle, onValueChange={streamTitle=it}, label={Text("Stream Title")}, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("JSTV Main","JSTV Sports","JSTV Music","JSTV News","JSTV Church").forEach{ c -> Button(onClick={channel=c}, modifier=Modifier.weight(1f).height(40.dp), contentPadding=PaddingValues(2.dp), colors=ButtonDefaults.buttonColors(containerColor=if(channel==c) Color.Yellow else Color(0xFF444444))){ Text(c, color=if(channel==c) Color.Black else Color.White, fontSize=9.sp) } } }; Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("Studio","Outdoor","Church").forEach{ s -> Button(onClick={scene=s}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(scene==s) Color.Yellow else Color(0xFF444444))){ Text(s, color=if(scene==s) Color.Black else Color.White) } } }; Spacer(Modifier.height(10.dp))
                Text("2) MULTISTREAM", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){ Button(onClick={ytOn=!ytOn}, colors=ButtonDefaults.buttonColors(containerColor=if(ytOn) Color.Red else Color(0xFF444444))){ Text(if(ytOn) "YouTube ON" else "YouTube OFF", fontSize=11.sp) }; Spacer(Modifier.width(6.dp)); Text("YouTube", color=Color.White, fontWeight=FontWeight.Bold) }; OutlinedTextField(value=ytUrl, onValueChange={ytUrl=it}, label={Text("YouTube RTMP URL")}, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=ytKey, onValueChange={ytKey=it}, label={Text("YouTube Key")}, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){ Button(onClick={fbOn=!fbOn}, colors=ButtonDefaults.buttonColors(containerColor=if(fbOn) Color(0xFF1877F2) else Color(0xFF444444))){ Text(if(fbOn) "Facebook ON" else "Facebook OFF", fontSize=11.sp) }; Spacer(Modifier.width(6.dp)); Text("Facebook", color=Color.White, fontWeight=FontWeight.Bold) }; OutlinedTextField(value=fbUrl, onValueChange={fbUrl=it}, label={Text("Facebook RTMP URL")}, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=fbKey, onValueChange={fbKey=it}, label={Text("Facebook Key")}, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){ Button(onClick={tkOn=!tkOn}, colors=ButtonDefaults.buttonColors(containerColor=if(tkOn) Color.Black else Color(0xFF444444))){ Text(if(tkOn) "TikTok ON" else "TikTok OFF", fontSize=11.sp) }; Spacer(Modifier.width(6.dp)); Text("TikTok", color=Color.White, fontWeight=FontWeight.Bold) }; OutlinedTextField(value=tkUrl, onValueChange={tkUrl=it}, label={Text("TikTok RTMP URL")}, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=tkKey, onValueChange={tkKey=it}, label={Text("TikTok Key")}, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){ Button(onClick={customOn=!customOn}, colors=ButtonDefaults.buttonColors(containerColor=if(customOn) Color.Yellow else Color(0xFF444444))){ Text(if(customOn) "Custom ON" else "Custom OFF", color=if(customOn) Color.Black else Color.White, fontSize=11.sp) }; Spacer(Modifier.width(6.dp)); Text("Custom", color=Color.White, fontWeight=FontWeight.Bold) }; OutlinedTextField(value=customUrl, onValueChange={customUrl=it}, label={Text("Custom RTMP URL")}, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=customKey, onValueChange={customKey=it}, label={Text("Custom Key")}, modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp))
                Text("3) CAMERA", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("SD 480p","HD 720p","FHD 1080p").forEach{ q -> Button(onClick={quality=q}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(quality==q) Color.Yellow else Color(0xFF444444))){ Text(q, color=if(quality==q) Color.Black else Color.White, fontSize=10.sp) } } }; Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("24 FPS","30 FPS","60 FPS").forEach{ f -> Button(onClick={fps=f}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(fps==f) Color.Yellow else Color(0xFF444444))){ Text(f, color=if(fps==f) Color.Black else Color.White, fontSize=11.sp) } } }; Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("1x","2x","3x").forEach{ z -> Button(onClick={zoom=z}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(zoom==z) Color.Yellow else Color(0xFF444444))){ Text(z, color=if(zoom==z) Color.Black else Color.White) } } }; Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={flash=!flash}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(flash) Color.Yellow else Color(0xFF444444))){ Text(if(flash) "FLASH ON" else "FLASH OFF", color=if(flash) Color.Black else Color.White, fontSize=11.sp) }; Button(onClick={beauty=!beauty}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(beauty) Color.Yellow else Color(0xFF444444))){ Text(if(beauty) "BEAUTY ON" else "BEAUTY OFF", color=if(beauty) Color.Black else Color.White, fontSize=11.sp) }; Button(onClick={stabilization=!stabilization}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(stabilization) Color.Yellow else Color(0xFF444444))){ Text(if(stabilization) "STABLE ON" else "STABLE OFF", color=if(stabilization) Color.Black else Color.White, fontSize=11.sp) } }; Spacer(Modifier.height(6.dp)); Button(onClick={autoFocus=!autoFocus}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(autoFocus) Color.Yellow else Color(0xFF444444))){ Text(if(autoFocus) "FOCUS AUTO" else "FOCUS LOCK", color=if(autoFocus) Color.Black else Color.White) }; Spacer(Modifier.height(10.dp))
                Text("4) INTRO, TICKER, LOGO, DISPLAY, VERSE", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth()); Text("Intro: $introVideo", color=Color.White, fontSize=12.sp); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={ filePicker.launch("video/*") }, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7B61FF))){ Text("📁 PICK INTRO", color=Color.White, fontSize=10.sp) }; Button(onClick={playIntro=!playIntro}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(playIntro) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(playIntro) "PLAY INTRO ON" else "PLAY INTRO OFF", fontSize=11.sp) }; Button(onClick={loopIntro=!loopIntro}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(loopIntro) Color.Yellow else Color(0xFF444444))){ Text(if(loopIntro) "LOOP ON" else "LOOP OFF", color=if(loopIntro) Color.Black else Color.White, fontSize=11.sp) } }; Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=ticker, onValueChange={ticker=it}, label={Text("Scrolling Text")}, modifier=Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={showTicker=!showTicker}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(showTicker) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(showTicker) "TICKER SHOW" else "TICKER HIDE", fontSize=11.sp) }; Button(onClick={loopTicker=!loopTicker}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(loopTicker) Color.Yellow else Color(0xFF444444))){ Text(if(loopTicker) "LOOP TICKER ON" else "LOOP TICKER OFF", color=if(loopTicker) Color.Black else Color.White, fontSize=11.sp) } }; Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=logoText, onValueChange={logoText=it}, label={Text("Logo Text")}, modifier=Modifier.fillMaxWidth()); Button(onClick={showLogo=!showLogo}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(showLogo) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(showLogo) "LOGO SHOW - $logoText" else "LOGO HIDE") }; Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=displayText, onValueChange={displayText=it}, label={Text("Display Text")}, modifier=Modifier.fillMaxWidth()); Button(onClick={showDisplayText=!showDisplayText}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(showDisplayText) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(showDisplayText) "DISPLAY SHOW" else "DISPLAY HIDE") }; Spacer(Modifier.height(8.dp))
                OutlinedTextField(value=verse, onValueChange={verse=it}, label={Text("Bible Verse")}, modifier=Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ Button(onClick={verse="Psalm 23:1 - The Lord is my shepherd"}, modifier=Modifier.weight(1f)){ Text("Psalm 23", fontSize=10.sp) }; Button(onClick={verse="John 3:16 - For God so loved the world..."}, modifier=Modifier.weight(1f)){ Text("John 3:16", fontSize=10.sp) }; Button(onClick={verse="Philippians 4:13 - I can do all things..."}, modifier=Modifier.weight(1f)){ Text("Phil 4:13", fontSize=10.sp) } }; Button(onClick={showVerse=!showVerse}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(showVerse) Color.Yellow else Color(0xFF444444))){ Text(if(showVerse) "VERSE SHOW ✅" else "VERSE HIDE", color=if(showVerse) Color.Black else Color.White) }; Spacer(Modifier.height(10.dp))
                Text("5) SAVE", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold, modifier=Modifier.fillMaxWidth()); Button(onClick={saveAfter=!saveAfter}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(saveAfter) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(saveAfter) "✅ SAVE AFTER - ON" else "SAVE AFTER - OFF") }; Spacer(Modifier.height(8.dp)); Button(onClick={ showSettings=false; toast("Settings Saved - Back to Camera ✅") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E600))){ Text("SAVE & CLOSE SETTINGS - BACK TO CAMERA", color=Color.White, fontWeight=FontWeight.Bold) }
            }
        }
        if(screen=="WIRELESS"){
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("WIRELESS CAMERA", color=Color.Black, fontSize=18.sp, fontWeight=FontWeight.Bold, modifier=Modifier.background(Color.White).padding(4.dp)); Button(onClick={ screen="FIRST" }){ Text("← BACK") } }; Spacer(Modifier.height(10.dp)); OutlinedTextField(value=roomCode, onValueChange={roomCode=it.uppercase()}, label={Text("Room Code - Same as MAIN")}, modifier=Modifier.fillMaxWidth()); Button(onClick={ if(roomCode.startsWith("JSTV-")){ linked=true; toast("CAMERA LINKED") } else toast("Enter Room Code") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.White)){ Text(if(linked) "CAMERA LINKED - $roomCode" else "LINK CAMERA TO ADMIN", color=Color.Black, fontWeight=FontWeight.Bold) }; Spacer(Modifier.height(10.dp)); RealCamera(); Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)){ listOf("BACK","FRONT").forEach{ c -> Button(onClick={cameraSide=c}, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=if(cameraSide==c) Color.White else Color(0xFF444444))){ Text(c, color=if(cameraSide==c) Color.Black else Color.White) } } }; Spacer(Modifier.height(8.dp)); Button(onClick={cameraOn=!cameraOn}, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(cameraOn) Color(0xFF00E600) else Color(0xFF444444))){ Text(if(cameraOn) "WIRELESS CAMERA ON - Sending to MAIN" else "WIRELESS CAMERA OFF") }
        }
        Spacer(Modifier.height(20.dp))
    }
}
