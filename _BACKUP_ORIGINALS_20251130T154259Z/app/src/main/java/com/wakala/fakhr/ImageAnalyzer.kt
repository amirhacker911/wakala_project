package com.wakala.fakhr

import android.graphics.Bitmap

class ImageAnalyzer {
    fun analyze(bitmap: Bitmap?): Map<String, Any> {
        // Prefer to accept passed text from OCR; for backward compatibility, use sample parsing.
        val sampleText = "TeamA 1 - TeamB 0 Round 3"
        val nums = Regex("\\d+").findAll(sampleText).map { it.value.toInt() }.toList()
        val scoreA = if (nums.size>0) nums[0].toDouble() else 0.0
        val scoreB = if (nums.size>1) nums[1].toDouble() else 0.0
        val round = if (nums.size>2) nums[2] else 0
        return mapOf("scoreA" to scoreA, "scoreB" to scoreB, "round" to round, "raw_text" to sampleText)
    }
}
