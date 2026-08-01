package com.seend.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository

class ChatDetailViewModelFactory(
    private val chatId: String,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketManager: WebSocketManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            return ChatDetailViewModel(chatId, chatRepository, userRepository, webSocketManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
