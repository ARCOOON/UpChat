package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.model.User
import com.devusercode.core.domain.user.repo.UserRepository
import javax.inject.Inject

class GetCurrentUser
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        suspend operator fun invoke(): User = repo.getCurrentUser()
    }
