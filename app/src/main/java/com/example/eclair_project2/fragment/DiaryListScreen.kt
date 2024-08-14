package com.example.eclair_project2.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun DiaryListScreen() {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "일기 목록", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn {
            items(diaryList.size) { index ->
                val diary = diaryList[index]
                DiaryItem(diary)
            }
        }
    }
}

@Composable
fun DiaryItem(diary: Diary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // 일기 상세 화면으로 이동 또는 일기 내용 보여주기 등의 처리
            }
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