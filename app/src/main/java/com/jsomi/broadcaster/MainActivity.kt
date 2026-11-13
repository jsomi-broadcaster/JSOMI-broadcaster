package com.jsomi.broadcaster

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JSTVBroadcasterApp() }
    }
}

@Composable
fun JSTVBroadcasterApp() {
    var screen by remember { mutableStateOf("ROLES") }
    var role by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var waiting by remember { mutableStateOf(false) }

    // LIVE SETUP - ALL OLD
    var channelName by remember { mutableStateOf("JSTV") }
    var ticker by remember { mutableStateOf("Welcome to JSTV") }
    var verse by remember { mutableStateOf("Psalm 23:1 - The LORD is my shepherd, I lack nothing.") }
    var showLogo by remember { mutableStateOf(true) }
    var showVerse by remember { mutableStateOf(true) }
    var showTicker by remember { mutableStateOf(true) }
    var showBible by remember { mutableStateOf(true) }
    var frontCam by remember { mutableStateOf(true) }
    var micOn by remember { mutableStateOf(true) }
    var cameraAllowed by remember { mutableStateOf(false) }

    // 3 STREAMING DESTINATIONS - RETURNED
    var youtubeOn by remember { mutableStateOf(false) }
    var facebookOn by remember { mutableStateOf(false) }
    var tiktokOn by remember { mutableStateOf(false) }
    var youtubeKey by remember { mutableStateOf("") }
    var facebookKey by remember { mutableStateOf("") }
    var tiktokKey by remember { mutableStateOf("") }

    // REMOTE
    var remoteVerse by remember { mutableStateOf("") }
    var remoteTicker by remember { mutableStateOf("") }
    var remoteLogoOn by remember { mutableStateOf(true) }

    // CHAT + TYPING
    var chatList by remember { mutableStateOf(listOf("Welcome to JSTV!", "God bless you")) }
    var chatInput by remember { mutableStateOf("") }
    var typingText by remember { mutableStateOf("") }

    // SUBSCRIPTION + PAYMENT
    var subscribed by remember { mutableStateOf(false) }
    var priceSelected by remember { mutableStateOf("Monthly - KSh 500") }
    var mpesaNumber by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }

    // ADMIN
    var adminMessage by remember { mutableStateOf("") }

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            when (screen) {
                "ROLES" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("JSTV", color = Color.Yellow, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text("Choose Role", color = Color.White, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { role="ADMIN"; screen="LOGIN" }, colors = ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier = Modifier.fillMaxWidth()) { Text("ADMIN") }
                    Button(onClick = { role="BROADCASTER"; screen="LOGIN" }, colors = ButtonDefaults.buttonColors(Color(0xFF00BCD4)), modifier = Modifier.fillMaxWidth()) { Text("BROADCASTER") }
                    Button(onClick = { role="REMOTE"; screen="LOGIN" }, colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) { Text("REMOTE") }
                    Button(onClick = { role="TV"; screen="TV" }, colors = ButtonDefaults.buttonColors(Color(0xFFFFC107)), modifier = Modifier.fillMaxWidth()) { Text("TV / CHURCH") }
                    Button(onClick = { role="VIEWER"; screen="SUBSCRIBE" }, colors = ButtonDefaults.buttonColors(Color.White), modifier = Modifier.fillMaxWidth()) { Text("VIEWER - SUBSCRIBE", color = Color.Black) }
                }
                "LOGIN" -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("LOGIN - $role", color = Color.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value=name, onValueChange={name=it}, label={Text("Name")}, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=code, onValueChange={code=it}, label={Text("Code")}, modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={ if(role=="BROADCASTER") screen="LIVE_SETUP" else if(role=="REMOTE") screen="REMOTE" else if(role=="ADMIN") screen="ADMIN_DASH" else screen="LIVE_SETUP" }, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()) { Text("LOGIN") }
                    Button(onClick={ screen="ROLES" }, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()) { Text("BACK") }
                    if(waiting) Text("Waiting for Admin to allow...", color=Color.Yellow)
                    Button(onClick={ waiting=true }, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()) { Text("REQUEST ACCESS") }
                }
                "LIVE_SETUP" -> Column(Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())) {
                    Text("LIVE SETUP", color=Color.Yellow, fontSize=28.sp, fontWeight=FontWeight.Bold)
                    OutlinedTextField(value=channelName, onValueChange={channelName=it}, label={Text("Channel Name")}, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=ticker, onValueChange={ticker=it}, label={Text("Ticker")}, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=verse, onValueChange={verse=it}, label={Text("Verse")}, modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Show Logo", color=Color.White); Switch(checked=showLogo, onCheckedChange={showLogo=it}) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Show Verse", color=Color.White); Switch(checked=showVerse, onCheckedChange={showVerse=it}) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Show Ticker", color=Color.White); Switch(checked=showTicker, onCheckedChange={showTicker=it}) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Show Bible (from Remote)", color=Color.White); Switch(checked=showBible, onCheckedChange={showBible=it}) }
                    // 3 DESTINATIONS
                    Text("STREAM DESTINATIONS", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("YouTube Live", color=Color.White); Switch(checked=youtubeOn, onCheckedChange={youtubeOn=it}) }
                    OutlinedTextField(value=youtubeKey, onValueChange={youtubeKey=it}, label={Text("YouTube Stream Key")}, modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Facebook Live", color=Color.White); Switch(checked=facebookOn, onCheckedChange={facebookOn=it}) }
                    OutlinedTextField(value=facebookKey, onValueChange={facebookKey=it}, label={Text("Facebook Stream Key")}, modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("TikTok Live", color=Color.White); Switch(checked=tiktokOn, onCheckedChange={tiktokOn=it}) }
                    OutlinedTextField(value=tiktokKey, onValueChange={tiktokKey=it}, label={Text("TikTok Stream Key")}, modifier=Modifier.fillMaxWidth())
                    Text("Streaming to: " + (if(youtubeOn) "YouTube " else "") + (if(facebookOn) "Facebook " else "") + (if(tiktokOn) "TikTok" else ""), color=Color.Green)
                    Row(Modifier.fillMaxWidth()){ Button(onClick={frontCam=true}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.weight(1f).padding(4.dp)){Text("FRONT CAM")} ; Button(onClick={frontCam=false}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.weight(1f).padding(4.dp)){Text("BACK CAM")} }
                    Button(onClick={micOn=!micOn}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text(if(micOn) "MIC ON" else "MIC OFF") }
                    Button(onClick={cameraAllowed=true}, colors=ButtonDefaults.buttonColors(Color.Red), modifier=Modifier.fillMaxWidth()){ Text("ALLOW CAMERA + MIC") }
                    if(cameraAllowed) Text("Camera + Mic Allowed ✓", color=Color.Green)
                    Button(onClick={screen="MAIN_LIVE"}, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()){ Text("OK - APPLY TO MAIN SCREEN") }
                    Button(onClick={screen="ROLES"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
                "MAIN_LIVE" -> Column(Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())) {
                    Text(channelName, color=Color.Yellow, fontSize=30.sp, fontWeight=FontWeight.Bold)
                    if(showLogo && remoteLogoOn) Text("JSTV LOGO", color=Color.Red, fontSize=24.sp, fontWeight=FontWeight.Bold)
                    Box(Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment=Alignment.Center){ Text(if(frontCam) "FRONT CAM - LIVE" else "BACK CAM - LIVE", color=Color.White) }
                    if(showTicker) Text("TICKER: $ticker ${if(remoteTicker.isNotEmpty()) " | $remoteTicker" else ""}", color=Color.Yellow)
                    if(showVerse) Text(verse, color=Color.White, fontSize=18.sp)
                    if(showBible && remoteVerse.isNotEmpty()) Text("BIBLE FROM REMOTE: $remoteVerse", color=Color.Yellow, fontSize=20.sp, fontWeight=FontWeight.Bold)
                    Text("LIVE TO: " + (if(youtubeOn) "YouTube " else "") + (if(facebookOn) "Facebook " else "") + (if(tiktokOn) "TikTok" else ""), color=Color.Green)
                    Text("CHAT", color=Color.Yellow, fontWeight=FontWeight.Bold)
                    chatList.forEach { Text(it, color=Color.White, modifier=Modifier.fillMaxWidth().background(Color(0xFF333333)).padding(6.dp)) }
                    OutlinedTextField(value=chatInput, onValueChange={chatInput=it; typingText=it}, label={Text("Type message - You see as you type")}, modifier=Modifier.fillMaxWidth())
                    Text("Typing: $typingText", color=Color.White)
                    Button(onClick={ if(chatInput.isNotEmpty()){ chatList=chatList+chatInput; chatInput=""; typingText="" } }, colors=ButtonDefaults.buttonColors(Color.Yellow), modifier=Modifier.fillMaxWidth()){ Text("SEND CHAT", color=Color.Black) }
                    // GO LIVE + 2 PAYMENTS - INSIDE - NO EXTRA BLOCK NEEDED
                    Text("GO LIVE - VIEWERS PAY TO WATCH", color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
                    Button(onClick={screen="PAYMENT"}, colors=ButtonDefaults.buttonColors(Color.Red), modifier=Modifier.fillMaxWidth()){ Text("🔴 GO LIVE") }
                    Button(onClick={screen="REMOTE"}, colors=ButtonDefaults.buttonColors(Color(0xFF4CAF50)), modifier=Modifier.fillMaxWidth()){ Text("OPEN REMOTE") }
                    Button(onClick={screen="LIVE_SETUP"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK TO SETUP") }
                    Button(onClick={screen="TV"}, colors=ButtonDefaults.buttonColors(Color(0xFFFFC107)), modifier=Modifier.fillMaxWidth()){ Text("TV VIEW") }
                }
                "PAYMENT" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("PAY TO WATCH LIVE", color=Color.Yellow, fontSize=28.sp, fontWeight=FontWeight.Bold)
                    Text("Choose 1 Payment System", color=Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("SYSTEM 1 - M-PESA", color=Color.Green, fontWeight=FontWeight.Bold, fontSize=20.sp)
                    OutlinedTextField(value=mpesaNumber, onValueChange={mpesaNumber=it}, label={Text("M-Pesa Number - 07XX XXX XXX")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={subscribed=true; screen="TV"}, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()){ Text("PAY WITH M-PESA - KSh 500") }
                    Text("You will get STK Push on your phone", color=Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("SYSTEM 2 - CARD", color=Color.Yellow, fontWeight=FontWeight.Bold, fontSize=20.sp)
                    OutlinedTextField(value=cardNumber, onValueChange={cardNumber=it}, label={Text("Card Number - Visa / Mastercard")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={subscribed=true; screen="TV"}, colors=ButtonDefaults.buttonColors(Color.Yellow), modifier=Modifier.fillMaxWidth()){ Text("PAY WITH CARD - Visa / Mastercard", color=Color.Black) }
                    Text("Secure Payment - Visa / Mastercard", color=Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick={screen="MAIN_LIVE"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
                "REMOTE" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("REMOTE CONTROL", color=Color.Green, fontSize=28.sp, fontWeight=FontWeight.Bold)
                    OutlinedTextField(value=remoteVerse, onValueChange={remoteVerse=it}, label={Text("Send Bible Verse to Live")}, modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(value=remoteTicker, onValueChange={remoteTicker=it}, label={Text("Send Ticker to Live")}, modifier=Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){ Text("Remote Logo ON/OFF", color=Color.White); Switch(checked=remoteLogoOn, onCheckedChange={remoteLogoOn=it}) }
                    Button(onClick={screen="MAIN_LIVE"}, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()){ Text("SEND TO LIVE") }
                    Button(onClick={screen="ROLES"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
                "TV" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally) {
                    Text("JSTV - TV / CHURCH", color=Color.Yellow, fontSize=28.sp, fontWeight=FontWeight.Bold)
                    Box(Modifier.fillMaxWidth().height(250.dp).background(Color.DarkGray), contentAlignment=Alignment.Center){ Text("LIVE VIDEO", color=Color.White) }
                    Text(verse, color=Color.Yellow, fontSize=22.sp, fontWeight=FontWeight.Bold)
                    if(remoteVerse.isNotEmpty()) Text(remoteVerse, color=Color.White, fontSize=20.sp)
                    Text(ticker, color=Color.White)
                    if(subscribed) Text("Paid ✓ - Watching Live", color=Color.Green) else Text("Not Paid - Please Pay", color=Color.Red)
                    Button(onClick={screen="PAYMENT"}, colors=ButtonDefaults.buttonColors(Color.Yellow), modifier=Modifier.fillMaxWidth()){ Text("SUBSCRIBE TO WATCH", color=Color.Black) }
                    Button(onClick={screen="ROLES"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
                "SUBSCRIBE" -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("SUBSCRIBE", color=Color.Yellow, fontSize=28.sp, fontWeight=FontWeight.Bold)
                    Button(onClick={priceSelected="Monthly - KSh 500"}, colors=ButtonDefaults.buttonColors(if(priceSelected.contains("Monthly")) Color.Yellow else Color.Gray), modifier=Modifier.fillMaxWidth()){ Text("Monthly - KSh 500", color=Color.Black) }
                    Button(onClick={priceSelected="Yearly - KSh 5000"}, colors=ButtonDefaults.buttonColors(if(priceSelected.contains("Yearly")) Color.Yellow else Color.Gray), modifier=Modifier.fillMaxWidth()){ Text("Yearly - KSh 5000", color=Color.Black) }
                    Button(onClick={priceSelected="Church - KSh 10000"}, colors=ButtonDefaults.buttonColors(if(priceSelected.contains("Church")) Color.Yellow else Color.Gray), modifier=Modifier.fillMaxWidth()){ Text("Church - KSh 10000", color=Color.Black) }
                    Text("Selected: $priceSelected", color=Color.White)
                    Button(onClick={screen="PAYMENT"}, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()){ Text("PAY NOW - M-PESA / CARD") }
                    if(subscribed) Text("Subscribed ✓", color=Color.Green)
                    Button(onClick={screen="ROLES"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
                "ADMIN_DASH" -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("ADMIN - CONTROL ROOM", color=Color.Yellow, fontSize=26.sp, fontWeight=FontWeight.Bold)
                    Text("Online: Broadcaster, Remote, 12 Viewers", color=Color.White)
                    OutlinedTextField(value=adminMessage, onValueChange={adminMessage=it}, label={Text("Admin Message to All")}, modifier=Modifier.fillMaxWidth())
                    Button(onClick={}, colors=ButtonDefaults.buttonColors(Color.Green), modifier=Modifier.fillMaxWidth()){ Text("ALLOW USER") }
                    Button(onClick={}, colors=ButtonDefaults.buttonColors(Color.Red), modifier=Modifier.fillMaxWidth()){ Text("BLOCK USER") }
                    Button(onClick={screen="LIVE_SETUP"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("GO TO LIVE SETUP") }
                    Button(onClick={screen="ROLES"}, colors=ButtonDefaults.buttonColors(Color(0xFF7B61FF)), modifier=Modifier.fillMaxWidth()){ Text("BACK") }
                }
            }
        }
    }
}
