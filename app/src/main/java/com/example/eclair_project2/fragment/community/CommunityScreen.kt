package com.example.eclair_project2.fragment.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.components.icon.BlackArrow
import com.example.eclair_project2.components.icon.Plus
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val sharedContentList = remember { mutableStateOf(listOf<SharedContent>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isPopularSort by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("sharedContent")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SharedContent>()
                for (contentSnapshot in snapshot.children) {
                    val content = contentSnapshot.getValue(SharedContent::class.java)
                    if (content != null) {
                        content.key = contentSnapshot.key // Store key for like functionality
                        list.add(content)
                    }
                }
                sharedContentList.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.ShareDiaryOrSolution.route) {
                    }
                },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Plus(modifier = Modifier.size(60.dp))
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("게시물 검색") },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clickable { isPopularSort = !isPopularSort },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isPopularSort) "인기순" else "최신순", fontSize = 16.sp)
                }

                val filteredContentList = sharedContentList.value.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                }.sortedWith(
                    if (isPopularSort) compareByDescending { it.likes }
                    else compareByDescending { it.date }
                )

                if (filteredContentList.isEmpty()) {
                    Text("아직 공유된 콘텐츠가 없습니다.")
                } else {
                    LazyColumn {
                        items(filteredContentList) { content ->
                            SharedContentItem(
                                content = content,
                                onLike = { likeContent(content) },
                                navController = navController // Pass navController to handle navigation
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SharedContentItem(content: SharedContent, onLike: () -> Unit, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, // Ensure vertical alignment of items
        horizontalArrangement = Arrangement.SpaceBetween // Space out items horizontally
    ) {
        // Left side: Title and Description
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = content.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = content.description, fontSize = 16.sp)
        }

        // Right side: Date, User Name, Likes, and Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End // Align all items to the end of the row
        ) {
            Column(
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = content.date,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "by ${content.userName}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onLike() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${content.likes}", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            BlackArrow(
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        val contentJson = Gson().toJson(content)
                        navController.navigate("${Screen.CommunityView.route}/$contentJson") {

                        }
                    }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
fun likeContent(content: SharedContent) {
    content.key?.let { key ->
        val database = FirebaseDatabase.getInstance().reference.child("sharedContent").child(key)
        val updatedLikes = content.likes + 1
        database.child("likes").setValue(updatedLikes)
    }
}