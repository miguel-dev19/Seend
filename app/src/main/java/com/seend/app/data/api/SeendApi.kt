package com.seend.app.data.api

import com.seend.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SeendApi {
    
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>
    
    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>
    
    @POST("api/chats/user/{userId}")
    suspend fun createOrGetChat(@Path("userId") userId: String): Response<CreateChatResponse>
}
