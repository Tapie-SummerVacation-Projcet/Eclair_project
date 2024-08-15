package com.example.eclair_project2.fragment

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun SolutionListScreen(navController: NavController, diaryId: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    var solutionList by remember { mutableStateOf(listOf<Solution>()) }

    LaunchedEffect(userId, diaryId) {
        if (userId != null && diaryId.isNotEmpty()) {
            FirebaseDatabase.getInstance().reference
                .child("solutions")
                .child(userId)
                .orderByChild("diaryId")
                .equalTo(diaryId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("SolutionListScreen", "DataSnapshot children count: ${snapshot.childrenCount}")
                        val solutions = mutableListOf<Solution>()
                        for (solutionSnapshot in snapshot.children) {
                            val solution = solutionSnapshot.getValue(Solution::class.java)
                            solution?.key = solutionSnapshot.key
                            Log.d("SolutionListScreen", "Fetched solution: $solution")
                            if (solution != null) {
                                solutions.add(solution)
                            }
                        }
                        Log.d("SolutionListScreen", "Final solution list: $solutions")
                        solutionList = solutions
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SolutionListScreen", "Failed to fetch solutions: ${error.message}")
                    }
                })
        } else {
            Log.e("SolutionListScreen", "Invalid userId or diaryId")
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "해결 방법 목록", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (solutionList.isEmpty()) {
            Text(text = "해결 방법이 없습니다.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(solutionList) { solution ->
                    SolutionItem(solution = solution) {
                        // 여기서 해결 방법 클릭시 처리할 동작
                        Log.d("SolutionListScreen", "Clicked on solution: ${solution.solution}")
                    }
                }
            }
        }
    }
}

@Composable
fun SolutionItem(solution: Solution, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable { onClick() }) {
        Text(text = solution.diaryTitle, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = solution.solution, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}