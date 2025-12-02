package com.wakala.fakhr.data

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TrainingUploader(private val context: Context) {

    /**
     * Create zip of dataset directory and return File
     */
    fun zipDataset(): File? {
        try {
            val ds = File(context.filesDir, "dataset")
            if (!ds.exists()) return null
            val out = File(context.cacheDir, "dataset_upload.zip")
            if (out.exists()) out.delete()
            val zipOut = ZipOutputStream(out.outputStream())
            zipFolder(ds, ds, zipOut)
            zipOut.close()
            return out
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun zipFolder(root: File, src: File, zos: ZipOutputStream) {
        val files = src.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                zipFolder(root, f, zos)
            } else {
                val name = f.relativeTo(root).path
                zos.putNextEntry(ZipEntry(name))
                val ins = FileInputStream(f)
                ins.copyTo(zos)
                ins.close()
                zos.closeEntry()
            }
        }
    }

    /**
     * Upload zipped dataset to backend using ApiClient.create(context) as Retrofit builder.
     * This is a blocking call and should be called from a coroutine or worker.
     */
    suspend fun uploadDatasetFile(file: File): Boolean {
    try {
        val apiService = com.wakala.fakhr.ApiClient.create(context)
        val reqFile = okhttp3.RequestBody.create("application/zip".toMediaTypeOrNull(), file)
        val part = okhttp3.MultipartBody.Part.createFormData("file", file.name, reqFile)
        try {
            val method = apiService::class.java.methods.firstOrNull { it.name.contains("uploadDataset") || it.name.contains("upload") }
            if (method != null) {
                val resp = method.invoke(apiService, part) as retrofit2.Response<*>
                return resp.isSuccessful
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

}
