package com.seend.app.ui.chats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.Chat
import com.seend.app.data.model.WsReceiveMessage
import com.seend.app.data.network.ConnectionEvent
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

        // Escuchar cambios de red
        viewModelScope.launch {
            networkMonitor.networkChangeFlow.collect { event ->
                when {
                    !event.isAvailable -> {
                        _uiState.update { it.copy(
                            connectionStatus = "Esperando red...",
                            connectionType = ""
                        )}
                    }
                    event.event == ConnectionEvent.CONNECTED || 
                    event.event == ConnectionEvent.RECONNECTED -> {
                        _uiState.update { it.copy(connectionStatus = "Conectando...") }
                        delay(1000)
                        
                        // Obtener tipo de conexión
                        val speedLabel = connectionManager.getCurrentSpeedLabel()
                        val typeLabel = when (event.type) {
                            com.seend.app.data.network.NetworkType.WIFI -> "WiFi"
                            com.seend.app.data.network.NetworkType.MOBILE -> speedLabel
                            else -> ""
                        }
                        
                        _uiState.update { it.copy(connectionStatus = "Actualizando...") }
                        loadChats()
                        delay(500)
                        
                        _uiState.update { it.copy(
                            connectionStatus = "Seend",
                            connectionType = typeLabel
                        )}
                    }
                }
            }
        }

        // Escuchar cambios de velocidad
        viewModelScope.launch {
            connectionManager.status.collect { status ->
                _uiState.update { state ->
                    if (state.connectionStatus == "Seend") {
                        state.copy(connectionType = status.download.label)
                    } else state
                }
            }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            chatRepository.getChats().fold(
                onSuccess = { chats ->
                    _uiState.update {
                        it.copy(chats = chats, isLoading = false, error = null)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
            )
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { wsMessage ->
                when (wsMessage.type) {
                    "message" -> loadChats()
                    "typing" -> {
                        wsMessage.userId?.let { userId ->
                            _uiState.update { state ->
                                state.copy(
                                    typingUsers = state.typingUsers + (userId to (wsMessage.typing ?: false))
                                )
                            }
                        }
                    }
                    "user_status" -> loadChats()
                }
            }
        }
    }

    fun refreshChats() {
        _uiState.update { it.copy(connectionStatus = "Actualizando...") }
        loadChats()
        viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(connectionStatus = "Seend") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}
