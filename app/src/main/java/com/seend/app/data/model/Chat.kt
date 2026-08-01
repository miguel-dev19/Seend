package com.seend.app.data.model

import com.google.gson.annotations.SerializedName

data class Chat(
    val id: String,
    @SerializedName("other_user")
    val otherUser: User,
    @SerializedName("last_message")
    val lastMessage: String? = null,
    @SerializedName("last_time")
    val lastTime: String? = null,
    @SerializedName("unread_count")
    val unreadCount: Int = 0,
    @SerializedName("last_msg_status")
    val lastMsgStatus: MessageStatus? = null
)

data class CreateChatResponse(
    @SerializedName("chat_id")
    val chatId: String
)
