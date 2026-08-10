package com.seend.app.di

import android.content.Context
import com.seend.app.data.api.*
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.repository.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {
    
    private const val BASE_URL = "https://seend-server.onrender.com/"
    
    private var database: SeendDatabase? = null
    
    fun initDatabase(context: Context) {
        database = SeendDatabase.getInstance(context)
    }
    
    fun provideWebSocketManager(): WebSocketManager = WebSocketManager()
    
    fun provideDatabase(): SeendDatabase {
        return database ?: throw IllegalStateException("Database not initialized")
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor())
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator())
            .connectTimeout(15, TimeUnit.SECONDS)   // ← 15s como ToDus
            .readTimeout(15, TimeUnit.SECONDS)      // ← 15s como ToDus
            .writeTimeout(15, TimeUnit.SECONDS)     // ← 15s como ToDus
            .retryOnConnectionFailure(true)
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
