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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BroadcasterApp() } }
    }
}
@Composable
fun BroadcasterApp() {
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
    // LINK TO ADMIN
    var roomCode by remember { mutableStateOf("") }
    var linked by remember { mutableStateOf(false) }
    // STREAMING SETTINGS - NEW REAL
    var destination by remember { mutableStateOf("YouTube") }
    var rtmpUrl by remember { mutableStateOf("rtmp://a.rtmp.youtube.com/live2") }
    var streamKey by remember { mutableStateOf("") }
    var viewers by remember { mutableStateOf(0) }
    var likes by remember { mutableStateOf(0) }
    var chatMsg by remember { mutableStateOf("") }
    var chatList by remember { mutableStateOf(listOf("Welcome to JSTV!")) }

    fun toast(m:String){ Toast.makeText(ctx,m,Toast.LENGTH_SHORT).show() }
    fun wa(msg:String){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911?text="+Uri.encode(msg)))
        try{ ctx.startActivity(i) }catch(e:Exception){ toast("WhatsApp not found") }
    }
    fun tryGoLive(){
        if(streamKey.isBlank()){ toast("Enter Stream Key first - in SETUP"); screen="SETUP"; return }
        if(!linked){ toast("Link to ADMIN first - Enter Room Code"); return }
        if(isPremium){ isLive=true; viewers=12; toast("You Are LIVE on $destination - $channel") }
        else showPremium=true
    }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally){
        Text("JSTV", color=Color(0xFF00E5FF), fontSize=42.sp, fontWeight=FontWeight.Bold)
        Text("BROADCASTER STUDIO", color=Color.White, fontSize=16.sp, fontWeight=FontWeight.Bold)
        Text("CYAN + GREEN = BROADCASTER", color=Color(0xFF00E5FF), fontSize=11.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
            listOf("HOME","SETUP","CHANNELS","HELP").forEach{ t ->
                Button(onClick={screen=t}, colors=ButtonDefaults.buttonColors(containerColor=if(screen==t) Color(0xFF00E5FF) else Color.DarkGray)){ Text(t, color=if(screen==t) Color.Black else Color.White, fontSize=12.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))

        if(screen=="HOME"){
            Box(Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment=Alignment.Center){
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Text(if(isLive) "● ON AIR - LIVE" else "PREVIEW", color=if(isLive) Color.Red else Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold)
                    Text("Channel: $channel", color=Color.White, fontSize=13.sp)
                    Text("Scene: $scene | $quality", color=Color.White, fontSize=12.sp)
                    Text("Camera: ${if(cameraOn) "ON" else "OFF"} | Mic: ${if(micOn) "ON" else "OFF"}", color=Color.Gray, fontSize=11.sp)
                    if(isLive){ Text("👁 $viewers watching  ❤️ $likes likes", color=Color.Yellow, fontSize=12.sp) }
                    Text("Ticker: $ticker", color=Color.Yellow, fontSize=11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Stream Title", color=Color.White, fontSize=13.sp)
            OutlinedTextField(value=streamTitle, onValueChange={streamTitle=it}, label={Text("e.g. My Live Show")}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
                Button(onClick={cameraOn=!cameraOn}, colors=ButtonDefaults.buttonColors(containerColor=if(cameraOn) Color.Green else Color.DarkGray)){ Text(if(cameraOn) "CAMERA ON" else "CAMERA OFF", fontSize=12.sp) }
                Button(onClick={micOn=!micOn}, colors=ButtonDefaults.buttonColors(containerColor=if(micOn) Color.Green else Color.DarkGray)){ Text(if(micOn) "MIC ON" else "MIC OFF", fontSize=12.sp) }
            }
            Spacer(Modifier.height(10.dp))
            // LINK TO ADMIN - KEEP
            Text("LINK TO JSTV STUDIO ADMIN (RED LOGO)", color=Color.Yellow, fontSize=13.sp, fontWeight=FontWeight.Bold)
            OutlinedTextField(value=roomCode, onValueChange={roomCode=it.uppercase()}, label={Text("Room Code from ADMIN - e.g. JSTV-6638")}, modifier=Modifier.fillMaxWidth())
            Button(onClick={ if(roomCode.startsWith("JSTV-")){ linked=true; toast("LINKED - $roomCode") } else toast("Enter correct Room Code") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))){ Text(if(linked) "LINKED - $roomCode" else "LINK TO STUDIO ADMIN", color=Color.Black, fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Button(onClick={ if(isLive){ isLive=false; toast("Broadcast Stopped") } else tryGoLive() }, modifier=Modifier.fillMaxWidth().height(56.dp), colors=ButtonDefaults.buttonColors(containerColor=if(isLive) Color.Red else Color.Green)){ Text(if(isLive) "STOP LIVE" else "GO LIVE", color=Color.White, fontSize=18.sp, fontWeight=FontWeight.Bold) }
            Text("Try all free. Go Live unlocks with Premium Code.", color=Color.Gray, fontSize=11.sp)
            if(isLive){
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()){
                    OutlinedTextField(value=chatMsg, onValueChange={chatMsg=it}, label={Text("Chat...")}, modifier=Modifier.weight(1f))
                    Button(onClick={ if(chatMsg.isNotBlank()){ chatList=chatList+("You: $chatMsg"); chatMsg="" } }){ Text("SEND") }
                }
                Button(onClick={ likes++; toast("Liked!") }, modifier=Modifier.fillMaxWidth()){ Text("❤️ LIKE - $likes") }
                chatList.takeLast(5).forEach{ Text(it, color=Color.White, fontSize=12.sp) }
            }
        }
        if(screen=="SETUP"){
            Text("SETUP - YOUR STUDIO + STREAMING", color=Color.Yellow, fontSize=16.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("WHERE TO STREAM TO?", color=Color.White, fontSize=13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
                listOf("YouTube","Facebook","TikTok","Custom").forEach{ d ->
                    Button(onClick={ destination=d; rtmpUrl=when(d){ "YouTube"->"rtmp://a.rtmp.youtube.com/live2"; "Facebook"->"rtmps://live-api-s.facebook.com:443/rtmp/"; "TikTok"->"rtmp://push.tiktok.com/live/"; else->"" } }, colors=ButtonDefaults.buttonColors(containerColor=if(destination==d) Color.Yellow else Color.DarkGray)){ Text(d, color=if(destination==d) Color.Black else Color.White, fontSize=11.sp) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("RTMP URL - Streaming Server", color=Color.White, fontSize=13.sp)
            OutlinedTextField(value=rtmpUrl, onValueChange={rtmpUrl=it}, label={Text("RTMP URL")}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("Stream Key - From $destination", color=Color.White, fontSize=13.sp)
            OutlinedTextField(value=streamKey, onValueChange={streamKey=it}, label={Text("Paste Stream Key here")}, modifier=Modifier.fillMaxWidth())
            Text("YouTube: YouTube Studio > Go Live > Stream Key  |  Facebook: Live Producer > Stream Key", color=Color.Gray, fontSize=10.sp)
            Spacer(Modifier.height(10.dp))
            Text("Video Quality", color=Color.White, fontSize=13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
                listOf("SD 480p","HD 720p","FHD 1080p").forEach{ q -> Button(onClick={quality=q}, colors=ButtonDefaults.buttonColors(containerColor=if(quality==q) Color.Yellow else Color.DarkGray)){ Text(q, color=if(quality==q) Color.Black else Color.White, fontSize=11.sp) } }
            }
            Spacer(Modifier.height(8.dp))
            Text("Scene", color=Color.White, fontSize=13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly){
                listOf("Studio","Outdoor","Church").forEach{ s -> Button(onClick={scene=s}, colors=ButtonDefaults.buttonColors(containerColor=if(scene==s) Color.Yellow else Color.DarkGray)){ Text(s, color=if(scene==s) Color.Black else Color.White) } }
            }
            Spacer(Modifier.height(8.dp))
            Text("Ticker Text", color=Color.White, fontSize=13.sp)
            OutlinedTextField(value=ticker, onValueChange={ticker=it}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick={ screen="HOME"; toast("Saved - RTMP: $destination") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("SAVE & BACK TO HOME", color=Color.Black) }
        }
        if(screen=="CHANNELS"){
            Text("YOUR CHANNELS", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold)
            listOf("JSTV Main","JSTV Sports","JSTV Music","JSTV News","JSTV Church","My Channel").forEach{ c ->
                Spacer(Modifier.height(6.dp))
                Button(onClick={ channel=c; screen="HOME" }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=if(channel==c) Color.Yellow else Color.DarkGray)){ Text(c, color=if(channel==c) Color.Black else Color.White) }
            }
        }
        if(screen=="HELP"){
            Text("HELP & CONTACT", color=Color.Yellow, fontSize=18.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color=Color.White, fontSize=13.sp)
            Text("Paybill 516600 Acc 5600935001", color=Color.White, fontSize=13.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick={ wa("Hi JSTV, I want Premium Code to Go Live. I have paid.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP US - 0763052911", fontWeight=FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            Text("Streaming: Get Stream Key from YouTube Studio > Go Live, paste it in SETUP.", color=Color.Gray, fontSize=12.sp)
        }
        if(showPremium){
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)){
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Text("GO LIVE - PREMIUM REQUIRED", color=Color.Yellow, fontSize=16.sp, fontWeight=FontWeight.Bold)
                    Text("Try free. To Go Live, enter Premium Code from ADMIN.", color=Color.White, fontSize=12.sp)
                    OutlinedTextField(value=code, onValueChange={code=it.uppercase()}, label={Text("Enter Code - e.g. JSTV-200-XXXX")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={ if(code.startsWith("JSTV-")){ isPremium=true; showPremium=false; isLive=true; viewers=12; toast("Premium Unlocked - LIVE!") } else toast("Wrong Code") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color.Yellow)){ Text("UNLOCK & GO LIVE", color=Color.Black, fontWeight=FontWeight.Bold) }
                    Text("Daily 200 | Monthly 1000 | Paybill 516600 Acc 5600935001", color=Color.White, fontSize=11.sp)
                    Button(onClick={ wa("Hi JSTV, I want Premium Code to Go Live.") }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF25D366))){ Text("WHATSAPP FOR CODE - 0763052911") }
                    Button(onClick={ showPremium=false }, modifier=Modifier.fillMaxWidth()){ Text("CANCEL") }
                }
            }
        }
    }
}
