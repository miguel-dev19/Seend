package com.seend.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val profilePic: String,
    val info: String,
    val lastSeen: String?,
    val isOnline: Boolean = false
)

@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["otherUserId"]),
        Index(value = ["lastTime"])
    ]
)
data class ChatEntity(
    @PrimaryKey val id: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherProfilePic: String,
    val lastMessage: String?,
    val lastTime: String?,
    val unreadCount: Int = 0,
    val lastMsgStatus: String?
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val status: String,
    val createdAt: String
)

@Entity(tableName = "offline_queue")
data class OfflineMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val receiverId: String,
    val content: String,
    val status: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
