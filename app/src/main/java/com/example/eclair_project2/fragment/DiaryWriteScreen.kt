package com.example.eclair_project2.fragment

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.navigation.Screen
import com.example.eclair_project2.ui.theme.Pink40
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun DiaryWriteScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("diaries")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "일기 작성",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            placeholder = { Text("제목을 입력하세요.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            placeholder = { Text("내용을 입력하세요.") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
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
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val diaryId = database.push().key // 고유 키 생성
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val diary = Diary(userId, title, content, currentDate)

                        Log.d("DiaryWriteScreen", "일단 버튼 클릭을 한: $diaryId")

                        if (diaryId != null) {
                            Log.d("DiaryWriteScreen", "일단 버튼 클릭을 한: $diaryId")
                            database.child(userId).child(diaryId).setValue(diary)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("DiaryWriteScreen", "일기 작성 성공: $diaryId")
                                        navController.navigate(Screen.Diary.route)
                                    } else {
                                        errorMessage = "일기 저장 실패: ${task.exception?.localizedMessage}"
                                        Log.e("DiaryWriteScreen", "일기 저장 실패: ${task.exception?.localizedMessage}")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("DiaryWriteScreen", "일기 저장 중 오류 발생: ${exception.localizedMessage}")
                                    errorMessage = "일기 저장 중 오류 발생: ${exception.localizedMessage}"
                                }
                            Log.d("DiaryWriteScreen", "일단 버튼 클릭을 헸고 id 값도 존재하지만 이상하게 일기 작성 성공 부분을 실행하지 못함: $diaryId")
                        }
                    } else {
                        errorMessage = "로그인이 필요합니다."
                        Log.e("DiaryWriteScreen", "로그인되지 않은 상태에서 일기 작성 시도")
                    }
                } else {
                    errorMessage = "모든 필드를 입력해주세요."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40)
        ) {
            Text(text = "작성 완료", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}