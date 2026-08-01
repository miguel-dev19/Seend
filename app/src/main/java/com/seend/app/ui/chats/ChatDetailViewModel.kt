package com.seend.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.Message
import com.seend.app.data.model.User
import com.seend.app.data.model.WsReceiveMessage
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val connectionStatus: String = "Conectado"
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

    init {
        loadMessages()
        observeWebSocket()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            chatRepository.getMessages(chatId).fold(
                onSuccess = { messages ->
                    _uiState.update {
                        it.copy(messages = messages, isLoading = false)
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

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(otherUser = user) }
                },
                onFailure = {}
            )
        }
    }

    fun sendMessage(content: String, receiverId: String) {
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
                                // Agregar o actualizar mensaje
                                _uiState.update { state ->
                                    val existingIndex = state.messages.indexOfFirst { it.id == msg.id }
                                    if (existingIndex >= 0) {
                                        val updatedMessages = state.messages.toMutableList()
                                        updatedMessages[existingIndex] = Message(
                                            id = msg.id,
                                            chatId = msg.chatId,
                                            senderId = msg.senderId,
                                            content = msg.content,
                                            status = com.seend.app.data.model.MessageStatus.valueOf(
                                                msg.status.uppercase()
                                            ),
                                            createdAt = msg.createdAt
                                        )
                                        state.copy(messages = updatedMessages)
                                    } else {
                                        state.copy(
                                            messages = state.messages + Message(
                                                id = msg.id,
                                                chatId = msg.chatId,
                                                senderId = msg.senderId,
                                                content = msg.content,
                                                status = com.seend.app.data.model.MessageStatus.valueOf(
                                                    msg.status.uppercase()
                                                ),
                                                createdAt = msg.createdAt
                                            )
                                        )
                                    }
                                }
                                
                                // Enviar confirmación de lectura para mensajes recibidos
                                if (msg.senderId != currentUserId) {
                                    sendReadReceipt(msg.id)
                                }
                            }
                        }
                    }
                    "typing" -> {
                        if (wsMessage.chatId == chatId && wsMessage.userId != currentUserId) {
                            _uiState.update { it.copy(isTyping = wsMessage.typing ?: false) }
                        }
                    }
                    "read_receipt" -> {
                        // Actualizar estado a leído
                        wsMessage.messageId?.let { messageId ->
                            _uiState.update { state ->
                                val updatedMessages = state.messages.map { msg ->
                                    if (msg.id == messageId) {
                                        msg.copy(status = com.seend.app.data.model.MessageStatus.READ)
                                    } else msg
                                }
                                state.copy(messages = updatedMessages)
                            }
                        }
                    }
                    "user_status" -> {
                        wsMessage.userId?.let { userId ->
                            _uiState.update { state ->
                                state.copy(
                                    otherUser = state.otherUser?.copy(
                                        isOnline = wsMessage.online ?: false,
                                        lastSeen = wsMessage.lastSeen
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
