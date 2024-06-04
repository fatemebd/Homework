package com.example.homework

import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.homework.ui.theme.HomeworkTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private val connectivityStatus = mutableStateOf("unknown")

    companion object {
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workRequest = PeriodicWorkRequestBuilder<StatusWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        readLogFile()


        readLogFile()

        connectivityReceiver = ConnectivityReceiver { status ->
            connectivityStatus.value = status
        }

        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)

        setContent {
            HomeworkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        status = connectivityStatus.value,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        if (!checkNotificationPermission()) {
            requestNotificationPermission()
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val connectivityManager =
                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected = activeNetwork?.isConnectedOrConnecting == true

                val status = if (isConnected) "connected" else "disconnected"
                connectivityReceiver.showNotification(this, status)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectivityReceiver)
    }

    private fun readLogFile() {
        val fileName = "log.json"
        val file = File(filesDir, fileName)
        val gson = Gson()
        val logList: MutableList<LogEntry> = if (file.exists()) {
            val existingLogs = file.readText()
            val type = object : TypeToken<MutableList<LogEntry>>() {}.type
            gson.fromJson(existingLogs, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        logList.forEach { logEntry ->
            Log.v("tagA", logEntry.toString())
        }
    }

    private fun readLogFile() {
        val fileName = "log.json"
        val file = File(filesDir, fileName)
        val gson = Gson()
        val logList: MutableList<LogEntry> = if (file.exists()) {
            val existingLogs = file.readText()
            val type = object : TypeToken<MutableList<LogEntry>>() {}.type
            gson.fromJson(existingLogs, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        logList.forEach { logEntry ->
            Log.v("tagB", logEntry.toString())
        }
    }
}

@Composable
fun Greeting(status: String, modifier: Modifier = Modifier) {
    Text(
        text = "Internet is $status",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomeworkTheme {
        Greeting("unknown")
    }
}