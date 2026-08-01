package com.seend.app.data.api

import com.seend.app.data.model.AuthRequest
import com.seend.app.data.model.AuthResponse
import com.seend.app.data.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
