package com.stevdza_san.chattyapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.stevdza_san.chattyapp.presentation.screen.auth.AuthScreen
import com.stevdza_san.chattyapp.presentation.screen.chat.ChatScreen
import com.stevdza_san.chattyapp.presentation.screen.create_room.CreateRoomScreen
import com.stevdza_san.chattyapp.presentation.screen.home.HomeScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: Screen
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                onAuthenticated = {
                    navController.popBackStack()
                    navController.navigate(Screen.Home)
                }
            )
        }
        composable<Screen.Home> {
            HomeScreen(
                onLogout = {
                    navController.popBackStack()
                    navController.navigate(Screen.Auth)
                },
                onCreateRoom = {
                    navController.navigate(Screen.CreateRoom)
                },
                onChatRoomSelect = {
                    navController.navigate(Screen.Chat(id = it))
                }
            )
        }
        composable<Screen.CreateRoom> {
            CreateRoomScreen(
                openChatScreen = {
                    navController.popBackStack()
                    navController.navigate(Screen.Chat(it))
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.Chat> {
            ChatScreen(onBackClick = { navController.navigateUp() })
        }
    }
}