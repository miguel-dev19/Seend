package com.seend.app.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.local.ChatEntity
import com.seend.app.data.model.User
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.di.AppModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingChat: Boolean = false,
    val error: String? = null,
    val createdChatId: String? = null
)

class UsersViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState

    init {
        observeUsersFromRoom()
        syncUsers()
        observeWebSocket()
    }

    private fun observeUsersFromRoom() {
        viewModelScope.launch {
            userRepository.getUsersFlow().collect { users ->
                val sorted = users.sortedWith(
                    compareByDescending<User> { it.isOnline }
                        .thenByDescending { it.lastSeen ?: "" }
                )
                _uiState.update { it.copy(users = sorted) }
            }
        }
    }

    private fun syncUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.syncUsers()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketManager.messages.collect { ws ->
                if (ws.type == "user_status") {
                    ws.userId?.let { uid ->
                        _uiState.update { state ->
                            state.copy(users = state.users.map { user ->
                                if (user.id == uid) user.copy(isOnline = ws.online ?: false, lastSeen = ws.lastSeen)
                                else user
                            }.sortedWith(compareByDescending<User> { it.isOnline }.thenByDescending { it.lastSeen ?: "" }))
                        }
                    }
                }
            }
        }
    }

    fun createChat(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingChat = true, error = null) }
            
            // 1. LOCAL PRIMERO: Crear chat en Room inmediatamente
            val localChatId = UUID.randomUUID().toString()
            val chatEntity = ChatEntity(
                id = localChatId,
                otherUserId = user.id,
                otherUsername = user.username,
                otherProfilePic = user.profilePic,
                lastMessage = "",
                lastTime = "",
                unreadCount = 0,
                lastMsgStatus = ""
            )
            
            try {
                AppModule.provideDatabase().chatDao().upsertChat(chatEntity)
            } catch (e: Exception) {
                // Continuar con chatId local
            }
            
            _uiState.update { it.copy(isCreatingChat = false, createdChatId = localChatId) }
            
            // 2. SYNC DESPUÉS: Crear en servidor en background
            chatRepository.createOrGetChat(user.id).fold(
                onSuccess = { serverChatId ->
                    // Actualizar Room con chatId real
                    _uiState.update { it.copy(createdChatId = serverChatId) }
                },
                onFailure = { e ->
                    // Mantener chat local, sincronizará después
                    _uiState.update { it.copy(error = "Sin conexión, chat guardado localmente") }
                }
            )
        }
    }

    fun clearCreatedChat() {
        _uiState.update { it.copy(createdChatId = null) }
    }
}
