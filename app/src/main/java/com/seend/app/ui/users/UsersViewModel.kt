package com.seend.app.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.model.User
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdChatId: String? = null
)

class UsersViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState

    init {
        observeUsersFromRoom()
        syncUsers()
    }

    // Flow desde Room: ordenados por online primero, luego última conexión
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

    // Sincronizar en background
    private fun syncUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.syncUsers()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun createChat(userId: String) {
        viewModelScope.launch {
            chatRepository.createOrGetChat(userId).fold(
                onSuccess = { chatId -> _uiState.update { it.copy(createdChatId = chatId) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun clearCreatedChat() {
        _uiState.update { it.copy(createdChatId = null) }
    }
}
