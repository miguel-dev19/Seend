package com.seend.app.data.repository

import com.seend.app.data.api.AuthApi
import com.seend.app.data.model.AuthRequest
import com.seend.app.data.model.AuthResponse
import com.seend.app.data.model.CheckUsernameResponse
import com.seend.app.data.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val authApi: AuthApi) {
    
    suspend fun register(username: String, password: String, photo: String? = null): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(AuthRequest(username, password, photo))
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun checkUsername(username: String): Result<CheckUsernameResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.checkUsername(username)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
