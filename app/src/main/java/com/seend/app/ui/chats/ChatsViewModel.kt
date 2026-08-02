package com.seend.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.Chat
import com.seend.app.data.model.WsReceiveMessage
import com.seend.app.data.repository.ChatRepository
import com.seend.app.util.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatsUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.ESPERANDO_RED,
    val chats: List<Chat> = emptyList(),
    val typingUsers: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ConnectionStatus(val label: String) {
    ESPERANDO_RED("Esperando red..."),
    CONECTANDO("Conectando..."),
    ACTUALIZANDO("Actualizando..."),
    CONECTADO("Seend")
}

class ChatsViewModel(
    private val chatRepository: ChatRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        loadChats()
        observeWebSocket()
        simulateConnection()
    }

    private fun simulateConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.ESPERANDO_RED) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONECTANDO) }
            kotlinx.coroutines.delay(1500)
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.ACTUALIZANDO) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONECTADO) }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = chatRepository.getChats()
            result.fold(
                onSuccess = { chats ->
                    _uiState.update { 
                        it.copy(
                            chats = chats,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { wsMessage ->
                when (wsMessage.type) {
                    "message" -> {
                        // Nuevo mensaje recibido, recargar chats
                        loadChats()
                    }
                    "typing" -> {
                        wsMessage.userId?.let { userId ->
                            val typing = wsMessage.typing ?: false
                            _uiState.update { state ->
                                state.copy(
                                    typingUsers = state.typingUsers + (userId to typing)
                                )
                            }
                        }
                    }
                    "user_status" -> {
                        // Actualizar estado online/offline
                        loadChats()
                    }
                }
            }
        }
    }

    fun refreshChats() {
        _uiState.update { it.copy(connectionStatus = ConnectionStatus.ACTUALIZANDO) }
        loadChats()
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONECTADO) }
        }
    }
}
