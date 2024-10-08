package com.example.eclair_project2.fragment.diary

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.eclair_project2.fragment.diary.chatgpt_api.SolutionAi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch


@Composable
fun DiaryScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val auth = FirebaseAuth.getInstance() // FirebaseAuth instance
    val diaryJson = backStackEntry.arguments?.getString("diaryJson")
    val gson = Gson()

    var diary by remember {
        mutableStateOf(
            try {
                gson.fromJson(diaryJson, Diary::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("DiaryScreen", "Failed to parse Diary JSON", e)
                Diary() // Default value
            }
        )
    }

    var solution by remember { mutableStateOf<String?>(diary.solution) } // Initialize with existing solution if available
    var isLoading by remember { mutableStateOf(false) } // Loading state for AI solution
    val coroutineScope = rememberCoroutineScope()
    val database = FirebaseDatabase.getInstance().reference.child("diaries")

    LaunchedEffect(diary) {
        Log.d("DiaryScreen", "Diary Loaded: Title - ${diary.title}, Content - ${diary.content}, Date - ${diary.date}")
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            if (diary == null) {
                Text(text = "Loading...", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
                Text(text = "Fetching diary details...", fontSize = 18.sp)
            } else {
                Text(text = "일기 수정", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

                OutlinedTextField(
                    value = diary.title,
                    onValueChange = { diary = diary.copy(title = it) },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = diary.content,
                    onValueChange = { diary = diary.copy(content = it) },
                    label = { Text("내용") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val userId = auth.currentUser?.uid
                        if (userId != null && diary.key != null) {
                            database.child(userId).child(diary.key!!).setValue(diary)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("DiaryScreen", "Diary Saved: Title - ${diary.title}, Content - ${diary.content}, Date - ${diary.date}")
                                    } else {
                                        Log.e("DiaryScreen", "Failed to save diary: ${task.exception?.message}")
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "수정 완료")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val userId = auth.currentUser?.uid
                        if (userId != null && diary.key != null) {
                            isLoading = true // Set loading state to true
                            Log.d("DiaryScreen", "AI 솔루션 요청 중...")
                            coroutineScope.launch {
                                try {
                                    val fetchedSolution = SolutionAi(diary.key ?: "", diary.content)
                                    solution = fetchedSolution
                                    diary = diary.copy(solution = fetchedSolution)
                                    database.child(userId).child(diary.key!!).setValue(diary)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d("DiaryScreen", "AI 솔루션 받은 후 저장 완료: $fetchedSolution")
                                            } else {
                                                Log.e("DiaryScreen", "Failed to save AI solution: ${task.exception?.message}")
                                            }
                                        }
                                } finally {
                                    isLoading = false // Set loading state to false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "AI 솔루션 받기")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    // Show loading text while fetching the AI solution
                    Text(
                        text = "AI 솔루션 가져오는 중...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    solution?.let {
                        if (it.isNotEmpty()) {
                            Text(text = "AI 솔루션:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = it, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}