package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.model.User
import com.devusercode.core.domain.user.repo.UserRepository

class GetCurrentUser(
    private val repo: UserRepository,
) {
    suspend operator fun invoke(): User = repo.getCurrentUser()
}
