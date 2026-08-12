package com.seend.app.data.api

import android.util.Log
import com.google.gson.Gson
import com.seend.app.data.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager {
    
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    private val _messages = MutableSharedFlow<WsReceiveMessage>()
    val messages: SharedFlow<WsReceiveMessage> = _messages
    
    fun connect(token: String) {
        val request = Request.Builder()
            .url("wss://seend-server.onrender.com/api/ws?token=$token")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Conectado")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, WsReceiveMessage::class.java)
                    _messages.tryEmit(message)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parseando mensaje", e)
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error de conexión", t)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Desconectado")
            }
        })
    }
    
    fun sendMessage(chatId: String, content: String, receiverId: String) {
        val message = WsSendMessage(
            data = MessageData(
                chatId = chatId,
                content = content,
                receiverId = receiverId
            )
        )
        webSocket?.send(gson.toJson(message))
    }
    
    fun sendTyping(chatId: String, isTyping: Boolean) {
        val typing = WsTyping(
            data = TypingData(
                chatId = chatId,
                isTyping = isTyping
            )
        )
        webSocket?.send(gson.toJson(typing))
    }
    
    fun sendReadReceipt(chatId: String, messageId: String) {
        val readReceipt = WsReadReceipt(
            data = ReadData(
                chatId = chatId,
                messageId = messageId
            )
        )
        webSocket?.send(gson.toJson(readReceipt))
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Usuario desconectado")
    }
}

// Enviar mensaje al chat global
fun sendGlobalMessage(content: String) {
    val message = WsSendMessage(
        data = MessageData(
            chatId = "global",
            content = content,
            receiverId = ""
        )
    )
    webSocket?.send(gson.toJson(message))
}
