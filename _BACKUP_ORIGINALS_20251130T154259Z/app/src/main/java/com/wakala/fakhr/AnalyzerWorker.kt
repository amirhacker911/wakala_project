package com.wakala.fakhr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wakala.fakhr.model.ChoicePrediction\nimport com.wakala.fakhr.ai.PatternScanner\nimport com.wakala.fakhr.ai.TimeBasedPatternAI\nimport com.wakala.fakhr.ai.BetVolumeAnalyzer\nimport com.wakala.fakhr.ai.TrapDetector\nimport java.util.Calendar\nimport com.wakala.fakhr.ai.PatternScanner\nimport com.wakala.fakhr.data.DatasetManager\nimport android.graphics.Bitmap
import com.wakala.fakhr.model.ChoiceBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyzerWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            // 1) Capture screen bitmap (requires that ScreenCapture has been initialized and permission previously granted)
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val sc = ScreenCapture(applicationContext)
                    sc.captureOnce()
                } catch (e: Exception) {
                    Log.e("AnalyzerWorker", "Capture failed", e)
                    null
                }
            }

            if (bitmap == null) {
                return Result.failure()
            }

            // 2) Run OCR to detect choices
            val ocr = OCRProcessor(applicationContext)
            val boxes: List<ChoiceBox> = ocr.detectChoices(bitmap)\n                // detect bottom bar sequence for pattern analysis\n                val bottomSeq = try { ocr.detectBottomBarSequence(bitmap, 10) } catch (e: Exception) { e.printStackTrace(); emptyList<String>() }\n                val ps = PatternScanner()\n                val patternResult = if (bottomSeq.isNotEmpty()) ps.analyze(bottomSeq.reversed()) else null

            if (boxes.isEmpty()) {
                // no choices found, maybe try fallback heuristics or return failure
                return Result.failure()
            }

            // 3) For each detected choice, crop and run prediction
            val predictions = mutableListOf<ChoicePrediction>()\n                // --- Advanced scoring engines ---\n                val tAi = TimeBasedPatternAI()\n                val bv = BetVolumeAnalyzer()\n                val trap = TrapDetector()\n                // prepare candidate labels list\n                val candidateLabels = boxes.map { it.text }
                // attempt to parse bet volumes from text if present (numbers inside text)
                val volumes = mutableMapOf<String, Double>()
                for (b in boxes) {
                    val num = try { Regex("([\\d.,]+)").find(b.text)?.value?.replace(",",".")?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 }
                    volumes[b.text] = num
                }
                // compute boosts
                val timeBoosts = tAi.computeBatchBoost(candidateLabels, Calendar.getInstance())
                val volBoosts = bv.computeBoosts(volumes)
                val patternBoosts = if (bottomSeq.isNotEmpty()) ps.predictNext(bottomSeq.reversed()) else emptyMap()
                val trapRes = trap.detect(bottomSeq.reversed(), volumes)

            val model = TFLiteModelManager(applicationContext)
            model.init() // best-effort; if no model available, we'll fallback to heuristic
            for (b in boxes) {
                val rect = Rect(b.rect)
                // ensure within bitmap bounds
                rect.intersect(0,0, bitmap.width, bitmap.height)
                val crop = Bitmap.createBitmap(bitmap, rect.left, rect.top, Math.max(1, rect.width()), Math.max(1, rect.height()))
                // If model available, run predict; else fallback heuristic
                var confidence = 0.0f\n                    // boost confidence if pattern suggests this option\n                    val patternBoost = if (patternResult != null) {\n                        val preds = ps.predictNext(bottomSeq.reversed())\n                        val token = b.text.trim()\n                        val pval = preds[token] ?: 0.0\n                        (pval * 0.5).toFloat()\n                    } else 0f\n                    
                if (model != null) {
                    try {
                        // Preprocess - example: resize float input expected by the model
                        // Here we attempt a simple pattern-informeder replaced */: treat confidence as length of text match heuristics
                        // Replace with model.predict implementation tailored to your model's I/O.
                        confidence = (heuristicConfidence(b.text, crop) + patternBoost).coerceAtMost(1.0f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        confidence = (heuristicConfidence(b.text, crop) + patternBoost).coerceAtMost(1.0f)
                    }
                } else {
                    confidence = (heuristicConfidence(b.text, crop) + patternBoost).coerceAtMost(1.0f)
                }
                // combine boosts into final score\n                    val label = b.text\n                    val timeBoost = timeBoosts[label] ?: 0f\n                    val volBoost = volBoosts[label] ?: 0f\n                    val patBoost = (patternBoosts[label] ?: 0.0).toFloat()\n                    var finalScore = confidence * 0.5f + timeBoost * 0.25f + volBoost * 0.15f + patBoost * 0.1f\n                    // apply trap penalty if detected (reduce score of likely-trapped option)
                    if (trapRes.isTrap) {
                        // if mostCommon equals this label, penalize
                        val most = patternResult?.mostCommon
                        if (most != null && most == label) {
                            finalScore = (finalScore - trapRes.penalty).coerceAtLeast(0f)
                        }
                    }
                    predictions.add(ChoicePrediction(b.text, rect, finalScore, false))
            }

            // 4) Choose winner as highest confidence (simple strategy)
            val max = predictions.maxByOrNull { it.confidence }
            max?.let { // winner assigned via immutable reconstruction; removed direct mutation } // data class is immutable; we'll construct new list
            val finalPreds = predictions.map {
                ChoicePrediction(it.text, it.rect, it.confidence, it == max)
            }

            // 5) Send broadcast or start OverlayService to display markers
            val intent = Intent(applicationContext, OverlayService::class.java)
            applicationContext.startService(intent) // ensure service running
            // send broadcast containing serialized results (we'll send minimal info: rects and winner index)
            val bcast = Intent("com.wakala.fakhr.ACTION_ANALYSIS_RESULTS")
            // pack arrays: texts, lefts, tops, rights, bottoms, confidences, winners
            val texts = ArrayList<String>()
            val lefts = ArrayList<Int>()
            val tops = ArrayList<Int>()
            val rights = ArrayList<Int>()
            val bottoms = ArrayList<Int>()
            val confs = ArrayList<Float>()
            val winners = ArrayList<Boolean>()
            for (p in finalPreds) {
                texts.add(p.text)
                lefts.add(p.rect.left)
                tops.add(p.rect.top)
                rights.add(p.rect.right)
                bottoms.add(p.rect.bottom)
                confs.add(p.confidence)
                winners.add(p.isWinner)
            }
            bcast.putStringArrayListExtra("texts", texts)
            bcast.putIntegerArrayListExtra("lefts", lefts)
            bcast.putIntegerArrayListExtra("tops", tops)
            bcast.putIntegerArrayListExtra("rights", rights)
            bcast.putIntegerArrayListExtra("bottoms", bottoms)
            bcast.putExtra("confs", confs.toFloatArray())
            bcast.putExtra("winners", winners.toBooleanArray())
            applicationContext.sendBroadcast(bcast)

            model.close()\n\n                // Persist crops as training samples with metadata (roundNumber default 0).\n                try {\n                    val ds = com.wakala.fakhr.data.DatasetManager(applicationContext)\n                    var idx = 0\n                    for (p in finalPreds) {\n                        val cropRect = p.rect\n                        val bmp = android.graphics.Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, Math.max(1, cropRect.width()), Math.max(1, cropRect.height()))\n                        val metaOk = ds.saveSample(bmp, cropRect, roundNumber = 0, matchNumber = 0, label = if (p.isWinner) "winner" else "loser")\n                        // optionally save pattern json next to sample\n                        try {\n                            if (patternResult != null) {\n                                val patFile = java.io.File(bmp.cacheDir ?: applicationContext.cacheDir, "pattern_${System.currentTimeMillis()}.json")\n                                // write small JSON with sequence and mostCommon\n                                val jo = org.json.JSONObject()\n                                jo.put("sequence", org.json.JSONArray(patternResult.sequence))\n                                jo.put("most", patternResult.mostCommon)\n                                patFile.writeText(jo.toString())\n                            }\n                        } catch (e: Exception) { e.printStackTrace() }\n                        idx++\n                    }\n                } catch (e: Exception) { e.printStackTrace() }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    // Simple heuristic function to estimate confidence based on OCR text and image features
    private fun heuristicConfidence(text: String, crop: Bitmap): Float {
        var score = 0f
        val lower = text.toLowerCase()
        // boost if text contains numbers or percent or odds-like tokens
        if (lower.matches(Regex(".*\\d.*"))) score += 0.4f
        if (lower.contains("%") || lower.contains("odds") || lower.contains(":")) score += 0.2f
        // image-based simple heuristic: brightness / contrast
        val avg = averageBrightness(crop)
        if (avg > 30) score += 0.2f
        // text length contributes
        val len = Math.min(20, text.length)
        score += (len / 20f) * 0.2f
        return Math.min(1f, score)
    }

    private fun averageBrightness(bitmap: Bitmap): Float {
        val w = bitmap.width
        val h = bitmap.height
        var sum = 0L
        val stepX = Math.max(1, w / 40)
        val stepY = Math.max(1, h / 40)
        for (x in 0 until w step stepX) {
            for (y in 0 until h step stepY) {
                val p = bitmap.getPixel(x,y)
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff
                sum += (r + g + b) / 3
            }
        }
        val samples = (Math.ceil(w/stepX.toDouble()) * Math.ceil(h/stepY.toDouble())).toInt()
        if (samples == 0) return 0f
        return sum.toFloat() / samples
    }
}


// === Auto-prediction hook added by assistant ===
try {
    val prefs = context.getSharedPreferences("wakala_prefs", android.content.Context.MODE_PRIVATE)
    // example: countsIntArray should be produced by OCR parsing - here we try to read existing variable counts
    // You must adapt variable names based on actual code; this is a safe best-effort integration point.
    val counts = IntArray(8)
    try { // attempt to read from a JSON string saved by OCRProcessor
        val json = prefs.getString("last_counts_json", null)
        if (json != null) {
            val ja = org.json.JSONArray(json)
            for (i in 0 until minOf(8, ja.length())) counts[i] = ja.optInt(i, 0)
        }
    } catch (e: Exception) { e.printStackTrace() }
    val bfa = BettingFlowAnalyzer()
    bfa.pushSnapshot(counts)
    val speeds = bfa.computeSpeeds()
    val td = TrapDetector()
    val traps = td.detectTraps(counts, speeds)
    val minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    val prev = IntArray(0)
    val fp = FinalPredictor()
    val pred = fp.predict(counts, speeds, minute, prev)
    // save prediction to prefs for overlay consumption
    val out = org.json.JSONObject()
    val parr = org.json.JSONArray()
    for (i in pred.probs.indices) parr.put(pred.probs[i].toDouble())
    out.put("probs", parr)
    val reasons = org.json.JSONArray()
    for (r in pred.reasons) reasons.put(r)
    out.put("reasons", reasons)
    prefs.edit().putString("last_prediction", out.toString()).apply()
    // broadcast for overlay
    val ib = android.content.Intent("com.wakala.fakhr.ACTION_PREDICTION_READY")
    ib.setPackage(context.packageName)
    context.sendBroadcast(ib)
} catch (e: Exception) { e.printStackTrace() }
// === end hook ===
