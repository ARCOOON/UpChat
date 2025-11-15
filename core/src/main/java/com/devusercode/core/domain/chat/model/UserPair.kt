package com.devusercode.core.domain.chat.model

import com.devusercode.core.domain.user.model.User

data class UserPair(
    val user: User,
    val conversationId: String,
    val lastMessageText: String? = null,
    val lastMessageTimeEpochMs: Long? = null,
)
