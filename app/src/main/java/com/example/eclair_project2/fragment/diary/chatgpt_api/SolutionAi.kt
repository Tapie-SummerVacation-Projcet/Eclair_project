package com.example.eclair_project2.fragment.diary.chatgpt_api

import android.util.Log
import com.example.eclair_project2.fragment.home.chatgpt_api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun SolutionAi(diaryId: String, diaryContent: String): String {
    Log.d("SolutionAi", "Generating solution for diaryId: $diaryId")

    val request = SolutionRequest(
        model = "gpt-4",
        messages = listOf(
            Message(role = "system", content = "당신은 상세하고 정확한 해결책을 제공하는 전문가입니다."),
            Message(role = "system", content = """
                사용자의 일기 내용을 분석하고, 언급된 문제나 도전에 대한 해결책을 제시하세요.
                해결책은 명확하고 실행 가능하며, 사용자의 감정 상태를 고려하여 공감적으로 작성되어야 합니다.
                사용자가 상황을 개선하거나 마음가짐을 향상시킬 수 있도록 돕는 응답을 제공하세요.
            """.trimIndent()),
            Message(role = "user", content = "다음은 사용자의 일기 내용입니다:\n$diaryContent")
        ),
        max_tokens = 1000,
        temperature = 0.7
    )

    return withContext(Dispatchers.IO) {
        val response = RetrofitInstance.api.generateResponse(request) // You can rename this method to a more generic one like `generateResponse` to match its broader use.
        if (response.isSuccessful) {
            val result = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
            if (result.isNullOrEmpty()) {
                Log.d("SolutionAi", "Solution generation failed")
                "Failed to generate solution"
            } else {
                Log.d("SolutionAi", "Solution result: $result")
                result
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                val jsonError = JSONObject(errorBody)
                val error = jsonError.optJSONObject("error")
                error?.optString("message") ?: "Failed to generate solution"
            } else {
                "Failed to generate solution"
            }
            Log.e("SolutionAi", errorMessage ?: "Unknown error")
            errorMessage
        }
    }
}