package com.seend.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.repository.ChatRepository

class ChatsViewModelFactory(
    private val chatRepository: ChatRepository,
    private val webSocketManager: WebSocketManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
            return ChatsViewModel(chatRepository, webSocketManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
