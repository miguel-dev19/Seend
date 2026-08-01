package com.seend.app.di

import com.seend.app.data.api.AuthApi
import com.seend.app.data.api.SeendApi
import com.seend.app.data.api.TokenInterceptor
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.repository.AuthRepository
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {
    
    private const val BASE_URL = "https://seend-server.onrender.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    fun provideWebSocketManager(): WebSocketManager = WebSocketManager()
    
    private fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private fun provideRetrofit(tokenManager: TokenManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun provideAuthApi(tokenManager: TokenManager): AuthApi {
        return provideRetrofit(tokenManager).create(AuthApi::class.java)
    }
    
    fun provideSeendApi(tokenManager: TokenManager): SeendApi {
        return provideRetrofit(tokenManager).create(SeendApi::class.java)
    }
    
    fun provideAuthRepository(authApi: AuthApi): AuthRepository = AuthRepository(authApi)
    
    fun provideUserRepository(seendApi: SeendApi): UserRepository = UserRepository(seendApi)
    
    fun provideChatRepository(seendApi: SeendApi): ChatRepository = ChatRepository(seendApi)
}
