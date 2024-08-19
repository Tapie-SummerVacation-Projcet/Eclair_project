package com.example.eclair_project2.chatgpt_api

import android.os.Message
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject


suspend fun analyzeEmotion(diaryEntries: List<String>): String {
    Log.d("analyzeEmotion", "Analyzing emotion for diary entries")

    val diaryContent = diaryEntries.joinToString(separator = "\n\n")

    val request = EmotionAnalysisRequest(
        model = "gpt-4",
        messages = listOf(
            Message(role = "system", content = "너는 상세하고 정확한 감정 분석을 제공하는 전문가야."),
            Message(role = "system", content = """
                사용자가 작성한 일기 내용을 분석해서 다음 6가지 감정 상태를 각각 50점 만점으로 평가해줘.
                - 행복
                - 외로움
                - 짜증
                - 좌절
                - 걱정
                - 스트레스
                
                응답 형식은 다음과 같아야 해:
                - 행복: [점수]/50
                - 외로움: [점수]/50
                - 짜증: [점수]/50
                - 좌절: [점수]/50
                - 걱정: [점수]/50
                - 스트레스: [점수]/50

                그리고 분석 결과 외에는 다른 어떤 정보도 포함시키지 마.
            """.trimIndent()),
            Message(role = "user", content = "다음은 사용자가 작성한 일기와 해결 일지입니다:\n$diaryContent")
        ),
        max_tokens = 1000,
        temperature = 0.7
    )

    return withContext(Dispatchers.IO) {
        val response = RetrofitInstance.api.analyzeEmotion(request)
        if (response.isSuccessful) {
            val result = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
            if (result.isNullOrEmpty()) {
                Log.d("analyzeEmotion", "Emotion analysis failed")
                "Failed to analyze emotion"
            } else {
                Log.d("analyzeEmotion", "Emotion analysis result: $result")
                result
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                val jsonError = JSONObject(errorBody)
                val error = jsonError.optJSONObject("error")
                error?.optString("message") ?: "Failed to analyze emotion"
            } else {
                "Failed to analyze emotion"
            }
            Log.e("analyzeEmotion", errorMessage ?: "Unknown error")
            errorMessage
        }
    }
}