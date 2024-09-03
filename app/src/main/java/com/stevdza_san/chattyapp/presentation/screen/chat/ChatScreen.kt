package com.stevdza_san.chattyapp.presentation.screen.chat

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parse.ParseUser
import com.stevdza_san.chattyapp.presentation.component.ErrorView
import com.stevdza_san.chattyapp.presentation.component.LoadingView
import com.stevdza_san.chattyapp.presentation.component.MessageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatScreen(onBackClick: () -> Unit) {
    val viewModel = viewModel<ChatViewModel>()
    val currentChat by viewModel.currentChat
    val messages = viewModel.messages
    val messageText by viewModel.messageInput
    val messageSeen by viewModel.seen
    val lastSeenMessage by viewModel.lastSeenMessage

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        delay(timeMillis = 1000)
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.size - 1)
        }
    }

    currentChat.DisplayResult(
        onLoading = { LoadingView() },
        onError = { ErrorView(message = it) },
        onSuccess = { chat ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = chat?.participants
                                    ?.filter { it.objectId != ParseUser.getCurrentUser().objectId }
                                    ?.map { participant -> participant.username }
                                    ?.firstOrNull() ?: "Unknown"
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "Back Arrow Icon"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                        .padding(all = 12.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.objectId }
                        ) { message ->
                            val currentUser by remember(key1 = message) {
                                derivedStateOf {
                                    ParseUser.getCurrentUser().objectId == message.owner.objectId
                                }
                            }
                            LaunchedEffect(key1 = message) {
                                if (message == messages.last() && lastSeenMessage != message) {
                                    messages.lastOrNull()?.let {
                                        viewModel.markMessageAsSeen(lastSeenMessage = it)
                                    }
                                }
                            }
                            MessageView(
                                text = message.text,
                                timestamp = message.timestamp,
                                arrangement = if (currentUser) Arrangement.End else Arrangement.Start,
                                contentColor = if (currentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                containerColor = if (currentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    2.dp
                                )
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = messageSeen && messages.isNotEmpty()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Checkmark icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Seen",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                    }
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(size = 99.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                2.dp
                            ),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                4.dp
                            ),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        value = messageText,
                        onValueChange = { viewModel.setMessageInput(it) },
                        placeholder = { Text(text = "Type here") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    viewModel.saveMessage(
                                        onSuccess = {
                                            scope.launch(Dispatchers.Main) {
                                                if (messages.isNotEmpty()) {
                                                    listState.animateScrollToItem(index = messages.size - 1)
                                                }
                                            }
                                        },
                                        onError = {
                                            println(it)
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send icon"
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}