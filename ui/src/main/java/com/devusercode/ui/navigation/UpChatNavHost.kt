package com.devusercode.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devusercode.ui.screens.auth.AuthScreen
import com.devusercode.ui.screens.chat.ChatScreen
import com.devusercode.ui.screens.home.HomeScreen
import com.devusercode.ui.screens.profile.ProfileScreen
import com.devusercode.ui.screens.settings.SettingsScreen

@Composable
fun UpChatNavHost(
    nav: NavHostController,
    start: String = Routes.HOME
) {
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.AUTH) {
            AuthScreen(onLoggedIn = {
                nav.navigate(Routes.HOME) { popUpTo(0) }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChat = { cid -> nav.navigate("chat/$cid") },
                onProfile = { nav.navigate(Routes.PROFILE) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onLoggedOutNavigateToAuth = { route ->
                    nav.navigate(route) { popUpTo(0) }
                }
            )
        }
        composable("chat/{cid}") { ChatScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
