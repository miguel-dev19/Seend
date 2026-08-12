package com.seend.app.data.api

import com.seend.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SeendApi {
    
    @GET("api/global/messages")
    suspend fun getGlobalMessages(@Query("offset") offset: Int): Response<List<GlobalMessageRow>>
    
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>
    
    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>
    
    @POST("api/chats/user/{userId}")
    suspend fun createOrGetChat(@Path("userId") userId: String): Response<CreateChatResponse>
    
    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): Response<List<Message>>
}

data class GlobalMessageRow(
    val id: String,
    @SerializedName("sender_id") val senderId: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("sender_avatar") val senderAvatar: String
)
