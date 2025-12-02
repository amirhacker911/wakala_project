package com.wakala.fakhr

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModelManager(private val context: Context, private val modelFileName: String = "model.tflite") {
    private var interpreter: Interpreter? = null
    private var modelFile: File? = null

    private fun loadModelFile(): MappedByteBuffer {
        val f = File(context.filesDir, modelFileName)
        modelFile = if (f.exists()) f else null
        if (modelFile == null) {
            // try assets fallback
            val afd = context.assets.openFd(modelFileName)
            val input = FileInputStream(afd.fileDescriptor)
            val fc = input.channel
            return fc.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.length)
        } else {
            val input = FileInputStream(modelFile!!)
            val fc = input.channel
            return fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
        }
    }

    fun hasModel(): Boolean {
        val f = File(context.filesDir, modelFileName)
        return f.exists()
    }

    fun init(): Boolean {
        return try {
            val modelBuffer = loadModelFile()
            interpreter = Interpreter(modelBuffer)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
            false
        }
    }

    /**
     * Predict with a bitmap input. This performs standard preprocessing (resize->224, normalize to [-1,1]) - adjust if your model uses different normalization or input size.
     * Returns float array of confidences or null if interpreter missing.
     */
    fun predictBitmap(bitmap: android.graphics.Bitmap, inputSize: Int = 224): FloatArray? {
    interpreter ?: return null
    return try {
        val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val byteBuffer = java.nio.ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(java.nio.ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        scaled.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)
        var i = 0
        while (i < intValues.size) {
            val v = intValues[i++]
            val r = ((v shr 16) and 0xFF).toFloat()
            val g = ((v shr 8) and 0xFF).toFloat()
            val b = (v and 0xFF).toFloat()
            byteBuffer.putFloat((r / 127.5f) - 1.0f)
            byteBuffer.putFloat((g / 127.5f) - 1.0f)
            byteBuffer.putFloat((b / 127.5f) - 1.0f)
        }
        byteBuffer.rewind()
        val output = Array(1) { FloatArray(2) }
        interpreter!!.run(byteBuffer, output)
        output[0]
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
