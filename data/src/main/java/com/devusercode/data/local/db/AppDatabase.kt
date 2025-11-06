package com.devusercode.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devusercode.data.local.db.dao.ConversationDao
import com.devusercode.data.local.db.entity.ConversationEntity

@Database(
    entities = [ConversationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
