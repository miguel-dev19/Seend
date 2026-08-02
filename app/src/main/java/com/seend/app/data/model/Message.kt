package com.seend.app.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: String = "",
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("sender_id")
    val senderId: String,
    val content: String,
    val status: MessageStatus = MessageStatus.SENDING,
    @SerializedName("created_at")
    val createdAt: String = ""
)

enum class MessageStatus {
    @SerializedName("sending")
    SENDING,
    @SerializedName("sent")
    SENT,
    @SerializedName("delivered")
    DELIVERED,
    @SerializedName("read")
    READ
}
