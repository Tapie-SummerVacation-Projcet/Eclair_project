package com.example.eclair_project2.fragment.community


data class SharedContent(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",  // 사용자 이름 추가
    val diaryOrSolutionId: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = ""
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val date: String = ""
)