package com.seend.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val username: String,
    @SerializedName("profile_pic")
    val profilePic: String = "",
    val info: String = "¡Hola! Estoy usando Seend.",
    @SerializedName("last_seen")
    val lastSeen: String? = null,
    @SerializedName("is_online")
    val isOnline: Boolean = false,
    @SerializedName("is_typing")
    val isTyping: Boolean = false
)

data class AuthRequest(
    val username: String,
    val password: String,
    val photo: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class LoginRequest(
    val username: String,
    val password: String
)
