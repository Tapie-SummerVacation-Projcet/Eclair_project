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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    var originalTitle by remember { mutableStateOf<String?>(null) }
    var originalContent by remember { mutableStateOf<String?>(null) } // State to hold the original content
    val database = FirebaseDatabase.getInstance().reference

    // Fetch the original content based on the originalId
    LaunchedEffect(content.originalId) {
        if (content.originalId.isNotEmpty()) {
            val originalPath = if (content.originalId.startsWith("-")) "diaries" else "solutions" // Adjust path as needed
            val userId = auth.currentUser?.uid ?: return@LaunchedEffect
            database.child(originalPath).child(userId).child(content.originalId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        originalTitle = snapshot.child("title").getValue(String::class.java)
                        originalContent = snapshot.child("content").getValue(String::class.java)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CommunityViewScreen", "Failed to load original content: ${error.message}")
                    }
                })
        }
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
                            // Increment the like count locally
                            val updatedContent = content.copy(likes = content.likes + 1)
                            content = updatedContent // Trigger recomposition by updating the state

                            // Update the likes count in the database
                            content.key?.let { key ->
                                database.child("sharedContent").child(key).child("likes").setValue(updatedContent.likes)
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

                originalContent?.let {
                    Column {
                        Text(
                            text = "${originalTitle ?: "Loading title"}: $it",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } ?: run {
                    Text(text = "Loading original content...", fontSize = 16.sp)
                }

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
