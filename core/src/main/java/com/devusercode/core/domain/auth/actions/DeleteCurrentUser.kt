package com.devusercode.core.domain.auth.actions

import com.devusercode.core.domain.auth.repo.AuthRepository

class DeleteCurrentUser(private val repo: AuthRepository) {
    suspend operator fun invoke(reAuthEmail: String? = null, reAuthPassword: String? = null) =
        repo.deleteCurrentUser(reAuthEmail, reAuthPassword)
}
