package com.wakala.fakhr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class ScreenCaptureActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CODE = 5466
    }

    private var projection: MediaProjection? = null
    private var projectionManager: MediaProjectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = projectionManager?.createScreenCaptureIntent()
        startActivityForResult(intent, REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            projection = projectionManager?.getMediaProjection(resultCode, data)
            // capture a single frame in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    captureOnce()
                } catch (e: Exception) {
                    Log.e("ScreenCaptureAct", "capture failed", e)
                } finally {
                    projection?.stop()
                    projection = null
                    runOnUiThread { finish() }
                }
            }
        }
    }

    private fun captureOnce() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        val density = dm.densityDpi

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
        val virtualDisplay = projection?.createVirtualDisplay(
            "screencap",
            width, height, density,
            0, imageReader.surface, null, null
        )
        // wait for image
        var img = imageReader.acquireLatestImage()
        var tries = 0
        while (img == null && tries < 20) {
            Thread.sleep(50)
            img = imageReader.acquireLatestImage()
            tries++
        }
        if (img == null) throw IllegalStateException("No image captured")
        val plane = img.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(buffer)
        val finalBmp = Bitmap.createBitmap(bmp, 0, 0, width, height)
        img.close()
        // save to filesDir
        val file = File(filesDir, "screencap_latest.png")
        val fos = FileOutputStream(file)
        finalBmp.compress(Bitmap.CompressFormat.PNG, 90, fos)
        fos.flush()
        fos.close()
        imageReader.close()
        virtualDisplay?.release()
    }
}
