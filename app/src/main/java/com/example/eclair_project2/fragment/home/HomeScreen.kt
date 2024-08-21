package com.example.eclair_project2.fragment.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.eclair_project2.fragment.home.chatgpt_api.analyzeEmotion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val EmotionColors = mapOf(
    "행복" to Color(0xFFFFEB3B),
    "외로움" to Color(0xFF2196F3),
    "짜증" to Color(0xFFF44336),
    "좌절" to Color(0xFF9C27B0),
    "걱정" to Color(0xFF3F51B5),
    "스트레스" to Color(0xFFFF9800)
)

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel = remember { HomeViewModel() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadEmotionData()
        viewModel.loadUserName() // Load the user's name
    }

    // Observe the emotion scores to determine if data is still loading
    LaunchedEffect(viewModel.emotionScores) {
        if (viewModel.emotionScores.isNotEmpty()) {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Display the emotion analysis screen
                EmotionAnalysisScreen(
                    emotionScores = viewModel.emotionScores,
                    userName = viewModel.userName // Pass the user name to the screen
                )
            }
        }
    }
}

@Composable
fun EmotionAnalysisScreen(emotionScores: Map<String, Int>, userName: String?) {
    val overallEmotion = determineOverallEmotion(emotionScores)
    val overallEmotionText = "$overallEmotion 편이에요!"
    val increaseText = "어제보다 ${overallEmotion}이 12% 늘었어요."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${userName ?: "OOO"}님은 현재", // Use the user's name if available
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = overallEmotionText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = getEmotionColor(overallEmotion)
            )
        )
        Text(
            text = increaseText,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        emotionScores.forEach { (emotion, score) ->
            EmotionBar(emotion = emotion, score = score)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EmotionBar(emotion: String, score: Int) {
    val color = getEmotionColor(emotion)
    Log.d("EmotionColorCheck", "Emotion: $emotion, Assigned Color: $color") // Log the color for debugging

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("EmotionBar", "Emotion: $emotion, Score: $score, Color: $color")
            }
    ) {
        Text(
            text = emotion,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp), // Further reduced font size
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(3.0f)
                .height(16.dp) // Reduced height of the bar
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp)) // Adjusted corner radius
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = score / 100f) // Scale width to show score as bar length (assuming score is out of 100)
                    .background(color, shape = RoundedCornerShape(8.dp)) // Adjusted corner radius
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$score",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp), // Reduced font size for the score
            modifier = Modifier.weight(1f)
        )
    }
}

fun determineOverallEmotion(emotionScores: Map<String, Int>): String {
    val highestEmotion = emotionScores.maxByOrNull { it.value }
    return highestEmotion?.key ?: "Unknown"
}

fun getEmotionColor(emotion: String): Color {
    val cleanedEmotion = emotion
        .replace("^[\\-\\s]+".toRegex(), "") // Remove leading hyphens and spaces
        .trim() // Trim any surrounding whitespace

    Log.d("EmotionColorCheck", "Cleaned Emotion: $cleanedEmotion") // Log the cleaned emotion for debugging

    return EmotionColors[cleanedEmotion] ?: Color.Gray
}

// ViewModel for handling data loading
class HomeViewModel : ViewModel() {
    var emotionScores by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var userName by mutableStateOf<String?>(null) // Mutable state for storing the user's name

    fun loadEmotionData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference.child("diaries").child(userId)

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val diaryEntries = mutableListOf<String>()
                    for (diarySnapshot in snapshot.children) {
                        val content = diarySnapshot.child("content").getValue(String::class.java)
                        content?.let { diaryEntries.add(it) }
                    }

                    if (diaryEntries.isNotEmpty()) {
                        viewModelScope.launch {
                            val analysisResult = analyzeEmotion(diaryEntries)
                            val newEmotionScores = parseEmotionScores(analysisResult)

                            val existingEmotionScores = fetchExistingEmotionScores(userId)
                            val combinedScores = combineEmotionScores(existingEmotionScores, newEmotionScores)

                            saveEmotionScores(userId, combinedScores)
                            emotionScores = combinedScores
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeViewModel", "Failed to fetch diary entries: ${error.message}")
                }
            })
        } else {
            Log.e("HomeViewModel", "UserId is null.")
        }
    }

    fun loadUserName() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userName = currentUser?.displayName ?: "OOO" // Get the user's display name or fallback to "OOO"
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
                Log.d("HomeViewModel", "Emotion scores saved successfully.")
            } else {
                Log.e("HomeViewModel", "Failed to save emotion scores: ${task.exception?.localizedMessage}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val testScores = mapOf(
        "행복" to 70,
        "외로움" to 30,
        "짜증" to 50,
        "좌절" to 20,
        "걱정" to 40,
        "스트레스" to 80
    )
    EmotionAnalysisScreen(
        emotionScores = testScores,
        userName = "테스트 사용자" // Simulated user name for preview
    )
}