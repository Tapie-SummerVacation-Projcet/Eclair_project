package com.example.eclair_project2.fragment.solution

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SolutionWriteScreen(navController: NavController, diaryId: String) {
    var solution by remember { mutableStateOf("") }
    var selectedDiary by remember { mutableStateOf<Pair<String, String>?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var showDiaryList by remember { mutableStateOf(false) }
    val diaries = remember { mutableStateListOf<Pair<String, String>>() } // (title, id) 형태의 리스트

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference.child("solutions")

    // 다이어리 목록 가져오기
    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseDatabase.getInstance().reference.child("diaries").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        diaries.clear()
                        if (snapshot.exists()) {
                            Log.d("SolutionWriteScreen", "Diary snapshot exists for userId: $userId")
                            snapshot.children.forEach { diarySnapshot ->
                                val title = diarySnapshot.child("title").getValue(String::class.java)
                                val key = diarySnapshot.key
                                if (title != null && key != null) {
                                    diaries.add(title to key)
                                    Log.d("SolutionWriteScreen", "Loaded diary: $title, ID: $key")
                                }
                            }
                        } else {
                            Log.d("SolutionWriteScreen", "No diaries found for userId: $userId")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SolutionWriteScreen", "Failed to load diaries: ${error.message}")
                    }
                })

            // 로그로 데이터를 확인
            Log.d("SolutionWriteScreen", "Total diaries loaded: ${diaries.size}")
        } else {
            Log.e("SolutionWriteScreen", "UserId is null.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "해결 방법 작성",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 다이어리 선택 리스트
        Text(
            text = selectedDiary?.first ?: "다이어리 선택",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDiaryList = !showDiaryList
                    Log.d("SolutionWriteScreen", "Diary selection toggled: $showDiaryList")
                }
                .padding(vertical = 8.dp),
            fontSize = 18.sp,
            color = if (selectedDiary != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        if (showDiaryList) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                items(diaries) { diary ->
                    Text(
                        text = diary.first,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDiary = diary
                                showDiaryList = false
                                Log.d("SolutionWriteScreen", "Diary selected: ${diary.first}")
                            }
                            .padding(8.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = solution,
            onValueChange = { solution = it },
            label = { Text("해결 방법") },
            placeholder = { Text("해결 방법을 입력하세요.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (solution.isNotEmpty() && selectedDiary != null) {
                    val solutionId = database.push().key
                    if (userId != null && solutionId != null) {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        val solutionData = Solution(
                            userId,
                            selectedDiary!!.second,
                            selectedDiary!!.first,
                            solution,
                            currentDate
                        )

                        database.child(userId).child(solutionId).setValue(solutionData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("emotion/${diaries.joinToString(",") { it.second }}")
                                } else {
                                    errorMessage = "저장 실패: ${task.exception?.localizedMessage}"
                                }
                            }
                            .addOnFailureListener { exception ->
                                errorMessage = "오류 발생: ${exception.localizedMessage}"
                            }
                    }
                } else {
                    errorMessage = "해결 방법을 입력하고 일기를 선택해주세요."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "작성 완료", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}