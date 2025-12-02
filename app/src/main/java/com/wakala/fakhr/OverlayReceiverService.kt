package com.wakala.fakhr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.IBinder

// We'll extend the previous OverlayService to handle broadcasts
class OverlayReceiverService : OverlayService() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == "com.wakala.fakhr.ACTION_ANALYSIS_RESULTS") {
                val texts = intent.getStringArrayListExtra("texts") ?: return
                val lefts = intent.getIntegerArrayListExtra("lefts") ?: return
                val tops = intent.getIntegerArrayListExtra("tops") ?: return
                val rights = intent.getIntegerArrayListExtra("rights") ?: return
                val bottoms = intent.getIntegerArrayListExtra("bottoms") ?: return
                val confs = intent.getFloatArrayExtra("confs") ?: FloatArray(0)
                val winners = intent.getBooleanArrayExtra("winners") ?: BooleanArray(0)
                val preds = mutableListOf<com.wakala.fakhr.model.ChoicePrediction>()
                for (i in texts.indices) {
                    val rect = Rect(lefts[i], tops[i], rights[i], bottoms[i])
                    val conf = if (i < confs.size) confs[i] else 0f
                    val win = if (i < winners.size) winners[i] else false
                    preds.add(com.wakala.fakhr.model.ChoicePrediction(texts[i], rect, conf, win))
                }
                showPredictions(preds)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter("com.wakala.fakhr.ACTION_ANALYSIS_RESULTS"))
    }

    override fun onDestroy() {
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

fun showPredictions(predictions: List<com.wakala.fakhr.model.ChoicePrediction>) { /* implemented */ }


fun showTrapWarning(text: String) {
    try {
        val wm = windowManager ?: return
        val tv = android.widget.TextView(this)
        tv.text = text
        tv.setTextColor(android.graphics.Color.WHITE)
        tv.setBackgroundColor(android.graphics.Color.parseColor("#660000"))
        tv.setPadding(12,12,12,12)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = android.view.Gravity.TOP or android.view.Gravity.END
        params.x = 20
        params.y = 120
        wm.addView(tv, params)
        // remove after 5 seconds
        tv.postDelayed({ try { wm.removeView(tv) } catch(e: Exception){ } }, 5000)
    } catch (e: Exception) { e.printStackTrace() }
}


// BroadcastReceiver to listen for predictions and render overlay circles
private val predictionReceiver = object : android.content.BroadcastReceiver() {
    override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
        try {
            if (intent?.action == "com.wakala.fakhr.ACTION_PREDICTION_READY") {
                // read last_prediction from prefs
                val prefs = getSharedPreferences("wakala_prefs", android.content.Context.MODE_PRIVATE)
                val predJson = prefs.getString("last_prediction", null) ?: return
                val jo = org.json.JSONObject(predJson)
                val probs = jo.optJSONArray("probs") ?: return
                // draw top 1-3 circles
                val list = ArrayList<Pair<Int, Float>>()
                for (i in 0 until probs.length()) {
                    list.add(Pair(i, probs.optDouble(i).toFloat()))
                }
                list.sortByDescending { it.second }
                // draw up to top3
                for (k in 0 until minOf(3, list.size)) {
                    val idx = list[k].first
                    val p = list[k].second
                    drawCircleForSlot(idx, p)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}

override fun onCreate() {
    super.onCreate()
    try {
        val filter = android.content.IntentFilter()
        filter.addAction("com.wakala.fakhr.ACTION_PREDICTION_READY")
        registerReceiver(predictionReceiver, filter)
    } catch (e: Exception) { e.printStackTrace() }
}

override fun onDestroy() {
    try { unregisterReceiver(predictionReceiver) } catch (e: Exception) {}
    super.onDestroy()
}

private fun drawCircleForSlot(index: Int, confidence: Float) {
    try {
        // remove any existing circle view for this slot first (best-effort)
        val wm = windowManager ?: return
        val bmp = android.graphics.Bitmap.createBitmap(200,200, android.graphics.Bitmap.Config.ARGB_8888)
        val cv = android.graphics.Canvas(bmp)
        val paint = android.graphics.Paint()
        paint.isAntiAlias = true
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = when {
            confidence >= 0.6f -> android.graphics.Color.parseColor("#FFD700") // gold
            confidence >= 0.35f -> android.graphics.Color.parseColor("#00BFFF") // deep sky blue
            else -> android.graphics.Color.parseColor("#FF4500") // orange red
        }
        cv.drawCircle(100f,100f,80f, paint)
        val iv = android.widget.ImageView(this)
        iv.setImageBitmap(bmp)
        val bitmap = bmp
        // compute position using TemplateMapper on a reference bitmap size - use screen size
        val display = android.view.WindowManager.LayoutParams()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
        // estimate position using display metrics
        val metrics = android.util.DisplayMetrics()
        val wmObj = getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
        wmObj.defaultDisplay.getMetrics(metrics)
        val w = metrics.widthPixels; val h = metrics.heightPixels
        val rect = TemplateMapper.getSlotRectForIndex(android.graphics.Bitmap.createBitmap(w,h, android.graphics.Bitmap.Config.ARGB_8888), index, 0.12f)
        params.x = rect.centerX() - 100
        params.y = rect.centerY() - 100
        try {
            wm.addView(iv, params)
            // remove after 4 seconds
            iv.postDelayed({ try { wm.removeView(iv) } catch(e: Exception){} }, 4000)
        } catch (e: Exception) { e.printStackTrace() }
    } catch (e: Exception) { e.printStackTrace() }
}
