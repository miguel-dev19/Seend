package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.local.UserEntity
import com.seend.app.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: SeendApi,
    private val db: SeendDatabase
) {
    
    // Flow desde Room: la UI se actualiza automáticamente
    fun getUsersFlow(): Flow<List<User>> {
        return db.userDao().getAllUsersFlow().map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    // Sincronizar con servidor en background
    suspend fun syncUsers(): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val entities = users.map { it.toEntity() }
                    db.userDao().upsertUsers(entities)
                    Result.success(users)
                } else {
                    Result.failure(Exception("Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getUserProfile(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUserProfile(userId)
                if (response.isSuccessful) {
                    val user = response.body()!!
                    db.userDao().upsertUser(user.toEntity())
                    Result.success(user)
                } else {
                    loadLocalUser(userId)
                }
            } catch (e: Exception) {
                loadLocalUser(userId)
            }
        }
    }
    
    private suspend fun loadLocalUser(userId: String): Result<User> {
        val local = db.userDao().getUserById(userId)
        return if (local != null) {
            Result.success(local.toModel())
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    }
}

fun User.toEntity() = UserEntity(id, username, profilePic, info, lastSeen, isOnline)
fun UserEntity.toModel() = User(id, username, profilePic, info, lastSeen, isOnline)
