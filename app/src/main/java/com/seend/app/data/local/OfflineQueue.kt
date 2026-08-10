package com.seend.app.data.local

import androidx.room.*

@Entity(tableName = "offline_queue")
data class OfflineMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val receiverId: String,
    val content: String,
    val status: Int = 0,  // 0=pendiente, 1=enviado, 2=error
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface OfflineQueueDao {
    @Insert
    suspend fun insert(msg: OfflineMessage): Long
    
    @Query("SELECT * FROM offline_queue WHERE status = 0 ORDER BY createdAt ASC")
    suspend fun getPending(): List<OfflineMessage>
    
    @Query("UPDATE offline_queue SET status = 1 WHERE id = :id")
    suspend fun markAsSent(id: Long)
    
    @Query("UPDATE offline_queue SET status = 2 WHERE id = :id")
    suspend fun markAsError(id: Long)
    
    @Query("DELETE FROM offline_queue WHERE status = 1")
    suspend fun clearSent()
    
    @Query("SELECT COUNT(*) FROM offline_queue WHERE status = 0")
    suspend fun getPendingCount(): Int
}
