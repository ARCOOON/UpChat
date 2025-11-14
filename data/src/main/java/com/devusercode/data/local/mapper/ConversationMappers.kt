package com.devusercode.data.local.mapper

import com.devusercode.core.domain.chat.model.UserPair
import com.devusercode.core.domain.user.model.User
import com.devusercode.data.local.db.entity.ConversationEntity

fun ConversationEntity.toDomain(): UserPair =
    UserPair(
        user =
            User(
                uid = otherUserId,
                displayName = displayName,
                photoUrl = photoUrl,
                online = online,
                lastSeenEpochMs = lastSeenEpochMs,
            ),
        conversationId = conversationId,
        lastMessageText = lastMessageText,
        lastMessageTimeEpochMs = lastMessageTimeEpochMs,
    )

fun UserPair.toEntity(): ConversationEntity =
    ConversationEntity(
        conversationId = conversationId,
        otherUserId = user.uid,
        displayName = user.displayName,
        photoUrl = user.photoUrl,
        online = user.online,
        lastSeenEpochMs = user.lastSeenEpochMs,
        lastMessageText = lastMessageText,
        lastMessageTimeEpochMs = lastMessageTimeEpochMs,
    )
