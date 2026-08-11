package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.local.*
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
    
    // Flow desde Room: UI se actualiza automáticamente
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
    
    // Sincronizar chats con servidor
    suspend fun syncChats(): Result<List<Chat>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChats()
                if (response.isSuccessful) {
                    val chats = response.body() ?: emptyList()
                    val entities = chats.map { chat ->
                        ChatEntity(
                            id = chat.id,
                            otherUserId = chat.otherUser.id,
                            otherUsername = chat.otherUser.username,
                            otherProfilePic = chat.otherUser.profilePic,
                            lastMessage = chat.lastMessage ?: "",
                            lastTime = chat.lastTime ?: "",
                            unreadCount = chat.unreadCount,
                            lastMsgStatus = chat.lastMsgStatus?.name ?: ""
                        )
                    }
                    db.chatDao().upsertChats(entities)
                    Result.success(chats)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Flow de mensajes desde Room
    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        return db.messageDao().getMessagesFlow(chatId).map { entities ->
            entities.map { Message(it.id, it.chatId, it.senderId, it.content, MessageStatus.valueOf(it.status), it.createdAt) }
        }
    }
    
    // Sincronizar mensajes con servidor
    suspend fun syncMessages(chatId: String): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMessages(chatId)
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    val entities = messages.map { msg ->
                        MessageEntity(
                            id = msg.id, chatId = msg.chatId, senderId = msg.senderId,
                            content = msg.content, status = msg.status.name, createdAt = msg.createdAt
                        )
                    }
                    db.messageDao().upsertMessages(entities)
                    Result.success(messages)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun createOrGetChat(userId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createOrGetChat(userId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!.chatId)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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
