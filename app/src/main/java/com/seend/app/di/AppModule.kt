package com.seend.app.di

import android.content.Context
import com.seend.app.data.api.AuthApi
import com.seend.app.data.api.SeendApi
import com.seend.app.data.api.TokenInterceptor
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.repository.AuthRepository
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
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
    
    private var database: SeendDatabase? = null
    
    fun initDatabase(context: Context) {
        database = SeendDatabase.getInstance(context)
    }
    
    fun provideWebSocketManager(): WebSocketManager = WebSocketManager()
    
    fun provideDatabase(): SeendDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call initDatabase() first.")
    }
    
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
    fun provideUserRepository(): UserRepository = UserRepository(provideSeendApi(), provideDatabase())
    fun provideChatRepository(): ChatRepository = ChatRepository(provideSeendApi(), provideDatabase())
}
