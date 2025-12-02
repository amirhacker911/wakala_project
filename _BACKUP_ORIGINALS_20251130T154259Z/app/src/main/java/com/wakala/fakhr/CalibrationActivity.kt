package com.wakala.fakhr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.graphics.PointF
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import java.io.File

class CalibrationActivity : AppCompatActivity() {

    private val points = mutableListOf<PointF>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)

        val btnSave = findViewById<Button>(R.id.btn_save_points)
        btnSave.setOnClickListener {
            val arr = points.map { mapOf("x" to it.x.toInt(), "y" to it.y.toInt()) }
            val f = File(filesDir, "calibration.json")
            f.writeText(Gson().toJson(arr))
            Toast.makeText(this, "Calibration saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            points.add(PointF(event.x, event.y))
        }
        return super.onTouchEvent(event)
    }
}
