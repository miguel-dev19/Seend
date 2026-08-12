package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.local.ChatEntity
import com.seend.app.data.local.MessageEntity
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.model.Chat
import com.seend.app.data.model.Message
import com.seend.app.data.model.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatRepository(
    private val api: SeendApi,
    private val db: SeendDatabase
) {
    
    // Flow desde Room
    fun getChatsFlow(): Flow<List<Chat>> {
        return db.chatDao().getAllChatsFlow().map { entities ->
            entities.map { entity ->
                Chat(
                    id = entity.id,
                    otherUser = com.seend.app.data.model.User(
                        id = entity.otherUserId,
                        username = entity.otherUsername,
                        profilePic = entity.otherProfilePic
                    ),
                    lastMessage = entity.lastMessage,
                    lastTime = entity.lastTime,
                    unreadCount = entity.unreadCount,
                    lastMsgStatus = entity.lastMsgStatus?.let { MessageStatus.valueOf(it) }
                )
            }
        }
    }
    
    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        return db.messageDao().getMessagesFlow(chatId).map { entities ->
            entities.map { Message(it.id, it.chatId, it.senderId, it.content, MessageStatus.valueOf(it.status), it.createdAt) }
        }
    }
    
    suspend fun updateLastMessage(chatId: String, message: String, time: String, status: String) {
        withContext(Dispatchers.IO) {
            db.chatDao().updateLastMessage(chatId, message, time, status)
        }
    }
    
    suspend fun updateMessageStatusProgressive(messageId: String, newStatus: String) {
        withContext(Dispatchers.IO) {
            db.messageDao().updateMessageStatusProgressive(messageId, newStatus)
        }
    }
    
    suspend fun markChatAsRead(chatId: String) {
        withContext(Dispatchers.IO) {
            db.chatDao().markAsRead(chatId)
        }
    }
}
