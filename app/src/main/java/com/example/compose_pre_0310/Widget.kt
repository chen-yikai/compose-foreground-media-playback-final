package com.example.compose_pre_0310

import android.content.Context
import android.provider.MediaStore.Audio.Media
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.Text

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            RootView()
        }
    }

    @Composable
    fun RootView() {
        val service = MediaPlayerService.getInstance()
        val isPlaying = MediaPlayerService.isPlaying.collectAsState().value
        val hasPrepare = MediaPlayerService.hasPrepare.collectAsState().value

        Column(
            GlanceModifier.fillMaxSize().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (hasPrepare) "Ready to play" else "Init first")
            Spacer(GlanceModifier.height(10.dp))
            if (hasPrepare) {
                Button(text = if (isPlaying) "Pause" else "Play", onClick = {
                    service?.toggle()
                })
            } else {
                Button(text = "Init", onClick = {
                    service?.init("https://skills-music-api.eliaschen.dev/music/ocean.mp3")
                })
            }
        }
    }
}

class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = Widget()
}