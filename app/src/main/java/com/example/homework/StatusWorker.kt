package com.example.homework

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val bluetoothStatus = checkBluetoothStatus()
        val airplaneStatus = checkAirplaneModeStatus()

        val currentTime = getCurrentTime()
        val bluetoothLogEntry = LogEntry(currentTime, "bluetooth", bluetoothStatus)
        val airplaneLogEntry = LogEntry(currentTime, "airplane", airplaneStatus)

        logStatus(bluetoothLogEntry)
        logStatus(airplaneLogEntry)

        saveLogEntry(applicationContext, bluetoothLogEntry)
        saveLogEntry(applicationContext, airplaneLogEntry)

        return Result.success()
    }

    private fun checkBluetoothStatus(): String {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            "connected"
        } else {
            "disconnected"
        }
    }

    private fun checkAirplaneModeStatus(): String {
        return if (Settings.System.getInt(
                applicationContext.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
        ) {
            "connected"
        } else {
            "disconnected"
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun logStatus(logEntry: LogEntry) {
        Log.i("worker_airplane", logEntry.toString())
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
}