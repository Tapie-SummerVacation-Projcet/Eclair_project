package com.example.eclair_project2.fragment

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Solution(
    val userId: String = "",
    val diaryId: String = "",
    val diaryTitle: String = "",
    val solution: String = "",
    val date: String = "",
    var key: String? = null
)

@Composable
fun EmotionScreen(navController: NavController, diaryIdParams: String) {
    val diaryIds = diaryIdParams.split(",")
    val solutionListState = remember { mutableStateListOf<Solution>() }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Fetch solutions from Firebase for all diary IDs
    LaunchedEffect(diaryIds) {
        if (userId != null) {
            FirebaseDatabase.getInstance().reference
                .child("solutions")
                .child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        solutionListState.clear() // Clear the existing list
                        // 각 다이어리 ID에 맞는 솔루션만 필터링하여 추가
                        for (diaryId in diaryIds) {
                            snapshot.children.forEach { solutionSnapshot ->
                                val solution = solutionSnapshot.getValue(Solution::class.java)
                                if (solution != null && solution.diaryId == diaryId) {
                                    solution.key = solutionSnapshot.key
                                    solutionListState.add(solution)
                                }
                            }
                        }
                        Log.d("EmotionScreen", "Loaded solutions: ${solutionListState.size}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("EmotionScreen", "Failed to fetch solutions: ${error.message}")
                    }
                })
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("solutionWrite/${diaryIds.firstOrNull() ?: ""}")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Solution")
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "해결 일지 목록", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (solutionListState.isEmpty()) {
                    Text(text = "해결 방법이 없습니다.", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn {
                        items(solutionListState) { solution ->
                            SolutionItem(
                                solution = solution,
                                onDelete = {
                                    solution.key?.let { key ->
                                        userId?.let { uid ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("solutions")
                                                .child(uid)
                                                .child(key)
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    solutionListState.remove(solution)
                                                    Log.d("EmotionScreen", "Solution deleted successfully")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("EmotionScreen", "Failed to delete solution: ${e.message}")
                                                }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SolutionItem(solution: Solution, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "해결 방법 삭제") },
            text = { Text("정말로 이 해결 방법을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDialog = false
                }) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = solution.diaryTitle, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Text(text = solution.date, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = solution.solution, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}