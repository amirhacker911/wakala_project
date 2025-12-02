package com.wakala.fakhr

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * AutoRoundWatcher
 *
 * Periodically inspects the latest OCR-parsed timer value saved by OCRProcessor or AnalyzerWorker.
 * When it detects a new round starting (timer resets to close-to-29), it broadcasts an intent to start analysis.
 *
 * It is resilient to transient OCR errors by requiring a stable pattern:
 * - detects a "timer jump" from small value (<=5) to large value (>=25) which indicates a new round just started
 *
 * Place this Service in Manifest and start it on app boot or on MainActivity startup.
 */
class AutoRoundWatcher : Service() {

    private val TAG = "AutoRoundWatcher"
    private val handler = Handler(Looper.getMainLooper())
    private var lastSeenTimer: Int? = null
    private var lastTriggerTs: Long = 0L

    private val loop = object : Runnable {
        override fun run() {
            try {
                checkTimerAndTrigger()
            } catch (e: Exception) {
                Log.e(TAG, "check failed", e)
            } finally {
                handler.postDelayed(this, 1000) // check every second
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(loop)
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkTimerAndTrigger() {
        val prefs = getSharedPreferences("wakala_prefs", Context.MODE_PRIVATE)
        // OCRProcessor or AnalyzerWorker should write last_timer_seconds to prefs when parsing screen
        val timer = prefs.getInt("last_timer_seconds", -1)
        if (timer == -1) return

        val prev = lastSeenTimer
        lastSeenTimer = timer
        val now = System.currentTimeMillis()

        // condition: previous small (<=5) then current large (>=25) -> new round started
        if (prev != null) {
            if (prev <= 5 && timer >= 25) {
                // avoid duplicate triggers within same minute
                if (now - lastTriggerTs > 2500) {
                    lastTriggerTs = now
                    triggerAnalysis()
                }
            }
        }
    }

    private fun triggerAnalysis() {
        Log.i(TAG, "Detected new round — triggering auto analysis")
        // broadcast intent consumed by AutoAnalyzerService / AnalyzerWorker to kick analysis
        val b = Intent("com.wakala.fakhr.ACTION_AUTO_ANALYZE")
        b.setPackage(packageName)
        sendBroadcast(b)
    }
}
