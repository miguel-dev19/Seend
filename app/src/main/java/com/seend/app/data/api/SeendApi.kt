package com.seend.app.data.api

import com.seend.app.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class GlobalMessageRow(
    val id: String,
    @SerializedName("sender_id") val senderId: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    val username: String,
    @SerializedName("profile_pic") val profilePic: String
)

interface SeendApi {
    
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>
    
    @GET("api/global/messages")
    suspend fun getGlobalMessages(@Query("offset") offset: Int): Response<List<GlobalMessageRow>>
}
