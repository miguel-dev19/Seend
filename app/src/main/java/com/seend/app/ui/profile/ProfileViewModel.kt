package com.seend.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.model.User
import com.seend.app.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeUserFromRoom()
        loadProfile()
    }

    // Flow: observar usuario desde Room
    private fun observeUserFromRoom() {
        viewModelScope.launch {
            userRepository.getUsersFlow().collect { users ->
                val found = users.find { it.id == userId }
                if (found != null) _user.value = found
            }
        }
    }

    // Cargar del servidor
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUserProfile(userId).fold(
                onSuccess = { user -> _user.value = user },
                onFailure = {}
            )
            _isLoading.value = false
        }
    }
}
