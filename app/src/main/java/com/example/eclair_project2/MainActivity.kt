package com.example.eclair_project2


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.eclair_project2.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 이 블록 내에서 @Composable 함수들을 호출할 수 있습니다.
            //clearFirebaseDatabase()
            Navigation() // 네비게이션 시작

        }
    }

    private fun printSHA1() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val sha1 = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("SHA-1", "SHA-1: $sha1")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}

fun clearFirebaseDatabase() {
    val database = FirebaseDatabase.getInstance().reference

    database.removeValue().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Firebase", "데이터베이스가 성공적으로 초기화되었습니다.")
        } else {
            Log.e("Firebase", "데이터베이스 초기화 실패: ${task.exception?.localizedMessage}")
        }
    }
}
