package com.stevdza_san.chattyapp.presentation.screen.chat

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import com.stevdza_san.chattyapp.domain.ChatRoom
import com.stevdza_san.chattyapp.domain.Message
import com.stevdza_san.chattyapp.navigation.Screen
import com.stevdza_san.chattyapp.util.ChatRoomTable
import com.stevdza_san.chattyapp.util.MessageTable
import com.stevdza_san.chattyapp.util.RequestState
import com.stevdza_san.chattyapp.util.mapMessages

const val TAG = "ChatViewModel"

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val chatId = savedStateHandle.toRoute<Screen.Chat>().id

    private var liveQueryClient: ParseLiveQueryClient? = null
    private var subscription: SubscriptionHandling<ParseObject>? = null
    private var subscription2: SubscriptionHandling<ParseObject>? = null

    private var _currentChat: MutableState<RequestState<ChatRoom?>> =
        mutableStateOf(RequestState.Loading)
    val currentChat: State<RequestState<ChatRoom?>> = _currentChat

    private var _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private var _messageInput: MutableState<String> = mutableStateOf("")
    var messageInput: State<String> = _messageInput

    private var _lastSeenMessage: MutableState<Message?> = mutableStateOf(null)
    var lastSeenMessage: State<Message?> = _lastSeenMessage

    private var _seen: MutableState<Boolean> = mutableStateOf(false)
    var seen: State<Boolean> = _seen

    init {
        getChatRoomById(chatId) { chatRoom, error ->
            if (error == null) {
                chatRoom?.let {
                    _currentChat.value = RequestState.Success(
                        ChatRoom(
                            objectId = chatRoom.objectId ?: "0",
                            participants = chatRoom.getList(
                                ChatRoomTable.PARTICIPANTS
                            ) ?: emptyList(),
                            messages = emptyList(),
                            lastSeenMessages = chatRoom.getMap(
                                ChatRoomTable.LAST_SEEN_MESSAGES
                            ) ?: emptyMap()
                        )
                    )

                    liveQueryClient = ParseLiveQueryClient.Factory.getClient()
                    observeChatRooms(chatRoom)
                    observeMessages(chatRoom)
                    val mappedMessages = mapMessages(chatRoom)
                    _messages.addAll(mappedMessages.sortedBy { it.timestamp })
                    checkIfSeen(chatRoom)
                }
            } else {
                _currentChat.value =
                    RequestState.Error(message = "Error fetching ChatRoom: ${error.message}")
            }
        }
    }

    fun setMessageInput(input: String) {
        _messageInput.value = input
    }

    private fun getChatRoomById(
        id: String,
        onComplete: (ParseObject?, ParseException?) -> Unit
    ) {
        val query = ParseQuery.getQuery<ParseObject>(ChatRoomTable.NAME)
        query.include(ChatRoomTable.PARTICIPANTS)
        query.include(ChatRoomTable.MESSAGES)
        query.include(ChatRoomTable.LAST_SEEN_MESSAGES)
        query.getInBackground(id) { chatRoom, e -> onComplete(chatRoom, e) }
    }

    private fun observeChatRooms(chatRoom: ParseObject) {
        val query = ParseQuery.getQuery<ParseObject>(ChatRoomTable.NAME)
        query.include(ChatRoomTable.PARTICIPANTS)
        query.include(ChatRoomTable.MESSAGES)
        query.include(ChatRoomTable.LAST_SEEN_MESSAGES)
        query.whereEqualTo("objectId", chatRoom.objectId)

        subscription = liveQueryClient!!.subscribe(query)
        subscription!!.handleSubscribe {
            subscription?.let {
                it.handleEvent(SubscriptionHandling.Event.UPDATE) { _, chatRoom ->
                    if (chatRoom != null) {
                        checkIfSeen(chatRoom)
                    } else {
                        Log.d(TAG, "ChatRoom UPDATED event Error: ChatRoom is null.")
                    }
                }
            }
        } ?: Log.e(TAG, "Subscription is null.")
    }

    private fun observeMessages(chatRoom: ParseObject) {
        val messageQuery = ParseQuery.getQuery<ParseObject>(
            MessageTable.NAME
        )
        messageQuery.whereEqualTo(MessageTable.CHAT_ROOM, chatRoom)

        subscription2 = liveQueryClient!!.subscribe(messageQuery)
        subscription2!!.handleSubscribe {
            if (subscription2 != null) {
                subscription2?.apply {
                    handleEvent(SubscriptionHandling.Event.CREATE) { _, data ->
                        data?.let {
                            val newMessage = Message(
                                objectId = it.objectId ?: "0",
                                chatRoom = it.getParseObject(MessageTable.CHAT_ROOM) ?: ParseObject(
                                    ChatRoomTable.NAME
                                ),
                                owner = it.getParseUser(MessageTable.OWNER)
                                    ?: ParseUser.getCurrentUser(),
                                text = it.getString(MessageTable.TEXT) ?: "",
                                timestamp = it.getLong(MessageTable.TIMESTAMP)
                            )
                            _messages.add(newMessage)
                            checkIfSeen(chatRoom)
                        }
                    }
                }
            } else {
                Log.d(TAG, "Subscription is null.")
            }
        }
    }

    private fun checkIfSeen(chatRoom: ParseObject) {
        val otherParticipantId = chatRoom.getList<ParseUser>(
            ChatRoomTable.PARTICIPANTS
        )?.find {
            it.objectId != ParseUser.getCurrentUser().objectId
        }?.objectId
        val lastSeenMessageId = chatRoom.getMap<String>(
            ChatRoomTable.LAST_SEEN_MESSAGES
        )?.get(otherParticipantId)
        _seen.value = lastSeenMessageId == _messages.lastOrNull()
            ?.objectId
    }

    fun markMessageAsSeen(lastSeenMessage: Message) {
        getChatRoomById(chatId) { chat, chatError ->
            if (chat != null && chatError == null) {
                val lastSeenMessages =
                    chat.getMap(ChatRoomTable.LAST_SEEN_MESSAGES)
                        ?: mutableMapOf<String, String>()
                lastSeenMessages[ParseUser.getCurrentUser().objectId] =
                    lastSeenMessage.objectId
                chat.put(ChatRoomTable.LAST_SEEN_MESSAGES, lastSeenMessages)
                chat.saveInBackground { error ->
                    if (error == null) {
                        _lastSeenMessage.value = lastSeenMessage
                    } else {
                        Log.d(TAG, "updateLastSeenMessage() Error: ${error.message}")
                    }
                }
            } else {
                Log.d(TAG, "getChatRoomById() Error: ${chatError?.message}")
            }
        }
    }

    fun saveMessage(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_currentChat.value is RequestState.Success) {
            val chatRoomId = (_currentChat.value as RequestState.Success).data?.objectId
            val chatRoomQuery = ParseQuery.getQuery<ParseObject>(ChatRoomTable.NAME)

            chatRoomQuery.getInBackground(chatRoomId) { chatRoom, chatRoomError ->
                if (chatRoomError == null && chatRoom != null) {
                    val message = ParseObject(MessageTable.NAME)
                    message.put(MessageTable.TEXT, _messageInput.value)
                    message.put(MessageTable.OWNER, ParseUser.getCurrentUser())
                    message.put(MessageTable.CHAT_ROOM, chatRoom)
                    message.put(MessageTable.TIMESTAMP, System.currentTimeMillis())

                    message.saveInBackground { messageError ->
                        if (messageError == null) {
                            chatRoom.add(ChatRoomTable.MESSAGES, message)
                            chatRoom.saveInBackground { chatRoomSaveError ->
                                if (chatRoomSaveError == null) {
                                    onSuccess()
                                    _messageInput.value = ""
                                } else {
                                    onError("Error while saving a ChatRoom: ${chatRoomSaveError.message}")
                                }
                            }
                        } else {
                            onError("Error while saving a Message: ${messageError.message}")
                        }
                    }
                } else {
                    onError("Error while retrieving ChatRoom: ${chatRoomError.message}")
                }
            }
        }
    }
}