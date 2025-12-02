package com.wakala.fakhr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            context?.let {
                // start overlay service
                val svc = Intent(it, OverlayReceiverService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.startForegroundService(svc)
                } else {
                    it.startService(svc)
                }
                // schedule periodic upload & train every 6 hours
                val work = PeriodicWorkRequestBuilder<com.wakala.fakhr.UploadAndTrainWorker>(6, TimeUnit.HOURS).build()
                WorkManager.getInstance(it).enqueue(work)
            }
        }
    }
}
