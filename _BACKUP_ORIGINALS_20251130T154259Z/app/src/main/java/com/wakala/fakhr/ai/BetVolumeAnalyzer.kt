package com.wakala.fakhr.ai

import kotlin.math.log10

/**
 * BetVolumeAnalyzer
 *
 * Analyzes numeric bet volumes displayed above each option and produces a relative score.
 * It expects bet values parsed as Long or Double and returns normalized boosts.
 */
class BetVolumeAnalyzer {

    /**
     * Given a map label->volume returns map label->boost (0..1)
     * Lower volume is assumed to have higher attractiveness (players less wagered there),
     * but extreme very-low volumes may be noisy; we apply a smoothing function.
     */
    fun computeBoosts(volumes: Map<String, Double>): Map<String, Float> {
        if (volumes.isEmpty()) return emptyMap()
        // convert to relative scores where lower volume -> higher base score
        val max = volumes.values.maxOrNull() ?: 1.0
        val min = volumes.values.minOrNull() ?: 0.0
        val scores = mutableMapOf<String, Double>()
        for ((k, v) in volumes) {
            // normalized inverse score in [0,1]
            val inv = 1.0 - (v / (max.coerceAtLeast(1.0)))
            // apply log smoothing to reduce impact of outliers
            val smooth = if (v > 0) inv * (1.0 / (1.0 + log10(v + 1.0))) else inv
            scores[k] = smooth
        }
        // normalize to 0..1
        val sum = scores.values.sum().coerceAtLeast(1.0)
        val out = mutableMapOf<String, Float>()
        for ((k, v) in scores) out[k] = (v / sum).toFloat()
        return out
    }
}
