package com.wakala.fakhr.ai

/**
 * TrapDetector
 *
 * Simple heuristics to detect possible "trap" rounds where the observed pattern might be broken intentionally.
 * It looks for long unbroken streaks or sudden severe imbalance in bet volumes.
 */
class TrapDetector {

    data class TrapResult(val isTrap: Boolean, val penalty: Float, val reason: String?)

    /**
     * Detect trap based on frequencies and optionally volumes.
     * - if the same label appeared >= streakThreshold times consecutively -> high chance of trap next round.
     * - if top label has > volumeImbalanceThreshold fraction of total bets -> possible trap indicator.
     */
    fun detect(sequence: List<String>, volumes: Map<String, Double>? = null, streakThreshold: Int = 5, volumeImbalanceThreshold: Double = 0.7): TrapResult {
        if (sequence.isEmpty()) return TrapResult(false, 0f, null)
        // check streak
        var streak = 1
        val first = sequence.first()
        for (i in 1 until sequence.size) {
            if (sequence[i] == first) streak++ else break
        }
        if (streak >= streakThreshold) {
            return TrapResult(true, 0.45f, "long_streak_$streak")
        }
        // check volume imbalance
        if (volumes != null && volumes.isNotEmpty()) {
            val total = volumes.values.sum()
            val top = volumes.values.maxOrNull() ?: 0.0
            if (total > 0 && top / total >= volumeImbalanceThreshold) {
                return TrapResult(true, 0.35f, "volume_imbalance")
            }
        }
        return TrapResult(false, 0f, null)
    }
}
