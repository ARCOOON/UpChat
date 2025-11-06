package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.repo.UserRepository

class SignOut(private val repo: UserRepository) {
    suspend operator fun invoke() = repo.signOut()
}
