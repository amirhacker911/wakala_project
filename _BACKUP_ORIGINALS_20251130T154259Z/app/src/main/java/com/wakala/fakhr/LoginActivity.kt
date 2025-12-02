package com.wakala.fakhr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val edtEmail = findViewById<EditText>(R.id.edt_email)
        val edtPass = findViewById<EditText>(R.id.edt_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        btnRegister.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtPass.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val form = FormBody.Builder().add("email", email).add("password", pass).build()
                    val req = Request.Builder().url(com.wakala.fakhr.Config.BASE_URL + "register").post(form).build()
                    val resp = client.newCall(req).execute()
                    val body = resp.body?.string()
                    runOnUiThread { Toast.makeText(this@LoginActivity, "Registered (please login)", Toast.LENGTH_SHORT).show() }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@LoginActivity, "Register failed", Toast.LENGTH_SHORT).show() }
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtPass.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val form = FormBody.Builder().add("email", email).add("password", pass).build()
                    val req = Request.Builder().url(com.wakala.fakhr.Config.BASE_URL + "login").post(form).build()
                    val resp = client.newCall(req).execute()
                    val body = resp.body?.string() ?: ""
                    val jo = org.json.JSONObject(body)
                    if (jo.optBoolean("ok")) {
                        val token = jo.optString("token")
                        val prefs = getSharedPreferences("wakala_prefs", MODE_PRIVATE)
                        prefs.edit().putString("auth_token", token).putString("auth_email", email).putBoolean("consent", jo.optBoolean("consent", false)).apply()
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            // if consent not given, open consent activity
                            val consent = jo.optBoolean("consent", false)
                            if (!consent) {
                                startActivity(Intent(this@LoginActivity, ConsentActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            }
                            finish()
                        }
                    } else {
                        runOnUiThread { Toast.makeText(this@LoginActivity, "Login failed: " + jo.optString("error"), Toast.LENGTH_SHORT).show() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@LoginActivity, "Login failed (network)", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }
}
