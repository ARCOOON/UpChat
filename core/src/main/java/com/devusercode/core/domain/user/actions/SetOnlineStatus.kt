package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.repo.UserRepository

class SetOnlineStatus(private val repo: UserRepository) {
    suspend operator fun invoke(online: Boolean) = repo.setOnline(online)
}
