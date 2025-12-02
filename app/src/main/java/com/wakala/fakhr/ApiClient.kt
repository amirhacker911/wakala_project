package com.wakala.fakhr

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.FieldMap

object ApiClient {
    private var BASE = "http://10.0.2.2:8000/"
    fun init(base: String) { BASE = base }
    val retrofit: Retrofit = Retrofit.Builder().baseUrl(BASE).addConverterFactory(GsonConverterFactory.create()).build()
    interface ApiService {
        @POST("auth/login")
        suspend fun login(@Body body: Map<String, String>): Map<String, Any>
        @POST("game/upload")
        suspend fun upload(@Body body: Map<String, Any>): Map<String, Any>
        @POST("game/predict")
        suspend fun predict(@Body body: Map<String, Any>): Map<String, Any>
        @GET("game/tflite_model")
        suspend fun downloadModel(): retrofit2.Response<okhttp3.ResponseBody>
    }
    val service: ApiService = retrofit.create(ApiService::class.java)
}
