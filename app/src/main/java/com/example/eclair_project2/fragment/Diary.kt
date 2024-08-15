package com.example.eclair_project2.fragment

data class Diary(
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    var key: String? = null // Firebase에서 사용될 고유 식별자
) {
    // 기본 생성자
    constructor() : this("", "", "", "")
}