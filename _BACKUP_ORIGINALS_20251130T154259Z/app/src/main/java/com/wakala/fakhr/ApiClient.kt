package com.wakala.fakhr

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor { message ->
            // Minimal logging; don't log sensitive info
            android.util.Log.d("ApiClient", message)
        }
        logger.level = HttpLoggingInterceptor.Level.BASIC
        return logger
    }

    private fun authInterceptor(apiKey: String?): Interceptor {
        return Interceptor { chain ->
            val reqBuilder = chain.request().newBuilder()
            apiKey?.takeIf { it.isNotBlank() }?.let {
                reqBuilder.addHeader("Authorization", "Bearer $it")
            }
            reqBuilder.addHeader("Accept", "application/json")
            chain.proceed(reqBuilder.build())
        }
    }

    fun create(context: Context): ApiService {
        val apiKey = BuildConfig.API_KEY.takeIf { it.isNotBlank() }
        val ok = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor(apiKey))
            .addInterceptor(loggingInterceptor())
            .retryOnConnectionFailure(true)
            .build()

        val base = BuildConfig.BACKEND_BASE_URL

        val retrofit = Retrofit.Builder()
            .baseUrl(base)
            .addConverterFactory(GsonConverterFactory.create())
            .client(ok)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
