package com.seend.app.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.model.User
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
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
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            userRepository.getUsers().fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            users = users,
                            filteredUsers = filterUsers(users, it.searchQuery),
                            isLoading = false,
                            error = null
                        )
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredUsers = filterUsers(it.users, query)
            )
        }
    }

    private fun filterUsers(users: List<User>, query: String): List<User> {
        return if (query.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.username.contains(query, ignoreCase = true)
            }
        }
    }

    fun createChat(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            chatRepository.createOrGetChat(userId).fold(
                onSuccess = { chatId ->
                    _uiState.update {
                        it.copy(isLoading = false, createdChatId = chatId)
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

    fun clearCreatedChat() {
        _uiState.update { it.copy(createdChatId = null) }
    }
}
