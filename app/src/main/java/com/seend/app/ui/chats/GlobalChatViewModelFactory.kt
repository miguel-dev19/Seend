package com.seend.app.ui.chats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository

class GlobalChatViewModelFactory(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketManager: WebSocketManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalChatViewModel::class.java)) {
            return GlobalChatViewModel(application, chatRepository, userRepository, webSocketManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
