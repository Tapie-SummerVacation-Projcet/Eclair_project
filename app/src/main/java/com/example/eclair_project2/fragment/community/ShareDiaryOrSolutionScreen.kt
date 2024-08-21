package com.example.eclair_project2.fragment.community

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.components.icon.Plus
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShareDiaryOrSolutionScreen(navController: NavController) {
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var selectedDiary by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedSolution by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showDiaryList by remember { mutableStateOf(false) }
    var showSolutionList by remember { mutableStateOf(false) }
    val diaries = remember { mutableStateListOf<Pair<String, String>>() }
    val solutions = remember { mutableStateListOf<Pair<String, String>>() }

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val userName = auth.currentUser?.displayName ?: "Anonymous"
    val database = FirebaseDatabase.getInstance().reference.child("sharedContent")

    LaunchedEffect(userId) {
        if (userId != null) {
            // Load diaries
            FirebaseDatabase.getInstance().reference.child("diaries").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        diaries.clear()
                        for (diarySnapshot in snapshot.children) {
                            val title = diarySnapshot.child("title").getValue(String::class.java)
                            val id = diarySnapshot.key ?: ""
                            if (title != null) {
                                diaries.add(title to id)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ShareScreen", "Failed to load diaries: ${error.message}")
                    }
                })

            // Load solutions
            FirebaseDatabase.getInstance().reference.child("solutions").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        solutions.clear()
                        for (solutionSnapshot in snapshot.children) {
                            val title = solutionSnapshot.child("diaryTitle").getValue(String::class.java)
                            val id = solutionSnapshot.key ?: ""
                            if (title != null) {
                                solutions.add(title to id)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ShareScreen", "Failed to load solutions: ${error.message}")
                    }
                })
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                Text(
                    text = "커뮤니티에 공유",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (diaries.isNotEmpty()) {
                    Text(
                        text = selectedDiary?.first ?: "다이어리 선택",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDiaryList = !showDiaryList }
                            .padding(vertical = 8.dp),
                        fontSize = 18.sp,
                        color = if (selectedDiary != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    if (showDiaryList) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((diaries.size.coerceAtMost(2) * 56).dp)
                        ) {
                            items(diaries.take(2)) { diary ->
                                Text(
                                    text = diary.first,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedDiary = diary
                                            showDiaryList = false
                                        }
                                        .padding(8.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (solutions.isNotEmpty()) {
                    Text(
                        text = selectedSolution?.first ?: "솔루션 선택",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSolutionList = !showSolutionList }
                            .padding(vertical = 8.dp),
                        fontSize = 18.sp,
                        color = if (selectedSolution != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    if (showSolutionList) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((solutions.size.coerceAtMost(2) * 56).dp)
                        ) {
                            items(solutions.take(2)) { solution ->
                                Text(
                                    text = solution.first,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSolution = solution
                                            showSolutionList = false
                                        }
                                        .padding(8.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    placeholder = { Text("공유할 설명을 입력하세요.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (description.isNotEmpty() && (selectedDiary != null || selectedSolution != null)) {
                            if (userId != null) {
                                val sharedContentId = database.push().key
                                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                                val originalId = selectedDiary?.second ?: selectedSolution?.second ?: ""

                                val sharedContent = SharedContent(
                                    title = selectedDiary?.first ?: selectedSolution?.first ?: "",
                                    description = description,
                                    userName = userName,
                                    date = currentDate,
                                    originalId = originalId // Store the original diary/solution ID
                                )

                                if (sharedContentId != null) {
                                    database.child(sharedContentId).setValue(sharedContent)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d("ShareDiaryOrSolutionScreen", "Content shared successfully: $sharedContent")
                                                navController.popBackStack()
                                            } else {
                                                errorMessage = "공유 실패: ${task.exception?.localizedMessage}"
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            errorMessage = "공유 중 오류 발생: ${exception.localizedMessage}"
                                        }
                                }
                            } else {
                                errorMessage = "로그인이 필요합니다."
                            }
                        } else {
                            errorMessage = "설명과 항목을 모두 입력해주세요."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "공유하기", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    )
}