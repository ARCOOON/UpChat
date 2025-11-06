package com.devusercode.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devusercode.core.domain.auth.repo.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val Context.userStore by preferencesDataStore("auth_prefs")

class FirebaseAuthRepository(
    private val ctx: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) : AuthRepository {

    private val KEY_REMEMBER = booleanPreferencesKey("remember_me")
    private val KEY_EMAIL = stringPreferencesKey("saved_email")

    override suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): String {
        // 1) Create auth user
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("User creation failed")
        val uid = user.uid

        // 2) Optional displayName on the auth profile
        if (!displayName.isNullOrBlank()) {
            val req = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(req).await()
        }

        // 3) Bootstrap user metadata in RTDB
        // Schema (simple, extensible):
        // /users/{uid} {
        //   email, info: { username }, photoUrl, online, lastSeen, createdAt
        //   conversations: { cid: true }    // added later by chat flows
        // }
        val userRef = db.getReference("users").child(uid)
        val payload = mapOf(
            "email" to email,
            "info" to mapOf(
                "username" to (displayName ?: email.substringBefore("@"))
            ),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "online" to "false",
            "lastSeen" to ServerValue.TIMESTAMP,
            "createdAt" to ServerValue.TIMESTAMP
        )
        userRef.updateChildren(payload).await()

        return uid
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun observeRememberMe(): Flow<Boolean> =
        ctx.userStore.data.map { it[KEY_REMEMBER] ?: false }

    override fun observeSavedEmail(): Flow<String?> =
        ctx.userStore.data.map { it[KEY_EMAIL] }

    override suspend fun setRememberMe(remember: Boolean) {
        ctx.userStore.edit { it[KEY_REMEMBER] = remember }
    }

    override suspend fun setSavedEmail(email: String?) {
        ctx.userStore.edit { prefs ->
            if (email.isNullOrBlank()) prefs.remove(KEY_EMAIL) else prefs[KEY_EMAIL] = email
        }
    }

    override suspend fun deleteCurrentUser(
        reAuthEmail: String?,
        reAuthPassword: String?
    ) {
        val current = auth.currentUser ?: return
        val uid = current.uid

        // 1) Best-effort DB cleanup
        // - Remove user node
        // - Remove user's conversation links (do NOT delete whole conversations; other members may exist)
        val userRef = db.getReference("users").child(uid)
        val convsSnap = userRef.child("conversations").get().await()
        val batch = mutableMapOf<String, Any?>()

        // delete each participant link under conversations/{cid}/participants/{uid}
        convsSnap.children.forEach { child ->
            val cid = child.key ?: return@forEach
            batch["conversations/$cid/participants/$uid"] = null
        }
        // delete the user root
        batch["users/$uid"] = null

        if (batch.isNotEmpty()) {
            db.reference.updateChildren(batch).await()
        }

        // 2) Delete Auth user (may require recent login)
        try {
            current.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // Attempt reauth if credentials provided
            if (!reAuthEmail.isNullOrBlank() && !reAuthPassword.isNullOrBlank()) {
                reauthenticate(reAuthEmail, reAuthPassword)
                // try delete again
                auth.currentUser?.delete()?.await()
            } else {
                throw e
            }
        }
    }

    override suspend fun reauthenticate(email: String, password: String) {
        val user = auth.currentUser ?: error("No authenticated user")
        val cred = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(cred).await()
    }
}
