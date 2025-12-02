package com.wakala.fakhr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etUser = findViewById<EditText>(R.id.et_username)
        val etPass = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_start_overlay)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val res = ApiClient.service.login(mapOf("username" to user, "password" to pass))
                    // handle token - simple flow
                    val token = res["access_token"] as? String
                    if (token != null) {
                        // save token to shared prefs (omitted here)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        runOnUiThread { Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@LoginActivity, "Login error", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }
}
