package com.devusercode.core.domain.auth.actions
import com.devusercode.core.domain.auth.repo.AuthRepository

class SendPasswordReset(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String) = repo.sendPasswordReset(email)
}
