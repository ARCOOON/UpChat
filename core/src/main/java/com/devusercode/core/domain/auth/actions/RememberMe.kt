package com.devusercode.core.domain.auth.actions
import com.devusercode.core.domain.auth.repo.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveRememberMe(private val repo: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = repo.observeRememberMe()
}

class ObserveSavedEmail(private val repo: AuthRepository) {
    operator fun invoke(): Flow<String?> = repo.observeSavedEmail()
}

class SetRememberMe(private val repo: AuthRepository) {
    suspend operator fun invoke(remember: Boolean) = repo.setRememberMe(remember)
}

class SetSavedEmail(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String?) = repo.setSavedEmail(email)
}
