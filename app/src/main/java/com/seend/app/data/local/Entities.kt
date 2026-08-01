package com.seend.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val profilePic: String,
    val info: String,
    val lastSeen: String?,
    val isOnline: Boolean = false
)

@Entity(tableName = "chats")
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

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val status: String,
    val createdAt: String
)
