package com.seend.app.ui.chats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.Message
import com.seend.app.data.model.User
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.di.AppModule
import com.seend.app.util.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class GlobalChatUiState(
    val messages: List<GlobalMessage> = emptyList(),
    val users: List<User> = emptyList(),
    val onlineCount: Int = 0,
    val typingUsers: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val currentOffset: Int = 0
)

data class GlobalMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val createdAt: String,
    val isMine: Boolean
)

class GlobalChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GlobalChatUiState())
    val uiState: StateFlow<GlobalChatUiState> = _uiState.asStateFlow()

    private val currentUserId = TokenManager.getUserId()
    private val offset = MutableStateFlow(0)

    init {
        observeUsersFromRoom()
        syncUsers()
        observeWebSocket()
    }

    private fun observeUsersFromRoom() {
        viewModelScope.launch {
            userRepository.getUsersFlow().collect { users ->
                val online = users.count { it.isOnline }
                _uiState.update { it.copy(users = users, onlineCount = online) }
            }
        }
    }

    private fun syncUsers() {
        viewModelScope.launch {
            userRepository.syncUsers()
        }
    }

    fun sendMessage(content: String) {
        val temp = GlobalMessage(
            id = UUID.randomUUID().toString(),
            senderId = currentUserId ?: "",
            senderName = "Tú",
            senderAvatar = "",
            content = content,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            isMine = true
        )
        _uiState.update { it.copy(messages = it.messages + temp) }
        
        webSocketManager.sendGlobalMessage(content)
    }

    fun sendTyping(isTyping: Boolean) {
        webSocketManager.sendTyping("global", isTyping)
    }

    // Cargar más mensajes (paginación)
    fun loadMoreMessages() {
        viewModelScope.launch {
            if (_uiState.value.isLoading || !_uiState.value.hasMore) return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            val nextOffset = offset.value + 20
            val api = com.seend.app.di.AppModule.provideSeendApi()
            
            try {
                val response = api.getGlobalMessages(nextOffset)
                if (response.isSuccessful) {
                    val rows = response.body() ?: emptyList()
                    if (rows.isEmpty()) {
                        _uiState.update { it.copy(hasMore = false, isLoading = false) }
                    } else {
                        val newMessages = rows.map { row ->
                            GlobalMessage(
                                id = row.id,
                                senderId = row.senderId,
                                senderName = row.username,
                                senderAvatar = row.profilePic,
                                content = row.content,
                                createdAt = row.createdAt,
                                isMine = row.senderId == currentUserId
                            )
                        }
                        _uiState.update {
                            it.copy(
                                messages = newMessages + it.messages,
                                isLoading = false,
                                currentOffset = nextOffset
                            )
                        }
                        offset.value = nextOffset
                    }
                } else {
                    _uiState.update { it.copy(hasMore = false, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(hasMore = false, isLoading = false) }
            }
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { ws ->
                when (ws.type) {
                    "message" -> {
                        ws.message?.let { msg ->
                            if (msg.chatId == "global") {
                                val globalMsg = GlobalMessage(
                                    id = msg.id,
                                    senderId = msg.senderId,
                                    senderName = msg.senderName ?: "Usuario",
                                    senderAvatar = msg.senderAvatar ?: "",
                                    content = msg.content,
                                    createdAt = msg.createdAt,
                                    isMine = msg.senderId == currentUserId
                                )
                                _uiState.update { state ->
                                    val existing = state.messages.indexOfFirst { it.id == msg.id }
                                    if (existing >= 0) {
                                        state.copy(messages = state.messages.toMutableList().also { it[existing] = globalMsg })
                                    } else {
                                        state.copy(messages = state.messages + globalMsg)
                                    }
                                }
                            }
                        }
                    }
                    "typing" -> {
                        if (ws.chatId == "global") {
                            ws.userId?.let { userId ->
                                _uiState.update { state ->
                                    val typing = state.typingUsers.toMutableList()
                                    if (ws.typing == true && userId !in typing) typing.add(userId)
                                    if (ws.typing == false) typing.remove(userId)
                                    state.copy(typingUsers = typing)
                                }
                            }
                        }
                    }
                    "user_status" -> {
                        ws.userId?.let { userId ->
                            _uiState.update { state ->
                                state.copy(
                                    users = state.users.map { user ->
                                        if (user.id == userId) user.copy(isOnline = ws.online ?: false)
                                        else user
                                    },
                                    onlineCount = state.users.count {
                                        if (it.id == userId) ws.online ?: false else it.isOnline
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun getTypingText(): String {
        val typing = _uiState.value.typingUsers
        if (typing.isEmpty()) return ""
        
        val names = typing.mapNotNull { userId ->
            _uiState.value.users.find { it.id == userId }?.username
        }
        
        return when {
            names.isEmpty() -> ""
            names.size == 1 -> "${names[0]} está escribiendo..."
            names.size == 2 -> "${names[0]} y ${names[1]} están escribiendo..."
            names.size == 3 -> "${names[0]}, ${names[1]} y ${names[2]} están escribiendo..."
            else -> "${names[0]}, ${names[1]} y ${names.size - 2} más están escribiendo..."
        }
    }
}
