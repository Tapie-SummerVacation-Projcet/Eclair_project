package com.example.eclair_project2.fragment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun DiaryListScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("diaries")
    val userId = auth.currentUser?.uid

    var diaryList by remember { mutableStateOf(listOf<Diary>()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val diaries = mutableListOf<Diary>()
                    for (diarySnapshot in snapshot.children) {
                        val diary = diarySnapshot.getValue(Diary::class.java)
                        if (diary != null) {
                            diary.key = diarySnapshot.key // Firebase의 key를 Diary 객체에 저장
                            diaries.add(diary)
                        }
                    }
                    diaryList = diaries
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = "일기 목록을 불러오는 데 실패했습니다: ${error.message}"
                }
            })
        } else {
            errorMessage = "로그인이 필요합니다."
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.DiaryWrite.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "일기 작성")
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(text = "일기 목록", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn {
                    items(diaryList.size) { index ->
                        val diary = diaryList[index]
                        DiaryItem(diary, onDelete = {
                            diary.key?.let { key -> // key가 null이 아닌 경우에만 삭제 수행
                                if (userId != null) {
                                    database.child(userId).child(key).removeValue().addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            errorMessage = "일기 삭제 실패: ${task.exception?.localizedMessage}"
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    )
}

@Composable
fun DiaryItem(diary: Diary, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "일기 삭제") },
            text = { Text("정말로 이 일기를 삭제하시겠습니까?") },
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
            .padding(8.dp)
            .clickable { showDialog = true }
    ) {
        Text(text = diary.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = diary.content, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = diary.date,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Divider()
    }
}