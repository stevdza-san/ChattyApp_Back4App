package com.stevdza_san.chattyapp.util

import com.parse.ParseObject
import com.parse.ParseUser
import com.stevdza_san.chattyapp.domain.Message

fun mapMessages(chatRoom: ParseObject): List<Message> {
    val messageObjects = chatRoom.getList<ParseObject>(
        ChatRoomTable.MESSAGES
    ) ?: emptyList()
    return messageObjects.map { parseObject ->
        Message(
            objectId = parseObject.objectId,
            chatRoom = chatRoom,
            owner = parseObject.getParseUser(MessageTable.OWNER)
                ?: ParseUser.getCurrentUser(),
            text = parseObject.getString(MessageTable.TEXT) ?: "",
            timestamp = parseObject.getLong(MessageTable.TIMESTAMP)
        )
    }
}