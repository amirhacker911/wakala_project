package com.wakala.fakhr.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SampleMeta(
    val timestamp: Long,
    val day: String,
    val hourMinute: String,
    val roundNumber: Int,
    val matchNumber: Int
)

class DatasetManager(private val context: Context) {
    private val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val sdfHourMinute = SimpleDateFormat("HH-mm", Locale.US)

    /**
     * Save a cropped bitmap as a training sample with metadata.
     * Structure: filesDir/dataset/<day>/<hour-minute>/round_<roundNumber>/sample_<ts>.png
     * Also writes sample_<ts>.json metadata next to PNG.
     *
     * patternSummary: optional short summary of detected pattern (e.g., mostCommon token)
     * volumes: optional map of label -> bet volume
     */
    fun saveSample(bitmap: Bitmap, rect: Rect, roundNumber: Int = 0, matchNumber: Int = 0, label: String? = null, patternSummary: String? = null, volumes: Map<String, Double>? = null): Boolean {
        try {
            val ts = System.currentTimeMillis()
            val day = sdfDay.format(Date(ts))
            val hm = sdfHourMinute.format(Date(ts))
            val base = File(context.filesDir, "dataset/$day/$hm/round_$roundNumber")
            if (!base.exists()) base.mkdirs()
            val pngFile = File(base, "sample_${ts}.png")
            val fos = FileOutputStream(pngFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.flush()
            fos.close()

            // metadata
            val meta = JSONObject()
            meta.put("timestamp", ts)
            meta.put("day", day)
            meta.put("hour_minute", hm)
            meta.put("time_epoch", ts)
            meta.put("round", roundNumber)
            meta.put("match", matchNumber)
            val rectObj = JSONObject()
            rectObj.put("left", rect.left); rectObj.put("top", rect.top); rectObj.put("right", rect.right); rectObj.put("bottom", rect.bottom)
            meta.put("rect", rectObj)
            if (label != null) meta.put("label", label)
            if (patternSummary != null) meta.put("pattern_summary", patternSummary)
            if (volumes != null) meta.put("volumes", JSONObject(volumes))

            val jsonFile = File(base, "sample_${ts}.json")
            jsonFile.writeText(meta.toString())

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
