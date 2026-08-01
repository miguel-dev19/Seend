package com.seend.app.data.api

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.UUID

object S3Uploader {
    
    private const val S3_BASE_URL = "https://s3.todus.cu/stream/profile_pics"
    
    private val client = OkHttpClient()
    
    suspend fun uploadPhoto(context: Context, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Leer la imagen del URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext Result.failure(Exception("No se pudo abrir la imagen"))
                
                val bytes = inputStream.use { it.readBytes() }
                inputStream.close()
                
                // Generar nombre único
                val fileName = "${UUID.randomUUID()}.jpg"
                val url = "$S3_BASE_URL/$fileName"
                
                // Subir a S3 con PUT
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Error al subir: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
