package com.example.compose_pre_0310

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayerController("https://skills-music-api.eliaschen.dev/music/ocean.mp3")
        }
    }
}

@Composable
fun PlayerController(audioUrl: String) {
    val service = MediaPlayerService.getInstance()
    val isPlaying = MediaPlayerService.isPlaying.collectAsState().value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                service?.init(audioUrl)
            }
        ) {
            Text("init")
        }
        Button(
            onClick = {
                if (isPlaying) {
                    service?.pause()
                } else {
                    service?.play()
                }
            }
        ) {
            Text(if (isPlaying) "Pause" else "Play")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                service?.pause()
            }
        ) {
            Text("Stop")
        }
    }
}