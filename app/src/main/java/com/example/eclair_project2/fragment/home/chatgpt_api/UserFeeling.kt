package com.example.eclair_project2.fragment.home.chatgpt_api

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
            Message(role = "system", content = "당신은 감정 분석을 제공하는 전문가입니다."),
            Message(role = "system", content = """
                사용자가 작성한 일기 내용을 분석해서 다음 6가지 감정 상태를 각각 50점을 기준으로 20~50점 사이에서 평가하세요.
                - 행복
                - 외로움
                - 짜증
                - 좌절
                - 걱정
                - 스트레스
                
                응답 형식은 다음과 같습니다:
                - 행복: [점수]/50
                - 외로움: [점수]/50
                - 짜증: [점수]/50
                - 좌절: [점수]/50
                - 걱정: [점수]/50
                - 스트레스: [점수]/50

                또한 분석 결과 외에는 다른 어떠한 내용도 포함하지 마십시오.
            """.trimIndent()),
            Message(role = "user", content = "다음은 사용자가 작성한 일기와 해결 일지입니다:\n$diaryContent")
        ),
        max_tokens = 1024,
        temperature = 0.4
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