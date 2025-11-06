package com.devusercode.core.domain.user.repo

import com.devusercode.core.domain.user.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun setOnline(online: Boolean)
    suspend fun setLastSeen(epochMs: Long)
    fun observePresence(userId: String): Flow<Pair<Boolean, Long?>>
    suspend fun signOut()
}
