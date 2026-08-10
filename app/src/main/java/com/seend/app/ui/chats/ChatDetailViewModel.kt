package com.seend.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.local.OfflineMessage
import com.seend.app.data.local.OfflineQueueDao
import com.seend.app.data.model.Message
import com.seend.app.data.model.MessageStatus
import com.seend.app.data.model.User
import com.seend.app.data.model.WsReceiveMessage
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.di.AppModule
import com.seend.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val pendingCount: Int = 0
)

class ChatDetailViewModel(
    private val chatId: String,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private val currentUserId = TokenManager.getUserId()
    private val offlineQueue: OfflineQueueDao = AppModule.provideDatabase().offlineQueueDao()

    init {
        loadMessages()
        observeWebSocket()
        sendPendingOffline()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            chatRepository.getMessages(chatId).fold(
                onSuccess = { messages -> _uiState.update { it.copy(messages = messages, isLoading = false) } },
                onFailure = { _uiState.update { it.copy(isLoading = false) } }
            )
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId).fold(
                onSuccess = { user -> _uiState.update { it.copy(otherUser = user) } },
                onFailure = {}
            )
        }
    }

    fun sendMessage(content: String, receiverId: String) {
        val tempMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = currentUserId ?: "",
            content = content,
            status = MessageStatus.SENDING,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        )
        _uiState.update { it.copy(messages = it.messages + tempMessage) }
        
        // Guardar en cola offline por si falla
        viewModelScope.launch {
            offlineQueue.insert(
                OfflineMessage(chatId = chatId, receiverId = receiverId, content = content)
            )
        }
        
        webSocketManager.sendMessage(chatId, content, receiverId)
    }

    fun sendTyping(isTyping: Boolean) {
        webSocketManager.sendTyping(chatId, isTyping)
    }

    fun sendReadReceipt(messageId: String) {
        webSocketManager.sendReadReceipt(chatId, messageId)
    }

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
                                        // Estado progresivo: solo actualiza si es mayor
                                        if (newStatus.level > current.status.level) {
                                            val updated = state.messages.toMutableList()
                                            updated[existing] = current.copy(status = newStatus)
                                            state.copy(messages = updated)
                                        } else state
                                    } else {
                                        state.copy(messages = state.messages + Message(
                                            msg.id, msg.chatId, msg.senderId, msg.content, newStatus, msg.createdAt
                                        ))
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
                                    if (it.id == mid && MessageStatus.READ.level > it.status.level)
                                        it.copy(status = MessageStatus.READ)
                                    else it
                                })
                            }
                        }
                    }
                    "user_status" -> {
                        wsMessage.userId?.let { uid ->
                            _uiState.update { state ->
                                state.copy(otherUser = state.otherUser?.copy(
                                    isOnline = wsMessage.online ?: false, lastSeen = wsMessage.lastSeen
                                ))
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun sendPendingOffline() {
        viewModelScope.launch {
            val pending = offlineQueue.getPending()
            _uiState.update { it.copy(pendingCount = pending.size) }
        }
    }
}
