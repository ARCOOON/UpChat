package com.devusercode.core.domain.user.actions

import com.devusercode.core.domain.user.repo.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePresence
    @Inject
    constructor(
        private val repo: UserRepository,
    ) {
        operator fun invoke(userId: String): Flow<Pair<Boolean, Long?>> = repo.observePresence(userId)
    }
