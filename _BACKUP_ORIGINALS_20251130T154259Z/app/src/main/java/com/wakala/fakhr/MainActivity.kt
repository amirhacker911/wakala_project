package com.wakala.fakhr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnStartOverlay: Button
    private lateinit var btnRequestCapture: Button

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // basic handling
        val granted = perms.values.all { it }
        Toast.makeText(this, if (granted) "Permissions granted" else "Permissions missing", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        
// start AutoRoundWatcher to monitor round timers and auto-trigger analysis
try {
    val ar = android.content.Intent(this, com.wakala.fakhr.AutoRoundWatcher::class.java)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(ar) else startService(ar)
} catch (e: Exception) { e.printStackTrace() }
// check auth and consent
try {
    val prefs = getSharedPreferences("wakala_prefs", MODE_PRIVATE)
    val token = prefs.getString("auth_token", null)
    val consent = prefs.getBoolean("consent", false)
    if (token == null) {
        startActivity(android.content.Intent(this, LoginActivity::class.java))
        finish(); return
    } else if (!consent) {
        startActivity(android.content.Intent(this, ConsentActivity::class.java))
        finish(); return
    }
} catch (e: Exception) { e.printStackTrace() }
\n\n        // schedule periodic upload & train worker (every 6 hours)\n        try {\n            val work = androidx.work.PeriodicWorkRequestBuilder<com.wakala.fakhr.UploadAndTrainWorker>(6, java.util.concurrent.TimeUnit.HOURS).build()\n            androidx.work.WorkManager.getInstance(this).enqueue(work)\n        } catch (e: Exception) { e.printStackTrace() }\n\n        // add quick access to dataset labeling/upload via intents

        btnStartOverlay = findViewById(R.id.btn_start_overlay)\n        // Additional button for labeling (optional)\n        val btnLabel = findViewById<android.widget.Button?>(R.id.btn_label_dataset)\n        val btnUpload = findViewById<android.widget.Button?>(R.id.btn_upload_dataset)
        btnRequestCapture = findViewById(R.id.btn_request_capture)

        btnRequestCapture.setOnClickListener {
            // launch ScreenCaptureActivity to request MediaProjection and capture a sample
            val i = Intent(this, ScreenCaptureActivity::class.java)
            startActivity(i)
        }

        btnStartOverlay.setOnClickListener {
            // check overlay permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                    Toast.makeText(this, "Allow display over other apps and retry", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            // start overlay service
            val s = Intent(this, OverlayReceiverService::class.java)
            ContextCompat.startForegroundService(this, s)
            Toast.makeText(this, "Overlay started", Toast.LENGTH_SHORT).show()
        }

        // Request runtime permissions commonly needed
        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.FOREGROUND_SERVICE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        requestPermissions.launch(perms.toTypedArray())\n\n        // label button action\n        btnLabel?.setOnClickListener { startActivity(android.content.Intent(this, DataLabelActivity::class.java)) }\n        btnUpload?.setOnClickListener {\n            // Zip dataset and notify user (upload should be done in background)\n            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {\n                val uploader = com.wakala.fakhr.data.TrainingUploader(applicationContext)\n                val zip = uploader.zipDataset()\n                if (zip != null) {\n                    // Attempt upload (best-effort)\n                    val ok = uploader.uploadDatasetFile(zip)\n                    runOnUiThread { android.widget.Toast.makeText(this@MainActivity, if (ok) "Uploaded dataset" else "Upload failed", android.widget.Toast.LENGTH_SHORT).show() }\n                } else {\n                    runOnUiThread { android.widget.Toast.makeText(this@MainActivity, "No dataset found", android.widget.Toast.LENGTH_SHORT).show() }\n                }\n            }\n        }
    }
}
