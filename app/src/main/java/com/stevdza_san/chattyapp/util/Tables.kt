package com.stevdza_san.chattyapp.util

object ChatRoomTable {
    const val NAME = "ChatRoom"
    const val MESSAGES = "messages"
    const val PARTICIPANTS = "participants"
    const val PARTICIPANTS_IDENTIFIER = "participantsIdentifier"
    const val LAST_SEEN_MESSAGES = "lastSeenMessages"
}

object MessageTable {
    const val NAME = "Message"
    const val OWNER = "owner"
    const val TEXT = "text"
    const val CHAT_ROOM = "chatRoom"
    const val TIMESTAMP = "timestamp"
}