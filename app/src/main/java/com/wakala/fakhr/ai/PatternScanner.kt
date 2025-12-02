package com.wakala.fakhr.ai

/**
 * PatternScanner
 *
 * Detects simple repeating patterns from a sequence of recent results.
 * Assumes sequence items are short tokens like "A", "B", "DRAW" or team names.
 *
 * Provides:
 * - frequency map
 * - most common n-gram patterns
 * - simple Markov-based next-item probabilities
 */
class PatternScanner {

    data class PatternResult(
        val sequence: List<String>,
        val frequencies: Map<String, Int>,
        val mostCommon: String?,
        val ngramPatterns: Map<String, Int>,
        val markovProbabilities: Map<String, Double>
    )

    /**
     * Analyze a recent sequence (latest first) and return PatternResult.
     * sequence: list like ["A","B","A","A","B"]
     */
    fun analyze(sequence: List<String>): PatternResult {
        val freqs = mutableMapOf<String, Int>()
        for (s in sequence) freqs[s] = freqs.getOrDefault(s, 0) + 1
        val most = freqs.maxByOrNull { it.value }?.key

        val ngramCounts = mutableMapOf<String, Int>()
        val n = 2
        for (i in 0 until sequence.size - n + 1) {
            val gram = sequence.subList(i, i + n).joinToString("-")
            ngramCounts[gram] = ngramCounts.getOrDefault(gram, 0) + 1
        }

        val markov = computeMarkov(sequence)

        return PatternResult(
            sequence = sequence,
            frequencies = freqs,
            mostCommon = most,
            ngramPatterns = ngramCounts,
            markovProbabilities = markov
        )
    }

    /**
     * Simple first-order Markov model: P(next = X | last = L)
     */
    private fun computeMarkov(seq: List<String>): Map<String, Double> {
        if (seq.isEmpty()) return emptyMap()
        val transitions = mutableMapOf<String, MutableMap<String, Int>>()
        for (i in 0 until seq.size - 1) {
            val a = seq[i]
            val b = seq[i + 1]
            val inner = transitions.getOrPut(a) { mutableMapOf() }
            inner[b] = inner.getOrDefault(b, 0) + 1
        }
        val last = seq.first()
        val probs = mutableMapOf<String, Double>()
        val inner = transitions[last] ?: return probs
        val total = inner.values.sum().toDouble()
        if (total <= 0) return probs
        for ((k, v) in inner) {
            probs[k] = v / total
        }
        return probs
    }

    /**
     * Heuristic predictor using frequencies and markov:
     * returns map of candidate -> score (0..1)
     */
    fun predictNext(sequence: List<String>): Map<String, Double> {
        val res = analyze(sequence)
        val scores = mutableMapOf<String, Double>()
        val totalFreq = res.frequencies.values.sum().toDouble().coerceAtLeast(1.0)
        for ((k, v) in res.frequencies) {
            scores[k] = (v.toDouble() / totalFreq) * 0.6 // base weight
        }
        // incorporate markov
        for ((k, v) in res.markovProbabilities) {
            scores[k] = scores.getOrDefault(k, 0.0) + v * 0.4
        }
        // normalize
        val sum = scores.values.sum().coerceAtLeast(1.0)
        val normalized = scores.mapValues { it.value / sum }
        return normalized
    }
}
