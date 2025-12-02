package com.wakala.fakhr

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wakala.fakhr.data.TrainingUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadAndTrainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val uploader = TrainingUploader(applicationContext)
            val zip = uploader.zipDataset()
            if (zip == null) {
                Log.i("UploadTrain", "No dataset to upload") 
                return Result.success()
            }
            // upload dataset via TrainingUploader (attempt)
            val ok = uploader.uploadDatasetFile(zip)
            if (!ok) {
                // fallback: manual upload to server via simple POST
                val base = inputData.getString("BASE_URL") ?: "http://127.0.0.1:5000/"
                val client = OkHttpClient()
                val media = okhttp3.RequestBody.create("application/zip".toMediaTypeOrNull(), zip)
                val part = MultipartBody.Part.createFormData("file", zip.name, media)
                // build multipart body manually
                val body = okhttp3.MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", zip.name, media).build()
                val req = Request.Builder().url(base + "datasets/upload").post(body).build()
                val resp = client.newCall(req).execute()
                if (!resp.isSuccessful) {
                    Log.e("UploadTrain", "upload failed: ${'$'}{resp.code}")
                    return Result.retry()
                }
            }
            // trigger server training
            val base = inputData.getString("BASE_URL") ?: "http://127.0.0.1:5000/"
            val client = OkHttpClient()
            val req2 = Request.Builder().url(base + "trigger-train").post("" .toRequestBody()).build()
            val resp2 = client.newCall(req2).execute()
            if (resp2.isSuccessful) {
                // fetch model
                val mu = ModelUpdater(applicationContext, base)
                mu.checkAndUpdate()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
