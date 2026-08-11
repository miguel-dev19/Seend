package com.seend.app.ui.chats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.Chat
import com.seend.app.data.model.MessageStatus
import com.seend.app.data.network.ConnectionManager
import com.seend.app.data.network.NetworkChangeMonitor
import com.seend.app.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatsUiState(
    val connectionStatus: String = "Esperando red...",
    val connectionType: String = "",
    val chats: List<Chat> = emptyList(),
    val typingUsers: Map<String, Boolean> = emptyMap(),
    val onlineUsers: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatsViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    private val networkMonitor = NetworkChangeMonitor(application)
    private val connectionManager = ConnectionManager(application)

    init {
        observeChatsFromRoom()
        monitorNetwork()
        observeWebSocket()
    }

    private fun observeChatsFromRoom() {
        viewModelScope.launch {
            chatRepository.getChatsFlow().collect { chats ->
                _uiState.update { it.copy(chats = chats) }
            }
        }
    }

    private fun monitorNetwork() {
        networkMonitor.startMonitoring()
        viewModelScope.launch {
            networkMonitor.networkChangeFlow.collect { event ->
                if (!event.isAvailable) {
                    _uiState.update { it.copy(connectionStatus = "Esperando red...", connectionType = "") }
                } else {
                    _uiState.update { it.copy(connectionStatus = "Conectando...") }
                    delay(1000)
                    _uiState.update { it.copy(connectionStatus = "Actualizando...") }
                    syncWithServer()
                    delay(500)
                    _uiState.update { it.copy(connectionStatus = "Seend", connectionType = connectionManager.getCurrentSpeedLabel()) }
                }
            }
        }
    }

    private fun syncWithServer() {
        viewModelScope.launch {
            chatRepository.syncChats()
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { ws ->
                when (ws.type) {
                    "message" -> {
                        ws.message?.let { msg ->
                            chatRepository.updateLastMessage(msg.chatId, msg.content, msg.createdAt, msg.status)
                            _uiState.update { state ->
                                val updated = state.chats.map { chat ->
                                    if (chat.id == msg.chatId) {
                                        chat.copy(
                                            lastMessage = msg.content,
                                            lastTime = msg.createdAt,
                                            lastMsgStatus = MessageStatus.valueOf(msg.status.uppercase()),
                                            unreadCount = if (msg.senderId != chat.otherUser.id) chat.unreadCount + 1 else chat.unreadCount
                                        )
                                    } else chat
                                }
                                state.copy(chats = updated)
                            }
                        }
                    }
                    "typing" -> {
                        ws.userId?.let { uid ->
                            _uiState.update { state -> state.copy(typingUsers = state.typingUsers + (uid to (ws.typing ?: false))) }
                        }
                    }
                    "user_status" -> {
                        ws.userId?.let { uid ->
                            val online = ws.online ?: false
                            _uiState.update { state ->
                                state.copy(
                                    onlineUsers = state.onlineUsers + (uid to online),
                                    chats = state.chats.map { chat ->
                                        if (chat.otherUser.id == uid) {
                                            chat.copy(otherUser = chat.otherUser.copy(isOnline = online, lastSeen = ws.lastSeen))
                                        } else chat
                                    }
                                )
                            }
                        }
                    }
                    "read_receipt" -> {
                        ws.messageId?.let {
                            _uiState.update { state ->
                                state.copy(chats = state.chats.map { chat ->
                                    if (chat.lastMsgStatus == MessageStatus.DELIVERED) chat.copy(lastMsgStatus = MessageStatus.READ)
                                    else chat
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = "Actualizando...") }
            syncWithServer()
            delay(500)
            _uiState.update { it.copy(connectionStatus = "Seend") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}
