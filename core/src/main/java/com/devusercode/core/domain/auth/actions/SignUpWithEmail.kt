package com.devusercode.core.domain.auth.actions

import com.devusercode.core.domain.auth.repo.AuthRepository

class SignUpWithEmail(
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String?,
    ): String = repo.signUpWithEmail(email, password, displayName)
}
