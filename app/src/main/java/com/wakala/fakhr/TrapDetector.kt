package com.wakala.fakhr

import kotlin.math.max

class TrapDetector {

    // threshold params
    var speedThreshold = 50.0f // bets per second large influx
    var largeCountThreshold = 4000 // if count above this it's suspicious often

    /**
     * returns boolean array per slot: true if likely trap
     */
    fun detectTraps(counts: IntArray, speeds: FloatArray): BooleanArray {
        val res = BooleanArray(8)
        val total = counts.sum().toFloat().coerceAtLeast(1f)
        for (i in 0..7) {
            val cnt = counts[i]
            val sp = speeds.getOrNull(i) ?: 0f
            val frac = cnt / total
            // heuristics:
            // - very large absolute count suggests house might target it
            // - sudden big speed suggests trap
            if (cnt >= largeCountThreshold && frac > 0.3) res[i] = true
            if (sp >= speedThreshold) res[i] = true
        }
        return res
    }
}
