package com.example.eclair_project2.chatgpt_api


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EmotionAnalysisApiService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun analyzeEmotion(@Body request: EmotionAnalysisRequest): Response<EmotionAnalysisResponse>
}