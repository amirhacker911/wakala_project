package com.wakala.fakhr

import kotlin.math.max
import kotlin.math.min

data class BettingSnapshot(val ts: Long, val counts: IntArray) // length 8

class BettingFlowAnalyzer {

    private val history = ArrayList<BettingSnapshot>()
    private val maxHistory = 10 // keep last 10 seconds

    fun pushSnapshot(counts: IntArray) {
        val now = System.currentTimeMillis()
        history.add(BettingSnapshot(now, counts.copyOf()))
        if (history.size > maxHistory) history.removeAt(0)
    }

    fun getLatest(): BettingSnapshot? = if (history.isEmpty()) null else history.last()

    /**
     * compute speed (delta per second) per slot using last two snapshots
     */
    fun computeSpeeds(): FloatArray {
        val out = FloatArray(8)
        if (history.size < 2) return out
        val a = history[history.size - 2]; val b = history[history.size - 1]
        val dt = max(1, (b.ts - a.ts) / 1000).toInt()
        for (i in 0..7) {
            out[i] = (b.counts[i] - a.counts[i]).toFloat() / dt.toFloat()
        }
        return out
    }

    /**
     * returns a score per slot where higher means likelier to be target of fast influx (possible trap)
     */
    fun computeTrapScores(): FloatArray {
        val speeds = computeSpeeds()
        val out = FloatArray(8)
        for (i in 0..7) {
            // trap score increases with positive speed and absolute value relative to total
            out[i] = speeds[i]
        }
        return out
    }

    fun clear() { history.clear() }
}
