package com.stevdza_san.chattyapp.presentation.screen.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parse.ParseUser
import com.stevdza_san.chattyapp.presentation.component.ChatView
import com.stevdza_san.chattyapp.presentation.component.ErrorView
import com.stevdza_san.chattyapp.presentation.component.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onCreateRoom: () -> Unit,
    onChatRoomSelect: (String) -> Unit,
) {
    val viewModel = viewModel<HomeViewModel>()
    val rooms by viewModel.rooms

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chatty") },
                actions = {
                    IconButton(
                        onClick = {
                            ParseUser.logOutInBackground { error ->
                                if (error == null) {
                                    onLogout()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out icon"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoom) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add icon"
                )
            }
        }
    ) { padding ->
        rooms.DisplayResult(
            onLoading = { LoadingView() },
            onError = { ErrorView(message = it) },
            onSuccess = { chatRooms ->
                if (chatRooms.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = chatRooms,
                            key = { it.objectId }
                        ) { room ->
                            val friendUsername = remember(room.participants) {
                                room.participants
                                    .filter { it.objectId != ParseUser.getCurrentUser().objectId }
                                    .map { participant -> participant.username }
                                    .firstOrNull()
                            }

                            val lastMessage by remember(room.messages) {
                                derivedStateOf {
                                    runCatching { room.messages.last().text }
                                        .getOrDefault("")
                                }
                            }
                            val unreadMessages by remember {
                                derivedStateOf {
                                    runCatching { room.lastSeenMessages[ParseUser.getCurrentUser().objectId] != room.messages.last().objectId }
                                        .getOrDefault(false)
                                }
                            }
                            if(lastMessage.isNotEmpty()) {
                                ChatView(
                                    name = friendUsername ?: "Unknown",
                                    unreadMessages = unreadMessages,
                                    lastMessage = lastMessage,
                                    onClick = { onChatRoomSelect(room.objectId) }
                                )
                            }
                        }
                    }
                } else {
                    ErrorView(message = "Empty ChatRoom.")
                }
            }
        )
    }
}