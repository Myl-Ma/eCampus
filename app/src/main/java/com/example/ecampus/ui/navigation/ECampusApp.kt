package com.example.ecampus.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ecampus.ui.screens.detail.DetailScreen
import com.example.ecampus.ui.screens.favorites.FavoritesScreen
import com.example.ecampus.ui.screens.home.HomeScreen
import com.example.ecampus.ui.screens.myitems.MyItemsScreen
import com.example.ecampus.ui.screens.profile.ProfileScreen
import com.example.ecampus.ui.screens.publish.PublishScreen

@Composable
fun ECampusApp(navController: NavHostController = rememberNavController()) {
    val items = listOf(BottomDest.Home, BottomDest.Publish, BottomDest.Favorites, BottomDest.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(dest.route) == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { /* You can add icons later */ },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Home) {
                HomeScreen(onItemClick = { id -> navController.navigate("detail/$id") })
            }
            composable(Routes.Publish) {
                PublishScreen(
                    onPublishSuccess = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.Favorites) {
                FavoritesScreen(onItemClick = { id -> navController.navigate("detail/$id") })
            }
            composable(Routes.Profile) {
                ProfileScreen(onMyItemsClick = { navController.navigate(Routes.MyItems) })
            }
            composable(Routes.MyItems) {
                MyItemsScreen(onItemClick = { id -> navController.navigate("detail/$id") })
            }
            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId").orEmpty()
                DetailScreen(itemId = itemId)
            }
        }
    }
}

sealed class BottomDest(val route: String, val label: String) {
    data object Home : BottomDest(Routes.Home, "首页")
    data object Publish : BottomDest(Routes.Publish, "发布")
    data object Favorites : BottomDest(Routes.Favorites, "收藏")
    data object Profile : BottomDest(Routes.Profile, "我的")
}
