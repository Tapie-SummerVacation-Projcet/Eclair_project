package com.example.eclair_project2.chatgpt_api

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmotionAnalysisScreen(emotionScores: Map<String, Int>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "심리 상태",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "사용자님은 현재",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = determineOverallEmotion(emotionScores),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 감정 점수를 순회하며 화면에 출력
        emotionScores.entries.forEach { (emotion, score) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = emotion,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                LinearProgressIndicator(
                    progress = score / 100f,
                    color = getEmotionColor(emotion),
                    modifier = Modifier
                        .height(8.dp)
                        .weight(4f)
                        .padding(start = 8.dp, end = 8.dp)
                )

                Text(
                    text = "$score",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
// 감정에 따른 색상을 결정하는 함수
fun getEmotionColor(emotion: String): Color {
    return when (emotion) {
        "행복" -> Color.Yellow
        "외로움" -> Color.Blue
        "짜증" -> Color.Red
        "좌절" -> Color(0xFF800080) // Custom Purple 색상
        "걱정" -> Color.Gray
        "스트레스" -> Color(0xFFFFA500) // Custom Orange 색상
        else -> Color.Gray
    }
}

// 전체 감정 상태를 결정하는 함수
fun determineOverallEmotion(emotionScores: Map<String, Int>): String {
    val highestEmotion = emotionScores.maxByOrNull { it.value }
    return highestEmotion?.key ?: "Unknown"
}

// 감정 점수를 로그로 출력하는 함수
fun logEmotionScores(emotionScores: Map<String, Int>) {
    for ((emotion, score) in emotionScores) {
        Log.d("EmotionAnalysis", "Emotion: $emotion, Score: $score")
    }
}