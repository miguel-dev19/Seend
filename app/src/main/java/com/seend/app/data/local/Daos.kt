package com.seend.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsertUser(user: UserEntity)
    
    @Upsert
    suspend fun upsertUsers(users: List<UserEntity>)
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChat(chat: ChatEntity)
    
    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)
    
    @Query("SELECT * FROM chats ORDER BY lastTime DESC")
    fun getAllChatsFlow(): Flow<List<ChatEntity>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    
    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markAsRead(chatId: String)
    
    @Query("UPDATE chats SET lastMessage = :message, lastTime = :time, lastMsgStatus = :status WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, message: String, time: String, status: String)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)
}

@Dao
interface MessageDao {
    @Upsert
    suspend fun upsertMessage(message: MessageEntity)
    
    @Upsert
    suspend fun upsertMessages(messages: List<MessageEntity>)
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesFlow(chatId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    suspend fun getMessagesByChatId(chatId: String): List<MessageEntity>
    
    @Query("UPDATE messages SET status = :status WHERE id = :messageId AND status < :status")
    suspend fun updateMessageStatusProgressive(messageId: String, status: String)
    
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)
    
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface OfflineQueueDao {
    @Insert
    suspend fun insert(msg: OfflineMessage): Long
    
    @Query("SELECT * FROM offline_queue WHERE status = 0 ORDER BY createdAt ASC")
    suspend fun getPending(): List<OfflineMessage>
    
    @Query("SELECT * FROM offline_queue WHERE status = 0 ORDER BY createdAt ASC")
    fun getPendingFlow(): Flow<List<OfflineMessage>>
    
    @Query("UPDATE offline_queue SET status = 1 WHERE id = :id")
    suspend fun markAsSent(id: Long)
    
    @Query("UPDATE offline_queue SET status = 2 WHERE id = :id")
    suspend fun markAsError(id: Long)
    
    @Query("DELETE FROM offline_queue WHERE status = 1")
    suspend fun clearSent()
    
    @Query("SELECT COUNT(*) FROM offline_queue WHERE status = 0")
    fun getPendingCountFlow(): Flow<Int>
}
