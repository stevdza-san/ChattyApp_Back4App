package com.stevdza_san.chattyapp.domain

import com.parse.ParseObject
import com.parse.ParseUser

data class Message(
    val objectId: String,
    val chatRoom: ParseObject,
    val owner: ParseUser,
    val text: String,
    val timestamp: Long
)
