package com.seend.app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

object TokenManager {
    
    private const val PREFS_NAME = "seend_prefs"
    private const val TOKEN_KEY = "jwt_token"
    private const val USERNAME_KEY = "username"
    private const val USER_ID_KEY = "user_id"
    
    private lateinit var prefs: SharedPreferences
    
    private val _tokenFlow = MutableStateFlow<String?>(null)
    val tokenFlow: Flow<String?> = _tokenFlow
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _tokenFlow.value = prefs.getString(TOKEN_KEY, null)
    }
    
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
        _tokenFlow.value = token
    }
    
    fun saveUsername(username: String) {
        prefs.edit().putString(USERNAME_KEY, username).apply()
    }
    
    fun saveUserId(userId: String) {
        prefs.edit().putString(USER_ID_KEY, userId).apply()
    }
    
    fun getToken(): Flow<String?> = _tokenFlow
    
    fun getTokenOnce(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }
    
    fun getUsername(): String? {
        return prefs.getString(USERNAME_KEY, null)
    }
    
    fun getUserId(): String? {
        return prefs.getString(USER_ID_KEY, null)
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
        _tokenFlow.value = null
    }
}
