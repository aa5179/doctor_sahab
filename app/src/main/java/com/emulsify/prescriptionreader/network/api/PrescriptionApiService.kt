package com.emulsify.prescriptionreader.network.api

import com.emulsify.prescriptionreader.network.model.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface PrescriptionApiService {
    
    @Multipart
    @POST("upload")
    suspend fun uploadPrescription(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @Multipart
    @POST("test-upload")
    suspend fun testUploadPrescription(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @Multipart
    @POST("extract-text")
    suspend fun extractTextOnly(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @POST("query")
    suspend fun queryPrescription(
        @Body request: QueryRequest
    ): Response<QueryResponse>
    
    @POST("ask")
    suspend fun askQuestion(
        @Body request: AskRequest
    ): Response<AskResponse>
    
    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>
    
    @POST("embed")
    suspend fun embedDocument(
        @Body request: QueryRequest
    ): Response<QueryResponse>
}