package com.wakala.fakhr

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRProcessor(private val context: Context) {
    private val recognizer = TextRecognition.getClient(ArabicTextRecognizerOptions.Builder().build())
    suspend fun processBitmap(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        return result.text
    }
}
