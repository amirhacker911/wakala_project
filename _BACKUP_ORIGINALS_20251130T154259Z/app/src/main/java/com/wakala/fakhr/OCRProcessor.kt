package com.wakala.fakhr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.wakala.fakhr.model.ChoiceBox
import kotlinx.coroutines.tasks.await

class OCRProcessor(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Process a bitmap and return detected choice boxes.
     * This function attempts to group nearby text blocks into "choices".
     */
    suspend fun detectChoices(bitmap: Bitmap): List<ChoiceBox> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result: Text = recognizer.process(image).await()
        val blocks = result.textBlocks
        val boxes = mutableListOf<ChoiceBox>()

        // Simple heuristic: treat each line-level element as potential choice.
        for (block in blocks) {
            for (line in block.lines) {
                val text = line.text.trim()
                if (text.isEmpty()) continue
                val rect = line.boundingBox ?: continue
                // Filter out very small boxes
                if (rect.width() < 40 || rect.height() < 20) continue
                boxes.add(ChoiceBox(text = text, rect = Rect(rect)))
            }
        }

        // Post-process: merge boxes that are horizontally aligned and close (likely multi-line)
        val merged = mergeNearbyBoxes(boxes)
        return merged
    }

    private fun mergeNearbyBoxes(input: List<ChoiceBox>): List<ChoiceBox> {
        if (input.size <= 1) return input
        val taken = BooleanArray(input.size)
        val out = mutableListOf<ChoiceBox>()
        for (i in input.indices) {
            if (taken[i]) continue
            var base = input[i]
            var mergedText = base.text
            var mergedRect = Rect(base.rect)
            for (j in i+1 until input.size) {
                if (taken[j]) continue
                val other = input[j]
                if (areAlignedHorizontally(mergedRect, other.rect) && isClose(mergedRect, other.rect)) {
                    // merge
                    mergedText += " " + other.text
                    mergedRect.union(other.rect)
                    taken[j] = true
                }
            }
            out.add(ChoiceBox(mergedText.trim(), mergedRect))
        }
        return out
    }

    private fun areAlignedHorizontally(a: Rect, b: Rect): Boolean {
        val aMid = a.centerY()
        val bMid = b.centerY()
        return Math.abs(aMid - bMid) <= Math.max(a.height(), b.height()) * 0.6
    }

    private fun isClose(a: Rect, b: Rect): Boolean {
        val gap = Math.max(10, (a.width() * 0.2).toInt())
        return (b.left <= a.right + gap && b.right >= a.left - gap) ||
               (a.left <= b.right + gap && a.right >= b.left - gap)
    }

/**
 * Detect a simple recent sequence from the bottom bar area.
 * This function looks for short tokens (A/B or team shortnames) and returns latest N tokens.
 */
suspend fun detectBottomBarSequence(bitmap: android.graphics.Bitmap, maxTokens: Int = 10): List<String> {
    val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
    val result = recognizer.process(image).await()
    val tokens = mutableListOf<Pair<String, android.graphics.Rect>>()
    for (block in result.textBlocks) {
        for (line in block.lines) {
            val text = line.text.trim()
            val rect = line.boundingBox ?: continue
            // Heuristic: bottom bar likely near bottom 25% of screen
            val threshold = bitmap.height * 0.75
            if (rect.centerY() < threshold) continue
            // token candidates: short texts (1-6 chars)
            if (text.length in 1..6) {
                tokens.add(Pair(text, android.graphics.Rect(rect)))
            }
        }
    }
    // sort by x (left to right) and return texts up to maxTokens
    tokens.sortBy { it.second.left }
    val seq = tokens.map { it.first }
    return seq.takeLast(maxTokens)
}
}


// Helper: save last parsed timer seconds to SharedPreferences for AutoRoundWatcher
fun saveLastTimerSeconds(context: android.content.Context, seconds: Int) {
    try {
        val prefs = context.getSharedPreferences("wakala_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putInt("last_timer_seconds", seconds).apply()
    } catch (e: Exception) { e.printStackTrace() }
}


// === assistant-added helpers for AutoRoundWatcher and Analyzer integration ===

fun saveLastTimerSeconds(context: android.content.Context, seconds: Int) {
    try {
        val prefs = context.getSharedPreferences("wakala_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putInt("last_timer_seconds", seconds).apply()
    } catch (e: Exception) { e.printStackTrace() }
}

fun saveLastCountsJson(context: android.content.Context, counts: IntArray) {
    try {
        val prefs = context.getSharedPreferences("wakala_prefs", android.content.Context.MODE_PRIVATE)
        val ja = org.json.JSONArray()
        for (i in counts.indices) ja.put(counts[i])
        prefs.edit().putString("last_counts_json", ja.toString()).apply()
    } catch (e: Exception) { e.printStackTrace() }
}

// Example: call saveLastTimerSeconds(context, parsedSeconds) and saveLastCountsJson(context, countsArray) in your OCR parsing flow.
