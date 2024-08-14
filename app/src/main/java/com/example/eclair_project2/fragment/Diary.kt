package com.example.eclair_project2.fragment

data class Diary(
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    var key: String? = null // key 속성 추가
) {
    // 기본 생성자
    constructor() : this("", "", "", "")
}