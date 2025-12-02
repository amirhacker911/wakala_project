package com.wakala.fakhr

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import kotlinx.coroutines.*

class AutoAnalyzerService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        val notif = NotificationHelper.buildForegroundNotification(this, "Analyzing in background")
        startForeground(101, notif)

        schedulePeriodicWork()

        coroutineScope.launch {
            while (isActive) {
                // Light periodic tasks can be run here; keep battery impact low.
                delay(60_000) // 1 minute - adjust as needed
            }
        }
    }

    private fun schedulePeriodicWork() {
        val request = PeriodicWorkRequestBuilder<AnalyzerWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "wakala_analyzer",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class AnalyzerWorker(appContext: android.content.Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            // Single analysis pass:
            // 1) perform capture or read queue
            // 2) run OCR via OCRProcessor
            // 3) run model inference via TFLiteModelManager
            // 4) persist via Room and upload via ApiClient
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
