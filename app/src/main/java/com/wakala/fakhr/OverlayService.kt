package com.wakala.fakhr

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.wakala.fakhr.model.ChoicePrediction

/**
 * OverlayService: shows floating button and draws prediction circles over screen positions.
 * Requires ACTION_MANAGE_OVERLAY_PERMISSION granted by user.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val drawnMarkers = mutableListOf<View>()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
    }

    private fun createFloatingButton() {
        val inflater = LayoutInflater.from(this)
        val root = FrameLayout(this)
        val btn = ImageView(this)
        btn.setImageResource(android.R.drawable.ic_menu_info_details)
        btn.setPadding(20,20,20,20)
        root.addView(btn, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        floatingView = root
        windowManager?.addView(floatingView, params)

        // touch to drag
        var lastX = 0f
        var lastY = 0f
        btn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - lastX).toInt()
                    val dy = (event.rawY - lastY).toInt()
                    params.x += dx
                    params.y += dy
                    windowManager?.updateViewLayout(floatingView, params)
                    lastX = event.rawX
                    lastY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Trigger analysis when clicked (short press)
                    performAnalysis()
                    true
                }
                else -> false
            }
        }
    }

    private fun performAnalysis() {
        // Send broadcast that floating button was pressed.
        val b = Intent("com.wakala.fakhr.ACTION_FLOAT_BUTTON_PRESSED")
        sendBroadcast(b)
    }

    /**
     * Draw predicted circles over the screen coordinates.
     * Coordinates are expected in absolute screen pixels (x,y,width,height).
     */
    fun showPredictions(predictions: List<ChoicePrediction>) {
        clearMarkers()
        val wm = windowManager ?: return
        for (p in predictions) {
            val iv = View(this)
            iv.background = resources.getDrawable(android.R.drawable.btn_default_small, null)
            val size = Math.max(60, Math.min(160, (Math.max(p.rect.width(), p.rect.height()) * 1.2).toInt()))
            val params = WindowManager.LayoutParams(
                size,
                size,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START
            params.x = p.rect.centerX() - size/2
            params.y = p.rect.centerY() - size/2
            wm.addView(iv, params)
            drawnMarkers.add(iv)
        }
    }

    private fun clearMarkers() {
        val wm = windowManager ?: return
        for (v in drawnMarkers) {
            try { wm.removeView(v) } catch (_: Exception) {}
        }
        drawnMarkers.clear()
    }

    override fun onDestroy() {
        try { if (floatingView != null) windowManager?.removeView(floatingView) } catch (_: Exception) {}
        clearMarkers()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
