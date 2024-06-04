package com.example.homework

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConnectivityReceiver(private val updateStatusCallback: (String) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        val status = if (isConnected) "connected" else "disconnected"
        updateStatusCallback(status)

        val statusType = "internet"
        val logEntry = LogEntry(getCurrentTime(), statusType, status)
        saveLogEntry(context, logEntry)

        if (checkNotificationPermission(context)) {
            showNotification(context, status)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun saveLogEntry(context: Context, logEntry: LogEntry) {
        val fileName = "log.json"
        val file = File(context.filesDir, fileName)
        val gson = Gson()
        val logList: MutableList<LogEntry> = if (file.exists()) {
            val existingLogs = file.readText()
            val type = object : TypeToken<MutableList<LogEntry>>() {}.type
            gson.fromJson(existingLogs, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        logList.add(logEntry)
        file.writeText(gson.toJson(logList))
    }

    private fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(context: Context, status: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "connectivity_status_channel"
        val channelName = "Connectivity Status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Connectivity Status")
            .setContentText("Internet is $status")
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .build()

        notificationManager.notify(1, notification)
    }
}