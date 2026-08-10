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

enum class MessageStatus(val level: Int) {
    @SerializedName("sending")
    SENDING(0),
    @SerializedName("sent")
    SENT(1),
    @SerializedName("delivered")
    DELIVERED(2),
    @SerializedName("read")
    READ(3);
    
    // Solo actualiza si el nuevo estado es mayor (progresivo)
    fun canUpgradeTo(newStatus: MessageStatus): Boolean {
        return newStatus.level > this.level
    }
}
