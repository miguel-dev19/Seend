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
        observeChatsFromRoom()  // Flow desde BD local
        monitorNetwork()
        observeWebSocket()
    }

    // Suscribirse a Room: UI se actualiza automáticamente
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
                    syncWithServer()  // Sincronizar en background
                    delay(500)
                    _uiState.update { it.copy(connectionStatus = "Seend", connectionType = connectionManager.getCurrentSpeedLabel()) }
                }
            }
        }
    }

    // Sincronizar con servidor sin bloquear UI
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
                            // Actualizar último mensaje en Room
                            chatRepository.updateLastMessage(
                                msg.chatId, msg.content, msg.createdAt, msg.status
                            )
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
