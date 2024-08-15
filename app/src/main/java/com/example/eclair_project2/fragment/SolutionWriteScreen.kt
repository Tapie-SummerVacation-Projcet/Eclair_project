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
    var selectedDiaryTitle by remember { mutableStateOf("") }
    var selectedDiaryId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("solutions")

    val diaryList = remember { mutableStateOf(listOf<Diary>()) }
    val userId = auth.currentUser?.uid

    // 일기 목록 가져오기
    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseDatabase.getInstance().reference.child("diaries").child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val diaries = mutableListOf<Diary>()
                        for (diarySnapshot in snapshot.children) {
                            val diary = diarySnapshot.getValue(Diary::class.java)
                            diary?.key = diarySnapshot.key // Firebase의 key를 Diary 객체에 저장
                            if (diary != null) {
                                diaries.add(diary)
                            }
                        }
                        diaryList.value = diaries
                    }

                    override fun onCancelled(error: DatabaseError) {
                        errorMessage = "일기 목록을 불러오는 데 실패했습니다: ${error.message}"
                    }
                })
        } else {
            errorMessage = "로그인이 필요합니다."
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

        // 일기 선택 드롭다운
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDiaryTitle,
                onValueChange = { },
                label = { Text("일기 선택") },
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
                diaryList.value.forEach { diary ->
                    DropdownMenuItem(
                        text = { Text(diary.title) },
                        onClick = {
                            selectedDiaryTitle = diary.title
                            selectedDiaryId = diary.key ?: ""
                            Log.d(
                                "SolutionWriteScreen",
                                "Diary selected: ${diary.title}, ID: $selectedDiaryId"
                            )
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
                Log.d("SolutionWriteScreen", "selectedDiaryId: $selectedDiaryId")
                Log.d("SolutionWriteScreen", "solution: $solution")

                if (solution.isNotEmpty() && selectedDiaryId.isNotEmpty()) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val solutionId = database.push().key // 고유 키 생성
                        val currentDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        val solutionData = Solution(
                            userId,
                            selectedDiaryId,
                            selectedDiaryTitle,
                            solution,
                            currentDate
                        )

                        Log.d("SolutionWriteScreen", "Saving solution: $solutionData")

                        if (solutionId != null) {
                            database.child(userId).child(solutionId).setValue(solutionData)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(
                                            "SolutionWriteScreen",
                                            "Solution saved successfully: $solutionData"
                                        )
                                        navController.popBackStack() // Go back to the previous screen
                                    } else {
                                        errorMessage =
                                            "해결 방법 저장 실패: ${task.exception?.localizedMessage}"
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    errorMessage = "해결 방법 저장 중 오류 발생: ${exception.localizedMessage}"
                                }
                        }
                    } else {
                        errorMessage = "로그인이 필요합니다."
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
