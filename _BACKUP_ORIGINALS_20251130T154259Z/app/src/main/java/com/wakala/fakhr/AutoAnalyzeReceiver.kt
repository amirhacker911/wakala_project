package com.wakala.fakhr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AutoAnalyzeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (intent?.action == "com.wakala.fakhr.ACTION_AUTO_ANALYZE") {
                Log.i("AutoAnalyzeReceiver", "Received AUTO_ANALYZE, starting AutoAnalyzerService")
                context?.let {
                    val svc = Intent(it, AutoAnalyzerService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        it.startForegroundService(svc)
                    } else {
                        it.startService(svc)
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
