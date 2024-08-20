package com.example.eclair_project2.fragment.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eclair_project2.fragment.home.chatgpt_api.EmotionAnalysisScreen
import com.example.eclair_project2.fragment.home.chatgpt_api.analyzeEmotion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    val emotionScores = remember { mutableStateMapOf<String, Int>() }
    val diaryEntries = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        startEmotionAnalysis(emotionScores, diaryEntries)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "홈 화면", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // 감정 분석 상태를 화면에 표시
        EmotionAnalysisScreen(emotionScores = emotionScores)
    }
}

suspend fun startEmotionAnalysis(emotionScores: MutableMap<String, Int>, diaryEntries: MutableList<String>) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val database = FirebaseDatabase.getInstance().reference.child("diaries").child(userId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                diaryEntries.clear()
                for (diarySnapshot in snapshot.children) {
                    val content = diarySnapshot.child("content").getValue(String::class.java)
                    content?.let { diaryEntries.add(it) }
                }

                if (diaryEntries.isNotEmpty()) {
                    GlobalScope.launch {
                        val analysisResult = analyzeEmotion(diaryEntries)
                        val newEmotionScores = parseEmotionScores(analysisResult)

                        val existingEmotionScores = fetchExistingEmotionScores(userId)
                        val combinedScores = combineEmotionScores(existingEmotionScores, newEmotionScores)

                        saveEmotionScores(userId, combinedScores)

                        emotionScores.clear()
                        emotionScores.putAll(combinedScores)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeScreen", "Failed to fetch diary entries: ${error.message}")
            }
        })
    } else {
        Log.e("HomeScreen", "UserId is null.")
    }
}


private fun parseEmotionScores(analysisResult: String): Map<String, Int> {
    val emotionScores = mutableMapOf<String, Int>()
    analysisResult.lines().forEach { line ->
        val parts = line.split(": ")
        if (parts.size == 2) {
            val emotion = parts[0].trim()
            val score = parts[1].trim().replace("/50", "").toIntOrNull() ?: 0
            emotionScores[emotion] = score
        }
    }
    return emotionScores
}

private suspend fun fetchExistingEmotionScores(userId: String): Map<String, Int> {
    val database = FirebaseDatabase.getInstance().reference.child("emotionScores").child(userId)
    val existingScores = mutableMapOf<String, Int>()

    val snapshot = database.get().await()
    for (emotionSnapshot in snapshot.children) {
        val emotion = emotionSnapshot.key ?: continue
        val score = emotionSnapshot.getValue(Int::class.java) ?: continue
        existingScores[emotion] = score
    }

    return existingScores
}

private fun combineEmotionScores(existingScores: Map<String, Int>, newScores: Map<String, Int>): Map<String, Int> {
    val combinedScores = mutableMapOf<String, Int>()

    newScores.forEach { (emotion, newScore) ->
        val existingScore = existingScores[emotion] ?: 0
        val combinedScore = (existingScore + newScore) / 2  // 평균을 계산
        combinedScores[emotion] = combinedScore
    }

    return combinedScores
}

private fun saveEmotionScores(userId: String, combinedScores: Map<String, Int>) {
    val database = FirebaseDatabase.getInstance().reference.child("emotionScores").child(userId)
    database.setValue(combinedScores).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("saveEmotionScores", "Emotion scores saved successfully.")
        } else {
            Log.e("saveEmotionScores", "Failed to save emotion scores: ${task.exception?.localizedMessage}")
        }
    }
}