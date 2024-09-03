package com.stevdza_san.chattyapp.presentation.screen.create_room

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.stevdza_san.chattyapp.util.ChatRoomTable

class CreateRoomViewModel: ViewModel() {
    private val query = ParseUser.getQuery()

    private val _allUsers = mutableStateListOf<ParseUser>()
    val allUsers: List<ParseUser> = _allUsers

    private var _searchQuery: MutableState<String> = mutableStateOf("")
    var searchQuery: State<String> = _searchQuery

    private var _isSearchBarActive: MutableState<Boolean> = mutableStateOf(false)
    val isSearchBarActive: State<Boolean> = _isSearchBarActive

    fun checkForExistingChatRoom(
        user: ParseUser,
        onDuplicateFound: (String) -> Unit,
        onDuplicateNotFound: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val participants = listOf(ParseUser.getCurrentUser(), user)
        checkForDuplicateChatRooms(
            participants = participants,
            onDuplicateNotFound = {
                createChatRoom(
                    participants = participants,
                    onSuccess = onDuplicateNotFound,
                    onError = onError
                )
            },
            onDuplicateFound = onDuplicateFound,
            onError = onError
        )
    }

    private fun checkForDuplicateChatRooms(
        participants: List<ParseUser>,
        onDuplicateFound: (String) -> Unit,
        onDuplicateNotFound: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Create a query for ChatRoom that matches the given participants
        val query = ParseQuery.getQuery<ParseObject>(ChatRoomTable.NAME)
        query.whereEqualTo(
            ChatRoomTable.PARTICIPANTS_IDENTIFIER,
            participants.sortedBy { it.objectId }
                .joinToString(separator = "_") { it.objectId }
        )

        query.findInBackground { chatRooms, queryError ->
            if (queryError == null) {
                if (chatRooms.isNullOrEmpty()) onDuplicateNotFound()
                else onDuplicateFound(chatRooms.first().objectId)
            } else {
                onError("Error searching the ChatRoom: ${queryError.message}")
            }
        }
    }

    private fun createChatRoom(
        participants: List<ParseUser>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val chatRoom = ParseObject(ChatRoomTable.NAME)
        chatRoom.put(ChatRoomTable.PARTICIPANTS, participants)
        chatRoom.put(
            ChatRoomTable.PARTICIPANTS_IDENTIFIER,
            participants.sortedBy { it.objectId }
                .joinToString(separator = "_") { it.objectId }
        )

        chatRoom.saveInBackground { error ->
            if (error == null) {
                onSuccess(chatRoom.objectId)
                onError("ChatRoom created successfully.")
            } else {
                onError("Error saving the ChatRoom: ${error.message}")
            }
        }
    }

    fun onSearch(onError: (String) -> Unit) {
        query.whereContains("username", searchQuery.value)
        query.findInBackground { users, error ->
            if (error == null) {
                for (user in users) {
                    if (user.username != ParseUser.getCurrentUser().username) {
                        _allUsers.add(user)
                    }
                }
            } else {
                onError("Error fetching users: " + error.message)
            }
        }
        _isSearchBarActive.value = false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchActive(active: Boolean) {
        _isSearchBarActive.value = active
    }
}