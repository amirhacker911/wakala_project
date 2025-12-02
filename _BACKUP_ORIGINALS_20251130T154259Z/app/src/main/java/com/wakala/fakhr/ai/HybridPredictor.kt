package com.wakala.fakhr.ai

import android.content.Context
import org.json.JSONObject

object HybridPredictor {
    // local fallback deterministic weights (if no server)
    private val localWeights = floatArrayOf(0.12f,0.12f,0.15f,0.15f,0.2f,0.12f,0.07f,0.07f)

    fun pickFromServer(context: Context, onResult:(JSONObject?)->Unit) {
        PatternEngineClient.getPrediction { json ->
            onResult(json)
        }
    }

    fun pickLocal(history:IntArray): JSONObject {
        // simple local heuristic mirror of server hybrid (fallback)
        val scores = DoubleArray(8) {0.0}
        if (history != null && history.isNotEmpty()) {
            val freq = IntArray(4) {0}
            for (v in history) {
                when (v) {
                    3 -> freq[0] += 1
                    8 -> freq[1] += 1
                    20 -> freq[2] += 1
                    100 -> freq[3] += 1
                }
            }
            val total = freq.sum().coerceAtLeast(1)
            for (i in 0..7) {
                val m = when(i) {0,1->3;2,3->8;4,5->20;6,7->100; else->3}
                val idx = when(m) {3->0;8->1;20->2;100->3; else->0}
                scores[i] += freq[idx].toDouble()/total
            }
        } else {
            for (i in 0..7) scores[i] = localWeights[i].toDouble()
        }
        var best = 0
        for (i in 1..7) if (scores[i] > scores[best]) best = i
        val jo = JSONObject()
        jo.put("prediction_slot", best)
        jo.put("prediction_multiplier", when(best) {0,1->3;2,3->8;4,5->20;6,7->100; else->3})
        jo.put("confidence", scores[best] / (scores.sum()))
        jo.put("reason", "local-freq")
        return jo
    }
}
