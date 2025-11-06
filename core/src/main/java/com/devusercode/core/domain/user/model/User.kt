package com.devusercode.core.domain.user.model

data class User(
    val uid: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val online: Boolean = false,
    val lastSeenEpochMs: Long? = null
)
