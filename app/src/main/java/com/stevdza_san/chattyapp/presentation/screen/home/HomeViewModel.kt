package com.stevdza_san.chattyapp.presentation.screen.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import com.stevdza_san.chattyapp.domain.ChatRoom
import com.stevdza_san.chattyapp.util.ChatRoomTable
import com.stevdza_san.chattyapp.util.RequestState
import com.stevdza_san.chattyapp.util.mapMessages

typealias Rooms = RequestState<List<ChatRoom>>

class HomeViewModel : ViewModel() {
    private val liveQueryClient: ParseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    private var subscription: SubscriptionHandling<ParseObject>? = null

    private var _rooms: MutableState<Rooms> = mutableStateOf(RequestState.Loading)
    val rooms: State<Rooms> = _rooms

    init {
        observeChatRooms()
    }

    private fun observeChatRooms() {
        val query = ParseQuery.getQuery<ParseObject>(ChatRoomTable.NAME)
        query.include(ChatRoomTable.PARTICIPANTS)
        query.include(ChatRoomTable.MESSAGES)
        query.include(ChatRoomTable.LAST_SEEN_MESSAGES)
//        query.whereEqualTo(ChatRoomTable.PARTICIPANTS, ParseUser.getCurrentUser())

        subscription = liveQueryClient.subscribe(query)
        subscription?.handleEvent(SubscriptionHandling.Event.CREATE) { q, _ ->
            updateChatRooms(q)
        }

        subscription?.handleEvent(SubscriptionHandling.Event.UPDATE) { q, _ ->
            updateChatRooms(q)
        }

        subscription?.handleEvent(SubscriptionHandling.Event.DELETE) { q, _ ->
            updateChatRooms(q)
        }

        // Initial load of data
        updateChatRooms(query)
    }

    private fun updateChatRooms(query: ParseQuery<ParseObject>) {
        query.findInBackground { chatRooms, error ->
            if (error == null) {
                if (chatRooms != null && chatRooms.isNotEmpty()) {
                    val parsedChatRooms = chatRooms.map { chatRoom ->
                        val objectId = chatRoom.objectId
                        val participants = chatRoom.getList<ParseUser>(
                            ChatRoomTable.PARTICIPANTS
                        ) ?: emptyList()
                        val messages = mapMessages(chatRoom)
                        val lastSeenMessages = chatRoom.getMap<String>(
                            ChatRoomTable.LAST_SEEN_MESSAGES
                        ) ?: emptyMap()
                        ChatRoom(
                            objectId = objectId,
                            participants = participants,
                            messages = messages,
                            lastSeenMessages = lastSeenMessages
                        )
                    }
                    _rooms.value = RequestState.Success(
                        data = parsedChatRooms
                    )
                } else {
                    _rooms.value = RequestState.Error(
                        message = "Empty ChatRooms"
                    )
                }
            } else {
                _rooms.value =
                    RequestState.Error(
                        message = "Error while Fetching ChatRooms: ${error.message}"
                    )
            }
        }
    }
}