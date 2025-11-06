package com.devusercode.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val otherUserId: String,
    val displayName: String?,
    val photoUrl: String?,
    val online: Boolean,
    val lastSeenEpochMs: Long?,
    val lastMessageText: String?,
    val lastMessageTimeEpochMs: Long?
)
