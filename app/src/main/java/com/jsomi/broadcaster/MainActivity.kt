package com.jsomi.broadcaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var isLive by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.jstv_logo),
                        contentDescription = "JSTV Logo",
                        modifier = Modifier.size(160.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "JEHOVAH SHALOM\nOUTREACH MINISTRIES",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "JSTV - Multi-Camera Live Studio", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { }) { Text(text = "MULTI-CAMERA") }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { }) { Text(text = "REMOTE CONTROL") }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isLive = !isLive },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLive) Color.Red else Color(0xFF1A7A3A)
                        )
                    ) {
                        Text(text = if (isLive) "STOP LIVE" else "GO LIVE")
                    }
                }
            }
        }
    }
}
