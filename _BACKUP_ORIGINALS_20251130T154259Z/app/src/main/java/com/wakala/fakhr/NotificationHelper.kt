package com.wakala.fakhr

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "wakala_foreground_channel"
    const val CHANNEL_NAME = "Background Analyzer"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            ch.setShowBadge(false)
            nm.createNotificationChannel(ch)
        }
    }

    fun buildForegroundNotification(context: Context, contentText: String): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Wakala Analyzer")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
