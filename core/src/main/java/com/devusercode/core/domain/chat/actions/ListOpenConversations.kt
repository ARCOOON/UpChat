package com.devusercode.core.domain.chat.actions

import com.devusercode.core.domain.chat.model.UserPair
import com.devusercode.core.domain.chat.repo.ChatRepository
import javax.inject.Inject

class ListOpenConversations
    @Inject
    constructor(
        private val repo: ChatRepository,
    ) {
        suspend operator fun invoke(currentUid: String): List<UserPair> = repo.listOpenConversations(currentUid)
    }
