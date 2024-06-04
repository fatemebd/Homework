package com.example.homework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ConnectivityService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val status = intent.getStringExtra("status")
        showNotification(status ?: "unknown")
        return START_NOT_STICKY
    }

    private fun showNotification(status: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "connectivity_status_channel"
        val channelName = "Connectivity Status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Connectivity Status")
            .setContentText("Internet is $status")
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
