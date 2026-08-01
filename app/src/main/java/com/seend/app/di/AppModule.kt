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
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun provideAuthApi(): AuthApi = retrofit.create(AuthApi::class.java)
    
    fun provideSeendApi(): SeendApi = retrofit.create(SeendApi::class.java)
    
    fun provideAuthRepository(): AuthRepository = AuthRepository(provideAuthApi())
    
    fun provideUserRepository(): UserRepository = UserRepository(provideSeendApi())
    
    fun provideChatRepository(): ChatRepository = ChatRepository(provideSeendApi())
}
