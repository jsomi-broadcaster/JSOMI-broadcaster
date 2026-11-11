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

// VIEWERS STUDIO APP - NO ADMIN - WATCH ONLY
// WhatsApp: 0763052911

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ViewersApp()
            }
        }
    }
}

@Composable
fun ViewersApp() {
    val ctx = LocalContext.current
    var screen by remember { mutableStateOf("HOME") }
    var code by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }

    fun toast(m: String) {
        Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()
    }
    fun openWhatsApp(msg: String) {
        val num = "254763052911"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + num + "?text=" + msg))
        try {
            ctx.startActivity(i)
        } catch (e: Exception) {
            toast("WhatsApp not found")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("JSTV", color = Color.Yellow, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text("VIEWERS STUDIO APP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Watch Live", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (screen == "HOME") {
            Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("LIVE NOW", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isPremium) "PREMIUM UNLOCKED - Enjoy Full Live" else "FREE PREVIEW - Enter Code For Full Live", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { screen = "WATCH" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("WATCH LIVE")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("ENTER PREMIUM CODE", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Paste Code - e.g. JSTV-200-XXXX") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (code.isBlank()) {
                    toast("Enter Code First")
                } else if (code.startsWith("JSTV-")) {
                    isPremium = true
                    toast("Premium Unlocked! Welcome")
                } else {
                    toast("Wrong Code - WhatsApp Us")
                }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) {
                Text("UNLOCK PREMIUM", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("PRICES", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Daily 200 | Monthly 1000 | 3 Months 2500 | Year 8000", color = Color.White, fontSize = 13.sp)
            Text("Paybill 516600 Acc 5600935001", color = Color.White, fontSize = 13.sp)
            Text("Send M-PESA, then WhatsApp us your M-PESA Code", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code. I have paid.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                Text("WHATSAPP US FOR CODE - 0763052911", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I need help with my code.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("WHATSAPP - NEED HELP - 0763052911", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Contact Only By WhatsApp - 0763052911", color = Color.Gray, fontSize = 11.sp)
        } else {
            Text("JSTV LIVE", color = Color.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Text(if (isPremium) "LIVE VIDEO HERE - Premium - Full Quality" else "FREE PREVIEW - Enter Code For Full Live", color = Color.White, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (!isPremium) {
                Text("This is Preview. Unlock for Full Live.", color = Color.Yellow, fontSize = 13.sp)
                Button(onClick = { screen = "HOME" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) {
                    Text("ENTER CODE", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { openWhatsApp("Hi JSTV, I want Premium Code.") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                Text("WHATSAPP US - 0763052911")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { screen = "HOME" }, modifier = Modifier.fillMaxWidth()) {
                Text("BACK TO HOME")
            }
        }
    }
}
