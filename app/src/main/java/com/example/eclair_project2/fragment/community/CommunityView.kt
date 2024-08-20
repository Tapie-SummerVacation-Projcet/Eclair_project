package com.example.eclair_project2.fragment.community

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

@Composable
fun CommunityViewScreen(navController: NavController, contentJson: String?) {
    val auth = FirebaseAuth.getInstance()
    val gson = Gson()
    var content by remember {
        mutableStateOf(
            try {
                gson.fromJson(contentJson, SharedContent::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("CommunityViewScreen", "Failed to parse SharedContent JSON", e)
                SharedContent() // 기본값 설정
            }
        )
    }

    val database = FirebaseDatabase.getInstance().reference.child("sharedContent")

    LaunchedEffect(content) {
        Log.d("CommunityViewScreen", "Content Loaded: Title - ${content.title}, Description - ${content.description}, Date - ${content.date}")
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            if (content == null) {
                Text(text = "Loading...", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
                Text(text = "Fetching content details...", fontSize = 18.sp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = content.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            // Handle like functionality
                            val updatedLikes = content.likes + 1
                            content.key?.let { key ->
                                database.child(key).child("likes").setValue(updatedLikes)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.Red
                        )
                    }
                    Text(text = "${content.likes}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = content.description, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text(
                        text = "Posted by ${content.userName} on ${content.date}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}