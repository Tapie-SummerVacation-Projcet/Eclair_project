package com.example.eclair_project2.fragment.diary
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eclair_project2.components.icon.ArrowIcon
import com.example.eclair_project2.components.icon.BlackArrow
import com.example.eclair_project2.components.icon.Plus
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("diaries")
    val userId = auth.currentUser?.uid

    var diaryList by remember { mutableStateOf(listOf<Diary>()) }
    var filteredDiaryList by remember { mutableStateOf(listOf<Diary>()) }
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRecentSort by remember { mutableStateOf(true) }
    var showSortOptions by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val diaries = mutableListOf<Diary>()
                    for (diarySnapshot in snapshot.children) {
                        val diary = diarySnapshot.getValue(Diary::class.java)
                        if (diary != null) {
                            diary.key = diarySnapshot.key
                            diaries.add(diary)
                        }
                    }
                    diaryList = diaries
                    filteredDiaryList = diaries.sortedByDescending { it.date }
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = "일기 목록을 불러오는 데 실패했습니다: ${error.message}"
                }
            })
        } else {
            errorMessage = "로그인이 필요합니다."
        }
    }

    LaunchedEffect(searchQuery, isRecentSort) {
        val sortedList = if (isRecentSort) {
            diaryList.sortedByDescending { it.date }
        } else {
            diaryList.sortedBy { it.date }
        }

        filteredDiaryList = if (searchQuery.isEmpty()) {
            sortedList
        } else {
            sortedList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.DiaryWrite.route) },
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
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "일기 검색") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { showSortOptions = !showSortOptions },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isRecentSort) "최근 순" else "옛날 순", fontSize = 16.sp)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort Options",
                            modifier = Modifier.size(20.dp).padding(start = 4.dp)
                        )
                    }

                    if (showSortOptions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "최근 순",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isRecentSort = true
                                        showSortOptions = false
                                    }
                                    .padding(vertical = 8.dp)
                            )
                            Text(
                                text = "옛날 순",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isRecentSort = false
                                        showSortOptions = false
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }

                LazyColumn {
                    items(filteredDiaryList.size) { index ->
                        val diary = filteredDiaryList[index]
                        DiaryItem(diary, onDelete = {
                            diary.key?.let { key ->
                                if (userId != null) {
                                    database.child(userId).child(key).removeValue().addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            errorMessage = "일기 삭제 실패: ${task.exception?.localizedMessage}"
                                        }
                                    }
                                }
                            }
                        }, navController = navController)
                    }
                }
            }
        }
    )
}


@Composable
fun DiaryItem(diary: Diary, onDelete: () -> Unit, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    val gson = Gson()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "일기 삭제") },
            text = { Text("정말로 이 일기를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDialog = false
                }) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { showDialog = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = diary.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = diary.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = diary.content,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        BlackArrow(
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    Log.d("DiaryItem", "화살 클릭")
                    val diaryJson = gson.toJson(diary)
                    navController.navigate("${Screen.DiaryScreen.route}/$diaryJson")
                }
        )
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}