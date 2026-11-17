package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var show by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(2000) // 2 seconds logo
                startActivity(Intent(this@WelcomeActivity, BroadcastActivity::class.java))
                finish()
            }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // YOUR OLD LOGO - DO NOT CHANGE THIS NAME
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "JSOMI Logo",
                    modifier = Modifier.size(220.dp)
                )
            }
        }
    }
}
