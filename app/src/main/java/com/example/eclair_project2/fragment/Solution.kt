package com.example.eclair_project2.fragment

data class Solution(
    val userId: String = "",
    val diaryId: String = "",
    val diaryTitle: String = "", // Add this to store the diary title
    val solution: String = "",
    val date: String = "",
    var key: String? = null // key 속성 추가
) {
    // 기본 생성자
    constructor() : this("", "", "", "", "")
}