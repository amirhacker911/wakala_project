package com.wakala.fakhr

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * ModelUpdater - checks server for a new model and downloads it to filesDir/model.tflite
 * Endpoint expected: GET /models/latest.json -> {"name":"model_123.tflite"}
 * and GET /models/<name> to download the file.
 */
class ModelUpdater(private val context: Context, private val baseUrl: String) {

    private val client = OkHttpClient()

    suspend fun checkAndUpdate(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val infoReq = Request.Builder().url("${'$'}baseUrl/models/latest.json").build()
                val infoResp = client.newCall(infoReq).execute()
                if (!infoResp.isSuccessful) return@withContext false
                val body = infoResp.body?.string() ?: return@withContext false
                val name = org.json.JSONObject(body).optString("name", null) ?: return@withContext false
                val local = File(context.filesDir, name)
                if (local.exists()) {
                    // already have it
                    return@withContext true
                }
                val dlReq = Request.Builder().url("${'$'}baseUrl/models/${'$'}name").build()
                val dlResp = client.newCall(dlReq).execute()
                if (!dlResp.isSuccessful) return@withContext false
                val bytes = dlResp.body?.bytes() ?: return@withContext false
                val out = File(context.filesDir, name)
                val fos = FileOutputStream(out)
                fos.write(bytes)
                fos.flush()
                fos.close()
                // optionally rename to model.tflite
                val target = File(context.filesDir, "model.tflite")
                if (target.exists()) target.delete()
                out.copyTo(target)
                return@withContext true
            } catch (e: Exception) {
                Log.e("ModelUpdater", "update failed", e)
                return@withContext false
            }
        }
    }
}
