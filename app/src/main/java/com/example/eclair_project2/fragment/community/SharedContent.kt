package com.example.eclair_project2.fragment.community


data class SharedContent(
    val title: String = "",
    val description: String = "",
    val userName: String = "",
    val date: String = "",
    var likes: Int = 0,
    var key: String? = null
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val date: String = ""
)