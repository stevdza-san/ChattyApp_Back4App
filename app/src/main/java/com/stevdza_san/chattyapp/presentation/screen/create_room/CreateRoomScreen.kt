package com.stevdza_san.chattyapp.presentation.screen.create_room

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stevdza_san.chattyapp.presentation.component.ErrorView
import com.stevdza_san.chattyapp.presentation.component.UserView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateRoomScreen(
    openChatScreen: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<CreateRoomViewModel>()
    val allUsers = viewModel.allUsers
    val isSearchBarActive by viewModel.isSearchBarActive
    val searchQuery by viewModel.searchQuery

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Create a Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updateSearchActive(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isSearchBarActive) {
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {
                    viewModel.onSearch(
                        onError = { error ->
                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                active = isSearchBarActive,
                onActiveChange = { viewModel.updateSearchActive(it) },
                placeholder = { Text(text = "Search here") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = {
                    if (isSearchBarActive) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) viewModel.updateSearchQuery(query = "")
                                else viewModel.updateSearchActive(active = false)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Icon"
                            )
                        }
                    }
                },
                content = {}
            )
        }

        if (allUsers.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .padding(all = 12.dp)
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = allUsers,
                    key = { it.objectId }
                ) {
                    UserView(
                        username = it.username,
                        onClick = {
                            viewModel.checkForExistingChatRoom(
                                user = it,
                                onDuplicateNotFound = { chatRoomId ->
                                    openChatScreen(chatRoomId)
                                },
                                onDuplicateFound = { chatRoomId ->
                                    Toast.makeText(
                                        context,
                                        "Chat already exists",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    openChatScreen(chatRoomId)
                                },
                                onError = { error ->
                                    Toast.makeText(
                                        context,
                                        error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    )
                }
            }
        } else {
            ErrorView(message = "Search for a user.")
        }
    }
}