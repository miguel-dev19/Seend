package com.seend.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, ChatEntity::class, MessageEntity::class, OfflineMessage::class],
    version = 2,
    exportSchema = false
)
abstract class SeendDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun offlineQueueDao(): OfflineQueueDao
    
    companion object {
        @Volatile
        private var INSTANCE: SeendDatabase? = null
        
        fun getInstance(context: Context): SeendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SeendDatabase::class.java,
                    "seend_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
