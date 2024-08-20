package com.example.eclair_project2.components.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.ui.theme.Pink40
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var termsAgreed by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().getReference("users")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "회원 가입", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "이메일",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("example@gmail.com") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(fontSize = 14.sp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )

        Text(
            text = "별명",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )
        TextField(
            value = nickname,
            onValueChange = { nickname = it },
            placeholder = { Text("ex.) 홍길동") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(fontSize = 14.sp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )

        Text(
            text = "비밀번호",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("비밀번호를 입력해주세요.") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(fontSize = 14.sp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )

        Text(
            text = "비밀번호 확인",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )
        TextField(
            value = passwordCheck,
            onValueChange = { passwordCheck = it },
            placeholder = { Text("비밀번호를 다시 입력해주세요.") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(fontSize = 14.sp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = TextStyle(fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agreement checkbox and text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAgreed,
                onCheckedChange = { termsAgreed = it }
            )
            Text(
                text = "개인정보 수집 및 이용 약관에 동의하시겠습니까?",
                color = Color.Blue,
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = ""
                if (email.isNotEmpty() && nickname.isNotEmpty() && password.isNotEmpty() && password == passwordCheck) {
                    auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                                if (signInMethods.isEmpty()) {
                                    // 이메일이 사용되지 않은 경우 회원가입 진행
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { signUpTask ->
                                            if (signUpTask.isSuccessful) {
                                                // 사용자 프로필 업데이트 (displayName에 닉네임 설정)
                                                val user = auth.currentUser
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(nickname)
                                                    .build()

                                                user?.updateProfile(profileUpdates)
                                                    ?.addOnCompleteListener { profileTask ->
                                                        if (profileTask.isSuccessful) {
                                                            // 닉네임을 포함한 사용자 정보 저장
                                                            val userId = user.uid
                                                            val userData = User(email, nickname)
                                                            database.child(userId).setValue(userData)
                                                                .addOnCompleteListener { dbTask ->
                                                                    if (dbTask.isSuccessful) {
                                                                        navController.navigate(Screen.Home.route) {
                                                                            popUpTo(Screen.SignUp.route) { inclusive = true }
                                                                        }
                                                                    } else {
                                                                        errorMessage = "데이터베이스 저장 실패: ${dbTask.exception?.localizedMessage}"
                                                                        Log.e("SignUp", "데이터베이스 저장 실패", dbTask.exception)
                                                                    }
                                                                }
                                                        } else {
                                                            errorMessage = "프로필 업데이트 실패: ${profileTask.exception?.localizedMessage}"
                                                            Log.e("SignUp", "프로필 업데이트 실패", profileTask.exception)
                                                        }
                                                    }
                                            } else {
                                                errorMessage = "회원가입 실패: ${signUpTask.exception?.localizedMessage}"
                                                Log.e("SignUp", "회원가입 실패", signUpTask.exception)
                                            }
                                        }
                                } else {
                                    // 이미 사용 중인 이메일인 경우
                                    errorMessage = "이미 사용 중인 이메일입니다."
                                }
                            } else {
                                errorMessage = "이메일 확인 중 오류 발생: ${task.exception?.localizedMessage}"
                                Log.e("SignUp", "이메일 확인 오류", task.exception)
                            }
                        }
                } else {
                    if (password != passwordCheck) {
                        errorMessage = "비밀번호가 일치하지 않습니다."
                    } else {
                        errorMessage = "모든 필드를 올바르게 입력해주세요."
                    }
                    Log.e("SignUp", "입력 검증 실패: $errorMessage")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "회원 가입", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                Text(text = "이미 계정이 있으신가요? 로그인", color = Pink40)
            }
        }
    }
}

data class User(val email: String, val nickname: String)