package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.repo.UserRepository
import javax.inject.Inject

class SignOut
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        suspend operator fun invoke() = repo.signOut()
    }
