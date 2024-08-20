package com.example.eclair_project2.fragment.home.chatgpt_api


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance { 
    private const val API_KEY = ""

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 시간 설정
        .readTimeout(60, TimeUnit.SECONDS) // 읽기 타임아웃 시간 설정
        .writeTimeout(60, TimeUnit.SECONDS) // 쓰기 타임아웃 시간 설정
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: EmotionAnalysisApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmotionAnalysisApiService::class.java)
    }
}