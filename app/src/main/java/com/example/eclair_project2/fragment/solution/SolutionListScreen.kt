package com.example.eclair_project2.fragment.solution

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.eclair_project2.components.icon.BlackArrow
import com.example.eclair_project2.components.icon.Plus
import com.example.eclair_project2.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson


data class Solution(
    val userId: String = "",
    val diaryId: String = "",
    val diaryTitle: String = "",
    val solution: String = "",
    val date: String = "",
    var key: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolutionListScreen(navController: NavController, diaryIdParams: String) {
    val diaryIds = diaryIdParams.split(",")
    val solutionListState = remember { mutableStateListOf<Solution>() }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var searchQuery by remember { mutableStateOf("") }
    var isRecentSort by remember { mutableStateOf(true) }
    var showSortOptions by remember { mutableStateOf(false) }
    var filteredSolutionList by remember { mutableStateOf(listOf<Solution>()) }

    LaunchedEffect(diaryIds, searchQuery, isRecentSort) {
        if (userId != null) {
            FirebaseDatabase.getInstance().reference
                .child("solutions")
                .child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        solutionListState.clear()
                        for (diaryId in diaryIds) {
                            snapshot.children.forEach { solutionSnapshot ->
                                val solution = solutionSnapshot.getValue(Solution::class.java)
                                if (solution != null && solution.diaryId == diaryId) {
                                    solution.key = solutionSnapshot.key
                                    solutionListState.add(solution)
                                }
                            }
                        }

                        val sortedList = if (isRecentSort) {
                            solutionListState.sortedByDescending { it.date }
                        } else {
                            solutionListState.sortedBy { it.date }
                        }

                        filteredSolutionList = if (searchQuery.isEmpty()) {
                            sortedList
                        } else {
                            sortedList.filter {
                                it.diaryTitle.contains(searchQuery, ignoreCase = true) ||
                                        it.solution.contains(searchQuery, ignoreCase = true)
                            }
                        }
                        Log.d("SolutionListScreen", "Loaded and filtered solutions: ${filteredSolutionList.size}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SolutionListScreen", "Failed to fetch solutions: ${error.message}")
                    }
                })
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SolutionWrite.route) {
                }},
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
                    placeholder = { Text(text = "솔루션 검색") },
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

                if (filteredSolutionList.isEmpty()) {
                    Text(text = "해결 방법이 없습니다.", fontSize = 18.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(filteredSolutionList) { solution ->
                            SolutionItem(
                                solution = solution,
                                onDelete = {
                                    solution.key?.let { key ->
                                        userId?.let { uid ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("solutions")
                                                .child(uid)
                                                .child(key)
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    solutionListState.remove(solution)
                                                    Log.d("SolutionListScreen", "Solution deleted successfully")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("SolutionListScreen", "Failed to delete solution: ${e.message}")
                                                }
                                        }
                                    }
                                },
                                navController = navController // Pass the navController here
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun SolutionItem(
    solution: Solution,
    onDelete: () -> Unit,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    val gson = Gson()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "해결 방법 삭제") },
            text = { Text("정말로 이 해결 방법을 삭제하시겠습니까?") },
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
                    text = solution.diaryTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = solution.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = solution.solution,
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
                    Log.d("SolutionItem", "화살 클릭")
                    val solutionJson = gson.toJson(solution)
                    navController.navigate("${Screen.SolutionEdit.route}/$solutionJson") {

                    }
                }
        )
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}