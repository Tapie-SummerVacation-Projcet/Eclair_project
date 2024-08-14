package com.example.eclair_project2.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eclair_project.components.icon.*

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: @Composable (Modifier) -> Unit,
    val selectedIcon: @Composable (Modifier) -> Unit,
)

@Composable
fun BottomNavigationBar(navController: NavController) {
//    val configuration = LocalConfiguration.current
//    val itemWidth = configuration.screenWidthDp / 4

    val itemWidth = 100


    val items = listOf(
        BottomNavItem("Home", "home", { HomeIcon(Modifier.width(itemWidth.dp).height(70.dp)) }, { HomeChosenIcon(Modifier.width(itemWidth.dp).height(70.dp)) }),
        BottomNavItem("Emotion", "emotion", { PenIcon(Modifier.width(itemWidth.dp).height(70.dp)) }, { PenChosenIcon(Modifier.width(itemWidth.dp).height(70.dp)) }),
        BottomNavItem("Diary", "diary", { BookIcon(Modifier.width(itemWidth.dp).height(70.dp)) }, { BookChosenIcon(Modifier.width(itemWidth.dp).height(70.dp)) }),
        BottomNavItem("Community", "community", { CommunityIcon(Modifier.width(itemWidth.dp).height(70.dp)) }, { CommunityChosenIcon(Modifier.width(itemWidth.dp).height(70.dp)) })
    )
    Surface(
        color = Color.White,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null
                        ) {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentRoute == item.route) {
                        item.selectedIcon(Modifier)
                    } else {
                        item.icon(Modifier)
                    }
                }
            }
        }
    }
}