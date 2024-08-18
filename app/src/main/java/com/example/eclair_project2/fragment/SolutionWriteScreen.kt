import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.fragment.Diary
import com.example.eclair_project2.fragment.DiaryId
import com.example.eclair_project2.fragment.Solution
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolutionWriteScreen(navController: NavController, diaryId: String) {
    var solution by remember { mutableStateOf("") }
    var selectedDiaryOrSolutionTitle by remember { mutableStateOf("") }
    var selectedDiaryOrSolutionId by remember { mutableStateOf(diaryId) } // 기본 다이어리 ID 설정
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference.child("solutions")

    val diaryAndSolutionList = remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isSaving by remember { mutableStateOf(false) } // 중복 저장 방지 플래그

    // 다이어리와 솔루션 목록 가져오기
    LaunchedEffect(userId) {
        if (userId != null) {
            val items = mutableListOf<Pair<String, String>>()

            // 다이어리 가져오기
            FirebaseDatabase.getInstance().reference.child("diaries").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (diarySnapshot in snapshot.children) {
                            val title = diarySnapshot.child("title").getValue(String::class.java)
                            val key = diarySnapshot.key
                            if (title != null && key != null) {
                                items.add(Pair(title, key))
                                Log.d("Diary", "Title: $title, Key: $key")
                            }
                        }
                        diaryAndSolutionList.value = items
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SolutionWriteScreen", "Failed to load diaries: ${error.message}")
                    }
                })

            // 솔루션 가져오기
            FirebaseDatabase.getInstance().reference.child("solutions").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (solutionSnapshot in snapshot.children) {
                            val title = solutionSnapshot.child("title").getValue(String::class.java)
                            val key = solutionSnapshot.key
                            if (title != null && key != null) {
                                items.add(Pair(title, key))
                                Log.d("Solution", "Title: $title, Key: $key")
                            }
                        }
                        diaryAndSolutionList.value = items
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SolutionWriteScreen", "Failed to load solutions: ${error.message}")
                    }
                })
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

        // 일기 또는 솔루션 선택 드롭다운
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDiaryOrSolutionTitle,
                onValueChange = { },
                label = { Text("다이어리 또는 솔루션 선택") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { expanded = true },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                diaryAndSolutionList.value.forEach { (title, key) ->
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = {
                            selectedDiaryOrSolutionTitle = title
                            selectedDiaryOrSolutionId = key
                            Log.d("SolutionWriteScreen", "Selected: $title, ID: $key")
                            expanded = false
                        }
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
                Log.d("SolutionWriteScreen", "Selected ID: $selectedDiaryOrSolutionId")
                Log.d("SolutionWriteScreen", "Solution: $solution")

                if (solution.isNotEmpty() && selectedDiaryOrSolutionId.isNotEmpty() && !isSaving) {
                    isSaving = true // 저장 중 상태로 설정
                    if (userId != null) {
                        val solutionId = database.push().key // 고유 키 생성
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        val solutionData = Solution(
                            userId,
                            selectedDiaryOrSolutionId, // 정확한 다이어리 또는 솔루션 ID 사용
                            selectedDiaryOrSolutionTitle,
                            solution,
                            currentDate
                        )

                        Log.d("SolutionWriteScreen", "Generated solutionId: $solutionId")
                        Log.d("SolutionWriteScreen", "Solution to save: $solutionData")

                        if (solutionId != null) {
                            database.child(userId).child(solutionId).setValue(solutionData)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("SolutionWriteScreen", "Solution saved successfully: $solutionData")
                                        navController.navigate("emotion/${diaryAndSolutionList.value.joinToString(",") { it.second }}")
                                    } else {
                                        Log.e("SolutionWriteScreen", "Solution save failed: ${task.exception?.localizedMessage}")
                                    }
                                    isSaving = false // 저장 완료 후 상태 해제
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("SolutionWriteScreen", "Error saving solution: ${exception.localizedMessage}")
                                    isSaving = false // 실패 시 상태 해제
                                }
                        } else {
                            Log.e("SolutionWriteScreen", "Solution ID is null, cannot save solution.")
                            isSaving = false // 상태 해제
                        }
                    } else {
                        errorMessage = "로그인이 필요합니다."
                        isSaving = false // 상태 해제
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
