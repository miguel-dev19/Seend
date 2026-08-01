package com.seend.app.ui.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.S3Uploader
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.AuthResponse
import com.seend.app.data.repository.AuthRepository
import com.seend.app.util.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _usernameStatus = MutableStateFlow<UsernameStatus>(UsernameStatus.Idle)
    val usernameStatus: StateFlow<UsernameStatus> = _usernameStatus

    private val _isCheckingUsername = MutableStateFlow(false)
    val isCheckingUsername: StateFlow<Boolean> = _isCheckingUsername

    private var checkUsernameJob: Job? = null
    
    // Contexto para el uploader (se asigna desde la pantalla)
    var appContext: Context? = null

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = TokenManager.getTokenOnce()
            _isLoggedIn.value = token != null
        }
    }

    fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        
        if (username.length < 3) {
            _usernameStatus.value = UsernameStatus.Idle
            _isCheckingUsername.value = false
            return
        }

        _isCheckingUsername.value = true
        
        checkUsernameJob = viewModelScope.launch {
            delay(500)
            
            val result = authRepository.checkUsername(username)
            
            result.fold(
                onSuccess = { response ->
                    _usernameStatus.value = if (response.available) {
                        UsernameStatus.Available(response.message)
                    } else {
                        UsernameStatus.Unavailable(response.message)
                    }
                },
                onFailure = {
                    _usernameStatus.value = UsernameStatus.Error("Error al verificar")
                }
            )
            
            _isCheckingUsername.value = false
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(username, password)
            
            result.fold(
                onSuccess = { authResponse ->
                    saveSession(authResponse)
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Error al iniciar sesión"
                }
            )
            
            _isLoading.value = false
        }
    }

    fun register(name: String, username: String, password: String, photoUri: Uri?) {
        if (name.isBlank() || username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Si hay foto, subirla a S3 primero
            var photoBase64: String? = null
            val context = appContext
            
            if (photoUri != null && context != null) {
                val uploadResult = S3Uploader.uploadPhoto(context, photoUri)
                photoBase64 = uploadResult.getOrNull() // URL de S3
            }

            val result = authRepository.register(username, password, photoBase64)
            
            result.fold(
                onSuccess = { authResponse ->
                    saveSession(authResponse)
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Error al registrar"
                }
            )
            
            _isLoading.value = false
        }
    }

    private suspend fun saveSession(authResponse: AuthResponse) {
        TokenManager.saveToken(authResponse.token)
        TokenManager.saveUsername(authResponse.user.username)
        TokenManager.saveUserId(authResponse.user.id)
        
        webSocketManager.connect(authResponse.token)
        
        _loginSuccess.value = true
        _isLoggedIn.value = true
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            webSocketManager.disconnect()
            TokenManager.clearAll()
            _isLoggedIn.value = false
            _loginSuccess.value = false
        }
    }
}

sealed class UsernameStatus {
    object Idle : UsernameStatus()
    data class Available(val message: String) : UsernameStatus()
    data class Unavailable(val message: String) : UsernameStatus()
    data class Error(val message: String) : UsernameStatus()
}
