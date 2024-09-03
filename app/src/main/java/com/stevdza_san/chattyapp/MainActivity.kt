package com.stevdza_san.chattyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.parse.ParseUser
import com.stevdza_san.chattyapp.navigation.Screen
import com.stevdza_san.chattyapp.navigation.SetupNavGraph
import com.stevdza_san.chattyapp.ui.theme.ChattyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChattyAppTheme {
                SetupNavGraph(
                    navController = rememberNavController(),
                    startDestination = if (ParseUser.getCurrentUser() != null) Screen.Home
                    else Screen.Auth
                )
            }
        }
    }
}