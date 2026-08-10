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
        monitorNetwork()
        loadChats()
        observeWebSocket()
    }

    private fun monitorNetwork() {
        networkMonitor.startMonitoring()
        viewModelScope.launch {
            networkMonitor.networkChangeFlow.collect { event ->
                when {
                    !event.isAvailable -> _uiState.update { it.copy(connectionStatus = "Esperando red...", connectionType = "") }
                    else -> {
                        _uiState.update { it.copy(connectionStatus = "Conectando...") }
                        delay(1000)
                        _uiState.update { it.copy(connectionStatus = "Actualizando...") }
                        loadChats()
                        delay(500)
                        _uiState.update { it.copy(connectionStatus = "Seend", connectionType = connectionManager.getCurrentSpeedLabel()) }
                    }
                }
            }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            chatRepository.getChats().fold(
                onSuccess = { chats -> _uiState.update { it.copy(chats = chats, isLoading = false, error = null) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { ws ->
                when (ws.type) {
                    "message" -> {
                        ws.message?.let { msg ->
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
                }
            }
        }
    }

    fun refreshChats() {
        loadChats()
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}
