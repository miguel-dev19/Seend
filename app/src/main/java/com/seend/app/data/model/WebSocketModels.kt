package com.seend.app.data.model

import com.google.gson.annotations.SerializedName

data class WsSendMessage(
    val type: String = "message",
    val data: MessageData
)

data class MessageData(
    @SerializedName("chat_id")
    val chatId: String,
    val content: String,
    @SerializedName("receiver_id")
    val receiverId: String
)

data class WsReceiveMessage(
    val type: String,
    val message: WsMessage? = null,
    @SerializedName("chat_id")
    val chatId: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    val typing: Boolean? = null,
    @SerializedName("message_id")
    val messageId: String? = null,
    @SerializedName("read_by")
    val readBy: String? = null,
    val online: Boolean? = null,
    @SerializedName("last_seen")
    val lastSeen: String? = null
)

data class WsMessage(
    val id: String,
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("sender_id")
    val senderId: String,
    val content: String,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class WsTyping(
    val type: String = "typing",
    val data: TypingData
)

data class TypingData(
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("is_typing")
    val isTyping: Boolean
)

data class WsReadReceipt(
    val type: String = "read",
    val data: ReadData
)

data class ReadData(
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("message_id")
    val messageId: String
)
