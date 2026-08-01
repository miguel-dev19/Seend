package com.seend.app.data.api

import com.seend.app.data.model.AuthRequest
import com.seend.app.data.model.AuthResponse
import com.seend.app.data.model.CheckUsernameResponse
import com.seend.app.data.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("api/auth/check-username/{username}")
    suspend fun checkUsername(@Path("username") username: String): Response<CheckUsernameResponse>
}
