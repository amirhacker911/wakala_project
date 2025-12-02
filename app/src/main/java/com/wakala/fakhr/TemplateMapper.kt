package com.wakala.fakhr

import android.graphics.Rect
import android.graphics.Bitmap

/**
 * TemplateMapper: maps screen coordinates to logical slot indices for the 8 teams.
 * The coordinates here are relative percentages (0..1) for center positions of each slot.
 * These values can be tuned per device/resolution if necessary.
 */
object TemplateMapper {
    // center positions for 8 slots (x,y) as percentages of width/height
    val centers = arrayOf(
        Pair(0.25f, 0.18f), // top-left
        Pair(0.75f, 0.18f), // top-right
        Pair(0.25f, 0.33f), // upper-middle-left
        Pair(0.75f, 0.33f), // upper-middle-right
        Pair(0.25f, 0.62f), // lower-middle-left
        Pair(0.75f, 0.62f), // lower-middle-right
        Pair(0.40f, 0.78f), // bottom-left-ish (20x/100x positions vary)
        Pair(0.60f, 0.78f)  // bottom-right-ish
    )

    fun getSlotRectForIndex(bitmap: Bitmap, index: Int, radiusPercent: Float = 0.12f): Rect {
        val w = bitmap.width; val h = bitmap.height
        val cx = (centers[index].first * w).toInt()
        val cy = (centers[index].second * h).toInt()
        val r = (Math.min(w,h) * radiusPercent).toInt()
        return Rect(cx - r, cy - r, cx + r, cy + r)
    }

    fun nearestSlotForPoint(bitmap: Bitmap, x: Int, y: Int): Int {
        val w = bitmap.width; val h = bitmap.height
        var best = 0; var bestd = Float.MAX_VALUE
        for (i in centers.indices) {
            val dx = x - (centers[i].first * w)
            val dy = y - (centers[i].second * h)
            val d = dx*dx + dy*dy
            if (d < bestd) { bestd = d.toFloat(); best = i }
        }
        return best
    }
}
