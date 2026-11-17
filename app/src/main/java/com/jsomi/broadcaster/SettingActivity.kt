package com.jsomi.broadcaster

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("General Settings", color = Color.White, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(30.dp))

                Button(onClick = { startActivity(Intent(this@SettingActivity, PlanActivity::class.java)) }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Plan") }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { startActivity(Intent(this@SettingActivity, DestinationActivity::class.java)) }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Destination") }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* Live Setup - later */ }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Live Setup") }
            }
        }
    }
}
