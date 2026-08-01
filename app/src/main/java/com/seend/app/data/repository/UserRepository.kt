package com.seend.app.data.repository

import com.seend.app.data.api.SeendApi
import com.seend.app.data.local.SeendDatabase
import com.seend.app.data.local.UserEntity
import com.seend.app.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: SeendApi,
    private val db: SeendDatabase
) {
    
    suspend fun getUsers(): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    // Guardar en Room
                    val entities = users.map { it.toEntity() }
                    db.userDao().insertUsers(entities)
                    Result.success(users)
                } else {
                    // Si falla, cargar de Room
                    val localUsers = db.userDao().getAllUsers()
                    Result.success(emptyList())
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
                    db.userDao().insertUser(user.toEntity())
                    Result.success(user)
                } else {
                    // Cargar de Room
                    val localUser = db.userDao().getUserById(userId)
                    if (localUser != null) {
                        Result.success(localUser.toModel())
                    } else {
                        Result.failure(Exception("Usuario no encontrado"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

fun User.toEntity() = UserEntity(id, username, profilePic, info, lastSeen, isOnline)
fun UserEntity.toModel() = User(id, username, profilePic, info, lastSeen, isOnline)
