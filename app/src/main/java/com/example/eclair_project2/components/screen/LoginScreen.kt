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
import com.example.eclair_project2.ui.theme.Pink40
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "로그인",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = W600,
                fontSize = 18.sp
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            placeholder = { Text("이메일을 입력해주세요.") },
            singleLine = true,
            isError = emailError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
            )
        )
        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = Color.Red,
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            placeholder = { Text("비밀번호를 입력해주세요.") },
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                fontWeight = W600,
                fontSize = 16.sp
            )
        )
        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = Color.Red,
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                emailError = ""
                passwordError = ""

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("home") // 로그인 성공 시 홈 화면으로 이동
                            } else {
                                when (task.exception) {
                                    is FirebaseAuthInvalidUserException -> {
                                        emailError = "존재하지 않는 이메일입니다."
                                        Log.e("Login", "이메일 오류: ${task.exception?.localizedMessage}")
                                    }
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        passwordError = "잘못된 비밀번호입니다."
                                        Log.e("Login", "비밀번호 오류: ${task.exception?.localizedMessage}")
                                    }
                                    else -> {
                                        emailError = "로그인 실패: ${task.exception?.localizedMessage}"
                                        Log.e("Login", "로그인 실패", task.exception)
                                    }
                                }
                            }
                        }
                } else {
                    if (email.isEmpty()) {
                        emailError = "이메일을 입력해주세요."
                        Log.e("Login", "이메일 입력 누락")
                    }
                    if (password.isEmpty()) {
                        passwordError = "비밀번호를 입력해주세요."
                        Log.e("Login", "비밀번호 입력 누락")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "로그인", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text(text = "계정이 없으신가요? 회원가입", color = Pink40)
        }
    }
}