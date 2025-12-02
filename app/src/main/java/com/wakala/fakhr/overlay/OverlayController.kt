
package com.wakala.fakhr.overlay

import android.content.Context
import android.view.WindowManager
import android.view.View
import android.widget.FrameLayout

class OverlayController(private val ctx: Context) {
    // placeholder: create floating view and control overlay lifecycle
    private var root: FrameLayout? = null
    fun start() { /* create overlay view and attach to WindowManager */ }
    fun stop() { /* remove overlay */ }
    fun captureArea(): View? { return root }
}
