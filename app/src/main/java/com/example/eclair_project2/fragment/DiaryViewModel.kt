package com.example.eclair_project2.fragment

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.eclair_project2.fragment.diary.Diary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DiaryViewModel : ViewModel() {
    private val _diaryList = MutableStateFlow<List<Diary>>(emptyList())
    val diaryList: StateFlow<List<Diary>> get() = _diaryList

    var currentDiaryIds by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        fetchDiaries()
    }

    private fun fetchDiaries() {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference.child("diaries")
        val userId = auth.currentUser?.uid

        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val diaries = mutableListOf<Diary>()
                    val diaryIds = mutableListOf<String>()
                    for (diarySnapshot in snapshot.children) {
                        val diary = diarySnapshot.getValue(Diary::class.java)
                        diary?.key = diarySnapshot.key
                        if (diary != null) {
                            diaries.add(diary)
                            diaryIds.add(diarySnapshot.key!!)
                        }
                    }
                    _diaryList.value = diaries
                    currentDiaryIds = diaryIds // 리스트에 모든 다이어리 ID 저장
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리
                }
            })
        }
    }
}