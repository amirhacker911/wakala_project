package com.wakala.fakhr

import kotlin.math.exp
import kotlin.math.max

data class Prediction(val probs: FloatArray, val reasons: List<String>)

class FinalPredictor {

    // simple weighted heuristic combined with optional TFLite model (not integrated here)
    var timeWeight = 1.2f
    var countWeight = -0.5f
    var speedWeight = -1.0f
    var modelAvailable = false

    fun predict(counts: IntArray, speeds: FloatArray, minuteOfHour: Int, prevResults: IntArray?): Prediction {
        val scores = FloatArray(8)
        val total = counts.sum().toFloat().coerceAtLeast(1f)
        val reasons = ArrayList<String>()
        for (i in 0..7) {
            // base: inverse of relative count (less crowded => higher)
            val rel = counts[i] / total
            var sc = (1.0f - rel) * 100.0f
            // penalize high speed (sudden influx)
            sc += ( - speedWeight * speeds.getOrNull(i) )
            // time heuristic: certain minutes favor some slots (example mapping)
            val timeBoost = timeBasedBoost(i, minuteOfHour)
            sc += timeBoost * timeWeight
            scores[i] = sc
        

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
        // normalize softmax
        val probs = softmax(scores)
        // build reasons for top3
        val top = probs.withIndex().sortedByDescending { it.value }.take(3)
        for ((idx, p) in top) {
            reasons.add("slot_${idx}: score=${String.format(\"%.2f\", scores[idx])}, prob=${String.format(\"%.2f\", p)}")
        

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
        return Prediction(probs, reasons)
    

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
    private fun timeBasedBoost(slotIndex: Int, minute: Int): Float {
        // naive mapping: slot 6 (index 6) corresponds to 100x in many samples (example)
        // you should tune this with real data
        if (minute == 43 || minute == 48 || minute == 12) {
            // boost 100x-related slots (assume indices 6,7 are 100x)
            if (slotIndex >= 6) return 40f
        

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
        return 0f
    

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
    private fun softmax(scores: FloatArray): FloatArray {
        val max = scores.maxOrNull() ?: 0f
        val exps = scores.map { exp((it - max).toDouble()).toFloat() 

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}
        val sum = exps.sum().coerceAtLeast(1e-6f)
        return exps.map { it / sum }.toFloatArray()
    

// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}


// Optional: load TFLite interpreter from filesDir/model.tflite and use it for prediction if available
private var tfliteInterpreter: org.tensorflow.lite.Interpreter? = null

fun loadModelInterpreter(context: android.content.Context) {
    try {
        val f = java.io.File(context.filesDir, "model.tflite")
        if (f.exists() && tfliteInterpreter == null) {
            val options = org.tensorflow.lite.Interpreter.Options()
            tfliteInterpreter = org.tensorflow.lite.Interpreter(f, options)
            modelAvailable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// If tfliteInterpreter exists, you can call it here. This placeholder shows where to integrate.

}