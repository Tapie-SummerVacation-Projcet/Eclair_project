package com.example.eclair_project2.fragment.diary.chatgpt_api


data class SolutionRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int,
    val temperature: Double
)

data class Message(
    val role: String,
    val content: String
)