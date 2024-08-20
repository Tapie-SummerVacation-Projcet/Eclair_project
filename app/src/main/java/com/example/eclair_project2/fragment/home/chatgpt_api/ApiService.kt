package com.example.eclair_project2.fragment.home.chatgpt_api


import com.example.eclair_project2.fragment.diary.chatgpt_api.SolutionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EmotionAnalysisApiService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun analyzeEmotion(@Body request: EmotionAnalysisRequest): Response<EmotionAnalysisResponse>

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun generateResponse(@Body request: SolutionRequest): Response<EmotionAnalysisResponse>
}