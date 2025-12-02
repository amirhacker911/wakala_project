package com.wakala.fakhr

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ModelHotSwapManager(private val context: Context) {

    /**
     * Save provided input stream as model.tflite into filesDir and return file reference.
     */
    fun saveModelStream(stream: InputStream, targetName: String = "model.tflite"): File? {
        return try {
            val out = File(context.filesDir, targetName)
            val fos = FileOutputStream(out)
            stream.copyTo(fos)
            fos.flush()
            fos.close()
            out
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Replace current model by moving provided file into place.
     */
    fun replaceModelFile(file: File, targetName: String = "model.tflite"): Boolean {
        return try {
            val tgt = File(context.filesDir, targetName)
            if (tgt.exists()) tgt.delete()
            file.copyTo(tgt)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
