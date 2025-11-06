package com.devusercode.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.devusercode.data.local.db.entity.ConversationEntity

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation ORDER BY CASE WHEN lastMessageTimeEpochMs IS NULL THEN 1 ELSE 0 END, lastMessageTimeEpochMs DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Upsert
    suspend fun upsertAll(items: List<ConversationEntity>)

    @Query("DELETE FROM conversation")
    suspend fun clearAll()
}
