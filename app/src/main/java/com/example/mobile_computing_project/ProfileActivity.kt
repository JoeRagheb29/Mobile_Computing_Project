package com.example.mobile_computing_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.mobile_computing_project.DataBase.Daf3aDatabase
import com.example.mobile_computing_project.DataBase.SessionManager
import com.example.mobile_computing_project.ui.ProfileScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        val db = Daf3aDatabase.getInstance(this)
        val userId = sessionManager.getUserId()

        setContent {
            // هنخزن اليوزر هنا أول ما نجيبه من الداتابيز
            var user by remember { mutableStateOf<com.example.mobile_computing_project.DataBase.UserEntity?>(null) }

            // أول ما الصفحة تفتح، نروح نجيب بيانات اليوزر
            LaunchedEffect(Unit) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = db.userDao().getById(userId)
                    withContext(Dispatchers.Main) {
                        user = result
                    }
                }
            }

            // لو لسه مجبناش البيانات، نعرض شاشة تحميل
            // لو جبناها، نعرض البروفايل
            if (user != null) {
                ProfileScreen(
                    user = user!!,
                    onBackClick = {
                        finish() // ارجعي للهوم
                    }
                )
            }
        }
    }
}