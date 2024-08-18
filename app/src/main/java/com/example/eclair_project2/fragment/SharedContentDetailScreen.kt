package com.example.eclair_project2.fragment

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedContentDetailScreen(navController: NavController, sharedContentId: String) {
    val sharedContent = remember { mutableStateOf<SharedContent?>(null) }
    val commentListState = remember { mutableStateListOf<Comment>() }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Firebase에서 공유된 콘텐츠와 댓글 가져오기
    LaunchedEffect(sharedContentId) {
        FirebaseDatabase.getInstance().reference
            .child("sharedContent")
            .child(sharedContentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sharedContent.value = snapshot.getValue(SharedContent::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SharedContentDetailScreen", "Failed to fetch content: ${error.message}")
                }
            })

        FirebaseDatabase.getInstance().reference
            .child("comments")
            .child(sharedContentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentListState.clear()
                    snapshot.children.forEach { commentSnapshot ->
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        if (comment != null) {
                            commentListState.add(comment)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SharedContentDetailScreen", "Failed to fetch comments: ${error.message}")
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("콘텐츠 상세") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                sharedContent.value?.let { content ->
                    Text(text = content.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = content.description, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = content.date, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "댓글", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (commentListState.isEmpty()) {
                        Text("댓글이 없습니다.", fontSize = 14.sp)
                    } else {
                        LazyColumn {
                            items(commentListState) { comment ->
                                CommentItem(comment = comment)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CommentInput(onCommentSubmit = { text ->
                        if (userId != null && text.isNotEmpty()) {
                            val commentId = FirebaseDatabase.getInstance().reference
                                .child("comments")
                                .child(sharedContentId)
                                .push().key
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                Date()
                            )

                            val comment = Comment(
                                id = commentId ?: "",
                                userId = userId,
                                text = text,
                                date = currentDate
                            )

                            if (commentId != null) {
                                FirebaseDatabase.getInstance().reference
                                    .child("comments")
                                    .child(sharedContentId)
                                    .child(commentId)
                                    .setValue(comment)
                                    .addOnSuccessListener {
                                        Log.d("SharedContentDetailScreen", "Comment added successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("SharedContentDetailScreen", "Failed to add comment: ${e.message}")
                                    }
                            }
                        }
                    })
                } ?: run {
                    Text("콘텐츠 정보를 불러오는 중입니다...", modifier = Modifier.padding(16.dp))
                }
            }
        }
    )
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = comment.text, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.date,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Divider()
    }
}

@Composable
fun CommentInput(onCommentSubmit: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = commentText,
        onValueChange = { commentText = it },
        label = { Text("댓글 작성") },
        placeholder = { Text("댓글을 입력하세요.") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            onCommentSubmit(commentText)
            commentText = ""
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("댓글 달기")
    }
}