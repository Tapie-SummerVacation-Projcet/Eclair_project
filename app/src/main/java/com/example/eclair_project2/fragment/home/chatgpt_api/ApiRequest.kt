package com.example.eclair_project2.fragment.home.chatgpt_api

data class EmotionAnalysisRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int,
    val temperature: Double
)

data class EmotionAnalysisResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)