package com.wakala.fakhr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class ScreenCapture(private val context: Context) {

    /**
     * captureOnce reads the latest screenshot saved by ScreenCaptureActivity
     * Returns Bitmap or null if not available.
     */
    fun captureOnce(): Bitmap? {
        try {
            val f = File(context.filesDir, "screencap_latest.png")
            if (!f.exists()) return null
            val bmp = BitmapFactory.decodeFile(f.absolutePath)
            return bmp
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
