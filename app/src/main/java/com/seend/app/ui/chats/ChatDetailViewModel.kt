package com.seend.app.ui.chats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.local.OfflineMessage
import com.seend.app.data.local.OfflineQueueDao
import com.seend.app.data.model.Message
import com.seend.app.data.model.MessageStatus
import com.seend.app.data.model.User
import com.seend.app.data.network.NetworkChangeMonitor
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.di.AppModule
import com.seend.app.util.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val pendingCount: Int = 0,
    val connectionStatus: String = "Conectado"
)

class ChatDetailViewModel(
    application: Application,
    private val chatId: String,
    private val username: String,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private val currentUserId = TokenManager.getUserId()
    private val offlineQueue: OfflineQueueDao = AppModule.provideDatabase().offlineQueueDao()
    private val networkMonitor = NetworkChangeMonitor(application)

    init {
        observeMessagesFromRoom()
        observeOtherUserFromRoom()
        syncMessages()
        syncOtherUserProfile()
        observeWebSocket()
        observePendingCount()
        monitorNetworkAndResend()
    }

    private fun observeMessagesFromRoom() {
        viewModelScope.launch {
            chatRepository.getMessagesFlow(chatId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun observeOtherUserFromRoom() {
        viewModelScope.launch {
            userRepository.getUsersFlow().collect { users ->
                val found = users.find { it.username == username }
                if (found != null) {
                    _uiState.update { it.copy(otherUser = found) }
                }
            }
        }
    }

    private fun syncMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            chatRepository.syncMessages(chatId)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun syncOtherUserProfile() {
        viewModelScope.launch {
            _uiState.value.otherUser?.let { user ->
                userRepository.getUserProfile(user.id).fold(
                    onSuccess = { updated -> _uiState.update { it.copy(otherUser = updated) } },
                    onFailure = {}
                )
            }
        }
    }

    private fun observePendingCount() {
        viewModelScope.launch {
            offlineQueue.getPendingFlow().collect { pending ->
                _uiState.update { it.copy(pendingCount = pending.size) }
            }
        }
    }

    // Monitorear red + reenviar pendientes al conectar
    private fun monitorNetworkAndResend() {
        networkMonitor.startMonitoring()
        viewModelScope.launch {
            networkMonitor.networkChangeFlow.collect { event ->
                if (!event.isAvailable) {
                    _uiState.update { it.copy(connectionStatus = "Esperando red...") }
                } else {
                    _uiState.update { it.copy(connectionStatus = "Conectado") }
                    // ¡REENVIAR PENDIENTES AL RECUPERAR CONEXIÓN!
                    resendPendingMessages()
                }
            }
        }
    }

    // Reenviar mensajes pendientes de la cola offline
    private fun resendPendingMessages() {
        viewModelScope.launch {
            val pending = offlineQueue.getPending()
            pending.forEach { offlineMsg ->
                if (offlineMsg.chatId == chatId || offlineMsg.receiverId == _uiState.value.otherUser?.id) {
                    webSocketManager.sendMessage(offlineMsg.chatId, offlineMsg.content, offlineMsg.receiverId)
                    offlineQueue.markAsSent(offlineMsg.id)
                }
            }
            // Limpiar enviados
            offlineQueue.clearSent()
        }
    }

    fun sendMessage(content: String) {
        val receiverId = _uiState.value.otherUser?.id ?: ""
        val tempMessage = Message(
            id = UUID.randomUUID().toString(), chatId = chatId,
            senderId = currentUserId ?: "", content = content,
            status = MessageStatus.SENDING,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        )
        _uiState.update { it.copy(messages = it.messages + tempMessage) }
        
        // LOCAL PRIMERO: Guardar en cola offline
        viewModelScope.launch {
            offlineQueue.insert(OfflineMessage(chatId = chatId, receiverId = receiverId, content = content))
        }
        
        // SYNC DESPUÉS: Enviar por WebSocket
        if (receiverId.isNotEmpty()) {
            webSocketManager.sendMessage(chatId, content, receiverId)
        }
    }

    fun sendTyping(isTyping: Boolean) { webSocketManager.sendTyping(chatId, isTyping) }
    fun sendReadReceipt(messageId: String) { webSocketManager.sendReadReceipt(chatId, messageId) }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { wsMessage ->
                when (wsMessage.type) {
                    "message" -> {
                        wsMessage.message?.let { msg ->
                            if (msg.chatId == chatId) {
                                val newStatus = MessageStatus.valueOf(msg.status.uppercase())
                                _uiState.update { state ->
                                    val existing = state.messages.indexOfFirst { it.id == msg.id }
                                    if (existing >= 0) {
                                        val current = state.messages[existing]
                                        if (newStatus.level > current.status.level) {
                                            val updated = state.messages.toMutableList()
                                            updated[existing] = current.copy(status = newStatus)
                                            state.copy(messages = updated)
                                        } else state
                                    } else {
                                        state.copy(messages = state.messages + Message(msg.id, msg.chatId, msg.senderId, msg.content, newStatus, msg.createdAt))
                                    }
                                }
                                if (msg.senderId != currentUserId) sendReadReceipt(msg.id)
                            }
                        }
                    }
                    "typing" -> {
                        if (wsMessage.chatId == chatId && wsMessage.userId != currentUserId)
                            _uiState.update { it.copy(isTyping = wsMessage.typing ?: false) }
                    }
                    "read_receipt" -> {
                        wsMessage.messageId?.let { mid ->
                            _uiState.update { state ->
                                state.copy(messages = state.messages.map {
                                    if (it.id == mid && MessageStatus.READ.level > it.status.level) it.copy(status = MessageStatus.READ) else it
                                })
                            }
                        }
                    }
                    "user_status" -> {
                        wsMessage.userId?.let { uid ->
                            _uiState.update { state ->
                                state.copy(otherUser = state.otherUser?.copy(isOnline = wsMessage.online ?: false, lastSeen = wsMessage.lastSeen))
                            }
                        }
                    }
                }
            }
        }
    }
}
