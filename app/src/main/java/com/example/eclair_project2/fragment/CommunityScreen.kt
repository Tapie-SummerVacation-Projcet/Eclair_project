package com.example.eclair_project2.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val sharedContentList = remember { mutableStateOf(listOf<SharedContent>()) }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("sharedContent")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SharedContent>()
                for (contentSnapshot in snapshot.children) {
                    val content = contentSnapshot.getValue(SharedContent::class.java)
                    if (content != null) {
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
        topBar = {
            TopAppBar(
                title = { Text("Community") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.ShareDiaryOrSolution.route)
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
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
                if (sharedContentList.value.isEmpty()) {
                    Text("아직 공유된 콘텐츠가 없습니다.")
                } else {
                    LazyColumn {
                        items(sharedContentList.value) { content ->
                            SharedContentItem(content = content)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SharedContentItem(content: SharedContent) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = content.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = "by ${content.userName}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Text(text = content.description, fontSize = 16.sp)
        Text(
            text = content.date,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}