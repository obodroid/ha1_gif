package com.obodroid.kaitomm.gifplayer.common.extensions

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

inline fun <reified T : Service> Context.runService(action: String? = null, block: Intent.() -> Unit = {}) {
    Intent(this, T::class.java)
        .apply {
            action?.let { this.action = it }
            block()
        }
        .let { this.startService(it) }
}

inline fun <reified T : Service> Context.stopService() {
    stopService(Intent(this, T::class.java))
}

fun Context.broadcast(action: String, receiverForeground: Boolean = false, block: Intent.() -> Unit = {}) {
    Intent()
        .apply {
            this.action = action
            block()
            if (receiverForeground) {
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
        }
        .let { sendBroadcast(it) }
}

fun Context.localBroadcast(action: String, block: Intent.() -> Unit = {}) {
    Intent(action)
        .apply {
            block()
        }
        .let {
            LocalBroadcastManager.getInstance(this).sendBroadcast(it)
        }
}

fun Context.registerReceiverFilter(receiver: BroadcastReceiver, actions: List<String>?) {
    IntentFilter()
        .apply {
            actions?.forEach { addAction(it) }
        }
        .let {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, it)
        }
}

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    Intent(this, T::class.java)
        .apply {
            block()
        }
        .let { startActivity(it) }
}

inline fun <reified T> Context.getApplicationSystemService(clazz: Class<T>): T {
    val serviceName = when (clazz) {
        AlarmManager::class.java -> Context.ALARM_SERVICE
        WifiManager::class.java -> Context.WIFI_SERVICE
        NotificationManager::class.java -> Context.NOTIFICATION_SERVICE
        ConnectivityManager::class.java -> Context.CONNECTIVITY_SERVICE
        else -> throw IllegalArgumentException("Unknown clazz $clazz")
    }
    return applicationContext.getSystemService(serviceName) as T
}

fun Context.registerApplicationReceiver(receiver: BroadcastReceiver, block: IntentFilter.() -> Unit = {}) {
    IntentFilter()
        .apply { block() }
        .let { applicationContext.registerReceiver(receiver, it) }
}

fun Context.unregisterApplicationReceiver(receiver: BroadcastReceiver) {
    applicationContext.unregisterReceiver(receiver)
}

fun <T> Context.createBroadcastPendingIntent(clazz: Class<T>, requestCode: Int = 0, flag: Int = 0, block: Intent.() -> Unit = {}): PendingIntent {
    return Intent(this, clazz)
        .apply { block() }
        .let { PendingIntent.getBroadcast(this, requestCode, it, flag) }
}