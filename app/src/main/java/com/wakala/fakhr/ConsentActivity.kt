package com.wakala.fakhr

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class ConsentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)
        val tv = findViewById<TextView>(R.id.tv_consent_text)
        val chk = findViewById<CheckBox>(R.id.chk_consent)
        val btn = findViewById<Button>(R.id.btn_accept)
        tv.text = "أوافق بأن يتم جمع بيانات اللعبة (صور، نتائج، رهانات) لاستخدامها في تحسين نموذج التنبؤ. جميع الحقوق محفوظة لوكالة فخر العرب."
        btn.setOnClickListener {
            if (!chk.isChecked) {
                Toast.makeText(this, "يرجى الموافقة للاستمرار", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            val prefs = getSharedPreferences("wakala_prefs", MODE_PRIVATE)
            val token = prefs.getString("auth_token", null) ?: run { Toast.makeText(this, "لا يوجد تسجيل دخول", Toast.LENGTH_SHORT).show(); finish(); return@setOnClickListener }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val form = FormBody.Builder().add("token", token).add("consent", "true").build()
                    val req = Request.Builder().url(com.wakala.fakhr.Config.BASE_URL + "consent").post(form).build()
                    val resp = client.newCall(req).execute()
                    val body = resp.body?.string() ?: ""
                    val jo = org.json.JSONObject(body)
                    if (jo.optBoolean("ok")) {
                        prefs.edit().putBoolean("consent", true).apply()
                        runOnUiThread { Toast.makeText(this@ConsentActivity, "تم حفظ الموافقة", Toast.LENGTH_SHORT).show(); startActivity(android.content.Intent(this@ConsentActivity, MainActivity::class.java)); finish() }
                    } else {
                        runOnUiThread { Toast.makeText(this@ConsentActivity, "فشل حفظ الموافقة", Toast.LENGTH_SHORT).show() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@ConsentActivity, "خطأ بالشبكة", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }
}
