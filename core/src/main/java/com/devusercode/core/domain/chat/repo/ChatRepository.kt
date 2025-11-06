package com.devusercode.core.domain.chat.repo

import com.devusercode.core.domain.chat.model.UserPair

interface ChatRepository {
    suspend fun listOpenConversations(currentUid: String): List<UserPair>
}
