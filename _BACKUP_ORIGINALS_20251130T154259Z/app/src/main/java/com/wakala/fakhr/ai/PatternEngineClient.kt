package com.wakala.fakhr.ai

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors

object PatternEngineClient {
    private val client = OkHttpClient()
    private val exec = Executors.newSingleThreadExecutor()
    var serverUrl = "http://YOUR_SERVER_HOST:8000"

    fun getHistory(limit:Int = 50, cb:(IntArray?)->Unit) {
        exec.submit {
            try {
                val req = Request.Builder().url(serverUrl + "/rounds/history?limit=" + limit).build()
                val resp = client.newCall(req).execute()
                val s = resp.body?.string()
                resp.close()
                if (s != null) {
                    val jo = JSONObject(s)
                    val arr = jo.getJSONArray("history")
                    val out = IntArray(arr.length())
                    for (i in 0 until arr.length()) out[i] = arr.getInt(i)
                    cb(out)
                    return@submit
                }
            } catch (e: Exception) { }
            cb(null)
        }
    }

    fun getPrediction(limit:Int = 50, minute:Int? = null, cb:(JSONObject?)->Unit) {
        exec.submit {
            try {
                var url = serverUrl + "/prediction/pro?limit=" + limit
                if (minute != null) url += "&minute=" + minute
                val req = Request.Builder().url(url).build()
                val resp = client.newCall(req).execute()
                val s = resp.body?.string(); resp.close()
                if (s != null) cb(JSONObject(s)) else cb(null)
            } catch (e: Exception) { cb(null) }
        }
    }
}
