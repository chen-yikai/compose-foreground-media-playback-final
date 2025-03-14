package com.example.compose_pre_0310

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    private fun createNotification() {
        val channel = NotificationChannel(
            "media_player",
            "Media PlayBack",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val serviceIntent = Intent(this, MediaPlayerService::class.java)
        startForegroundService(serviceIntent)
    }
}