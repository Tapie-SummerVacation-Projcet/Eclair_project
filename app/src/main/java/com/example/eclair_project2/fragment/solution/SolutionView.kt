package com.example.eclair_project2.fragment.solution

import android.util.Log
import androidx.compose.foundation.layout.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch

@Composable
fun SolutionEditScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val auth = FirebaseAuth.getInstance() // FirebaseAuth 인스턴스 가져오기

    val solutionJson = backStackEntry.arguments?.getString("solutionJson")
    val gson = Gson()
    var solution by remember {
        mutableStateOf(
            try {
                gson.fromJson(solutionJson, Solution::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("SolutionEditScreen", "Failed to parse Solution JSON", e)
                Solution() // 기본값 설정
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val database = FirebaseDatabase.getInstance().reference.child("solutions")

    LaunchedEffect(solution) {
        Log.d("SolutionEditScreen", "Solution Loaded: Title - ${solution.diaryTitle}, Content - ${solution.solution}, Date - ${solution.date}")
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (solution == null) {
            Text(text = "Loading...", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(text = "Fetching solution details...", fontSize = 18.sp)
        } else {
            Text(text = "해결 일지 수정", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = solution.diaryTitle,
                onValueChange = { solution = solution.copy(diaryTitle = it) },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = solution.solution,
                onValueChange = { solution = solution.copy(solution = it) },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = auth.currentUser?.uid
                    if (userId != null && solution.key != null) {
                        // 기존 해결 일지를 동일한 키로 업데이트합니다.
                        database.child(userId).child(solution.key!!).setValue(solution)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("SolutionEditScreen", "Solution Saved: Title - ${solution.diaryTitle}, Content - ${solution.solution}, Date - ${solution.date}")
                                    navController.popBackStack()
                                } else {
                                    Log.e("SolutionEditScreen", "Failed to save solution: ${task.exception?.message}")
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "수정 완료")
            }
        }
    }
}

