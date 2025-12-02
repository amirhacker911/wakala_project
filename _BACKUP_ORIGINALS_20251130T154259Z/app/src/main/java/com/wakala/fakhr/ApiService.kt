package com.wakala.fakhr

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class UploadSampleRequest(val data: String, val metadata: Map<String, String>)
data class UploadSampleResponse(val ok: Boolean, val id: String?)
data class HealthCheckResponse(val status: String)

interface ApiService {
    @GET("health")
    suspend fun health(): Response<HealthCheckResponse>

    @POST("samples/upload")
    suspend fun uploadSample(@Body body: UploadSampleRequest): Response<UploadSampleResponse>
}

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST

interface DatasetApi {
    @Multipart
    @POST("datasets/upload")
    suspend fun uploadDataset(@Part filePart: MultipartBody.Part): retrofit2.Response<Void>
}
