package com.example.eclair_project2.fragment

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
                    for (diarySnapshot in snapshot.children) {
                        val diary = diarySnapshot.getValue(Diary::class.java)
                        if (diary != null) {
                            diaries.add(diary)
                        }
                    }
                    _diaryList.value = diaries
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리
                }
            })
        }
    }
}