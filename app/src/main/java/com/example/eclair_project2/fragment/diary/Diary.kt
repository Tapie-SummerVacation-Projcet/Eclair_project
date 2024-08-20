package com.example.eclair_project2.fragment.diary

data class Diary(
    var title: String = "",
    var content: String = "",
    var date: String = "",
    var solution: String? = null,
    var key: String? = null,
) {
    constructor() : this("", "", "", null, "")
}