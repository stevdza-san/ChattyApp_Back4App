package com.stevdza_san.chattyapp.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Auth: Screen()
    @Serializable
    data object Home: Screen()
    @Serializable
    data object CreateRoom: Screen()
    @Serializable
    data class Chat(val id: String): Screen()
}