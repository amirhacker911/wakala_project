package com.wakala.fakhr

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wakala.fakhr.data.DatasetManager
import java.io.File

class DataLabelActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var edtLabel: EditText
    private lateinit var edtRound: EditText
    private lateinit var edtMatch: EditText
    private lateinit var btnSave: Button
    private var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label)

        imgView = findViewById(R.id.sample_image)
        edtLabel = findViewById(R.id.edt_label)
        edtRound = findViewById(R.id.edt_round)
        edtMatch = findViewById(R.id.edt_match)
        btnSave = findViewById(R.id.btn_save_label)

        // find next unlabeled sample in dataset
        val dsRoot = File(filesDir, "dataset")
        val next = findNextUnlabeled(dsRoot)
        if (next != null) {
            currentFile = next
            imgView.setImageBitmap(BitmapFactory.decodeFile(next.absolutePath))
        } else {
            Toast.makeText(this, "No unlabeled samples found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        btnSave.setOnClickListener {
            val label = edtLabel.text.toString().trim()
            val round = edtRound.text.toString().toIntOrNull() ?: 0
            val match = edtMatch.text.toString().toIntOrNull() ?: 0
            if (label.isEmpty()) {
                Toast.makeText(this, "Enter label", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // write metadata JSON next to image, or update existing JSON
            val jsonFile = File(currentFile!!.parentFile, currentFile!!.name.replace(".png", ".json"))
            val meta = org.json.JSONObject()
            meta.put("label", label)
            meta.put("round", round)
            meta.put("match", match)
            jsonFile.writeText(meta.toString())
            Toast.makeText(this, "Saved label", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun findNextUnlabeled(root: File): File? {
        if (!root.exists()) return null
        val files = root.walkTopDown().filter { it.isFile && it.extension.toLowerCase() == "png" }.toList()
        for (f in files) {
            val j = File(f.parentFile, f.name.replace(".png", ".json"))
            if (!j.exists() || org.json.JSONObject(j.readText()).optString("label").isNullOrEmpty()) {
                return f
            }
        }
        return null
    }
}
