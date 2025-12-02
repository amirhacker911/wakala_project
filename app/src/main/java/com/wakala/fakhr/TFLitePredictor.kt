package com.wakala.fakhr

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLitePredictor(context: Context, assetPath: String = "model.tflite") {
    private var interpreter: Interpreter? = null

    init {
        try {
            val f = File(context.filesDir, assetPath)
            val modelBytes = if (f.exists()) f.readBytes() else context.assets.open(assetPath).readBytes()
            val bb = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
            bb.put(modelBytes)
            val opts = Interpreter.Options()
            opts.setNumThreads(2)
            interpreter = Interpreter(bb, opts)
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
        }
    }

    fun predict(input: FloatArray): FloatArray? {
        if (interpreter == null) return null
        val inputBuffer = ByteBuffer.allocateDirect(input.size * 4).order(ByteOrder.nativeOrder())
        for (v in input) inputBuffer.putFloat(v)
        val output = Array(1) { FloatArray(8) }
        interpreter!!.run(inputBuffer, output)
        return output[0]
    }
}
