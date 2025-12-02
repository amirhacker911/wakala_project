package com.wakala.fakhr

import kotlin.math.log10

class ImageAnalyzer {
    fun analyzeFromText(rawText: String): Map<String, Any> {
        val out = HashMap<String, Any>()
        out["raw_text"] = rawText
        val mulRegex = Regex("(\\d{1,3})\\s*(?:×|x|مرات|X)", RegexOption.IGNORE_CASE)
        val muls = mulRegex.findAll(rawText).mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }.toList()
        out["multipliers"] = muls
        val numRegex = Regex("\\d{1,7}")
        val nums = numRegex.findAll(rawText).map { it.value }.toList()
        fun arabicToLatin(s: String): String {
            val map = mapOf('٠' to '0','١' to '1','٢' to '2','٣' to '3','٤' to '4','٥' to '5','٦' to '6','٧' to '7','٨' to '8','٩' to '9')
            return s.map { map.getOrDefault(it, it) }.joinToString("")
        }
        val numsClean = nums.map { arabicToLatin(it) }
        out["numbers_raw"] = numsClean
        val possibleAmounts = numsClean.mapNotNull { it.toDoubleOrNull() }.sortedDescending()
        val bets = mutableMapOf<String, Double>()
        val lines = rawText.split('\n').map{ it.trim() }.filter { it.isNotEmpty() }
        val options = mutableListOf<String>()
        for (ln in lines) {
            if (ln.any { it.isLetter() } && options.size < 8) {
                options.add(ln)
            }
        }
        while (options.size < 8) { options.add("opt${options.size}") }
        val chosen = if (possibleAmounts.size >= 8) possibleAmounts.subList(0,8) else {
            val res = possibleAmounts.toMutableList()
            while (res.size < 8) res.add(0.0)
            res
        }
        for (i in 0 until 8) {
            val key = options.getOrElse(i) { "opt$i" }
            bets[key] = chosen.getOrNull(i) ?: 0.0
        }
        out["options"] = options
        out["bets_amounts"] = bets
        val total = bets.values.sum()
        val maxBet = if (bets.isNotEmpty()) bets.values.maxOrNull() ?: 0.0 else 0.0
        val avgLog = if (bets.values.any { it>0 }) bets.values.filter{it>0}.map{ log10(it+1) }.average() else 0.0
        out["feature_vector"] = listOf(total, maxBet, avgLog)
        return out
    }
}
