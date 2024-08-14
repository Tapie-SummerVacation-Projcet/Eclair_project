package com.example.eclair_project2.components.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project.ui.theme.Pink40
import com.example.eclair_project.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "회원 가입",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = W600,
                fontSize = 18.sp
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            placeholder = { Text("example@gmail.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("별명") },
            placeholder = { Text("ex.) 홍길동") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            placeholder = { Text("비밀번호를 입력해주세요.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordCheck,
            onValueChange = { passwordCheck = it },
            label = { Text("비밀번호 확인") },
            placeholder = { Text("비밀번호를 다시 입력해주세요.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = ""
                if (email.isNotEmpty() && nickname.isNotEmpty() && password.isNotEmpty() && password == passwordCheck) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("home")
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    val user = User(email, nickname)
                                    database.child(userId).setValue(user)
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
                                }
                            } else {
                                when (task.exception) {
                                    is FirebaseAuthUserCollisionException -> {
                                        errorMessage = "이미 존재하는 이메일입니다."
                                        Log.e("SignUp", "이메일 중복 오류: ${task.exception?.localizedMessage}")
                                    }
                                    else -> {
                                        errorMessage = "회원가입 실패: ${task.exception?.localizedMessage}"
                                        Log.e("SignUp", "회원가입 실패", task.exception)
                                    }
                                }
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

        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text(text = "이미 계정이 있으신가요? 로그인", color = Pink40)
        }
    }
}

data class User(val email: String, val nickname: String)