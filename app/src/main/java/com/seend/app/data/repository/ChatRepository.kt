package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.model.Chat
import com.seend.app.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val api: SeendApi) {
    
    suspend fun getChats(): Result<List<Chat>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChats()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
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
    
    suspend fun getMessages(chatId: String): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMessages(chatId)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
