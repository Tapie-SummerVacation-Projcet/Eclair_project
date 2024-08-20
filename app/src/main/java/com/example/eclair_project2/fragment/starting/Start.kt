package com.example.eclair_project2.fragment.starting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.components.icon.AccountIcon
import com.example.eclair_project2.components.icon.LoginIcon
import com.example.eclair_project2.components.typography.Pridi
import com.example.eclair_project2.ui.theme.LightPink
import com.example.eclair_project2.ui.theme.Pink40

@Composable
fun Starting(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)  // Emsta 텍스트 너비 설정
                .height(119.dp) // Emsta 텍스트 높이 설정
        ) {
            Text(
                text = "Emsta",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = Pridi,
                    fontWeight = FontWeight.Bold,
                    fontSize = 65.sp,
                    color = LightPink // 텍스트 색상은 디자인에 맞게 조정
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)

            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "당신의 감성 도우미",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = Color.Gray
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40),
            onClick = {
                navController.navigate("login")
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "로그인",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom =8.dp)
                )
               LoginIcon()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("signup")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "회원 가입",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AccountIcon()
            }
        }
    }
}
