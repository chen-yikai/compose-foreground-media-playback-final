package com.example.compose_pre_0310

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaPlayerService : Service() {
    companion object {
        private var instance: MediaPlayerService? = null
        fun getInstance(): MediaPlayerService? = instance

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying

        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "media_player"
    }

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        initializePlayer()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Media player controls" }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()

        mediaSession = MediaSession.Builder(this, player).build()

        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                updateNotification()
            }
        })
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, MediaPlayerService::class.java).setAction("PLAY")
        val pauseIntent = Intent(this, MediaPlayerService::class.java).setAction("PAUSE")
        val stopIntent = Intent(this, MediaPlayerService::class.java).setAction("STOP")

        val playPendingIntent =
            PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent =
            PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopPendingIntent =
            PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Media Player")
            .setContentText(if (_isPlaying.value) "Playing" else "Paused")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                if (_isPlaying.value) R.drawable.pause else R.drawable.play,
                if (_isPlaying.value) "Pause" else "Play",
                if (_isPlaying.value) pausePendingIntent else playPendingIntent
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1)
            ).build()
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> play()
            "PAUSE" -> pause()
            "STOP" -> stop()
        }
        return START_STICKY
    }

    fun init(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        instance = null
        super.onDestroy()
    }
}