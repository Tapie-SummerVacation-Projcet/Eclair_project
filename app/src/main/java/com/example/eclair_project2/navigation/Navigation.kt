package com.example.eclair_project2.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eclair_project.components.navigation.BottomNavigationBar

import com.example.eclair_project.components.screen.LoginScreen
import com.example.eclair_project.components.screen.SignUpScreen
import com.example.eclair_project.fragment.CommunityScren
import com.example.eclair_project.fragment.DiaryWriteScreen
import com.example.eclair_project.fragment.EmotionScreen
import com.example.eclair_project.fragment.HomeScreen
import com.example.eclair_project.fragment.ProblemSolvingScreen
import com.example.eclair_project.fragment.Starting

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Emotion : Screen("emotion")
    object Diary : Screen("diary")
    object DiaryWrite : Screen("diary_write")
    object Community : Screen("community")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route && currentRoute != Screen.SignUp.route && currentRoute != Screen.Start.route) {
                BottomNavigationBar(navController)
            }
        },
        content = { innerPadding ->
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    fadeIn(animationSpec = tween(1000)) with fadeOut(animationSpec = tween(500))
                }
            ) { targetRoute ->
                Row(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(navController = navController, startDestination = Screen.Start.route) {
                            composable(Screen.Start.route){ Starting(navController) }
                            composable(Screen.Login.route){ LoginScreen(navController)}
                            composable(Screen.SignUp.route){ SignUpScreen(navController) }
                            composable(Screen.Home.route) { HomeScreen(navController)}
                            composable(Screen.Emotion.route) { EmotionScreen(navController)}
                            composable(Screen.Diary.route) { ProblemSolvingScreen(navController) }
                            composable(Screen.DiaryWrite.route) { DiaryWriteScreen(navController)}
                            composable(Screen.Community.route) { CommunityScren(navController) }
                        }
                    }
                }
            }
        }
    )
}