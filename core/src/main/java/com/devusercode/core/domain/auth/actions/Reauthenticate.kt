package com.devusercode.core.domain.auth.actions

import com.devusercode.core.domain.auth.repo.AuthRepository

class Reauthenticate(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repo.reauthenticate(email, password)
}
