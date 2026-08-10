package com.seend.app.data.api

import com.seend.app.util.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Si ya hay un token, no re-intentar (evitar bucles)
        if (response.request.header("Authorization") != null) {
            return null
        }

        // Intentar refrescar token
        val newToken = runBlocking {
            TokenManager.getTokenOnce()
        }

        return if (newToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            null // No hay token, no re-intentar
        }
    }
}
