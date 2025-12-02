package com.wakala.fakhr.ai

import java.util.*

/**
 * TimeBasedPatternAI
 *
 * Uses known "hot minutes" and time-based heuristics to boost scores for certain multipliers.
 * You can configure hotMinuteRanges map to reflect observed behavior (e.g., 12, 25-35, 43, 48)
 */
class TimeBasedPatternAI {

    // map of label -> list of minute ranges (inclusive). minute ranges can be single minute "12" or "25-35"
    private val hotMinuteRanges: Map<String, List<String>> = mapOf(
        "100x" to listOf("12", "25-35", "43", "48"),
        "20x" to listOf(), // empty means no known hot ranges yet
        "8x" to listOf(),
        "3x" to listOf()
    )

    data class TimeScore(val label: String, val scoreBoost: Float)

    fun computeTimeBoost(label: String, calendar: Calendar): Float {
        val minute = calendar.get(Calendar.MINUTE)
        val ranges = hotMinuteRanges[label] ?: return 0f
        for (r in ranges) {
            if (r.contains("-")) {
                val parts = r.split("-")
                val a = parts[0].toIntOrNull() ?: continue
                val b = parts[1].toIntOrNull() ?: continue
                if (minute in a..b) return 0.35f // strong boost for hot window
            } else {
                val m = r.toIntOrNull() ?: continue
                if (minute == m) return 0.45f // very strong boost for exact minutes
            }
        }
        return 0f
    }

    /**
     * Batch compute for candidate labels; returns map label->boost
     */
    fun computeBatchBoost(candidates: List<String>, calendar: Calendar = Calendar.getInstance()): Map<String, Float> {
        val out = mutableMapOf<String, Float>()
        for (c in candidates) {
            out[c] = computeTimeBoost(c, calendar)
        }
        return out
    }
}
