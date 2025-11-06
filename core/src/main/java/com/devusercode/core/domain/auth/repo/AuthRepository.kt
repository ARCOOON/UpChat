package com.devusercode.core.domain.auth.repo

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): String
    suspend fun sendPasswordReset(email: String)
    fun observeRememberMe(): Flow<Boolean>
    fun observeSavedEmail(): Flow<String?>
    suspend fun setRememberMe(remember: Boolean)
    suspend fun setSavedEmail(email: String?)

    suspend fun deleteCurrentUser(reAuthEmail: String? = null, reAuthPassword: String? = null)

    suspend fun reauthenticate(email: String, password: String)
}
