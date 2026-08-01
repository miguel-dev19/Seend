package com.seend.app.ui.auth

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.model.AuthResponse
import com.seend.app.data.repository.AuthRepository
import com.seend.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = TokenManager.getToken().first()
            _isLoggedIn.value = token != null
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

            val photoBase64 = photoUri?.let { uri ->
                try {
                    val context = TokenManager.javaClass.classLoader?.let { 
                        // Necesitamos el contexto, lo obtenemos del TokenManager
                        null 
                    }
                    null // Por ahora sin foto, necesitamos acceso al ContentResolver
                } catch (e: Exception) {
                    null
                }
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
        
        // Conectar WebSocket
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
