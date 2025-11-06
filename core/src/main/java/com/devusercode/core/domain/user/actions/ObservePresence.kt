package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.repo.UserRepository
import kotlinx.coroutines.flow.Flow

class ObservePresence(private val repo: UserRepository) {
    operator fun invoke(userId: String): Flow<Pair<Boolean, Long?>> = repo.observePresence(userId)
}
