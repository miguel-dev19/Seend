package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.local.ChatEntity
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.model.Chat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(
    private val api: SeendApi,
    private val db: SeendDatabase
) {
    
    suspend fun getChats(): Result<List<Chat>> {
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
                            lastMessage = chat.lastMessage,
                            lastTime = chat.lastTime,
                            unreadCount = chat.unreadCount,
                            lastMsgStatus = chat.lastMsgStatus?.name
                        )
                    }
                    db.chatDao().insertChats(entities)
                    Result.success(chats)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun getChatsFlow(): Flow<List<ChatEntity>> = db.chatDao().getAllChats()
    
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
    
    suspend fun markChatAsRead(chatId: String) {
        db.chatDao().markAsRead(chatId)
    }
}
