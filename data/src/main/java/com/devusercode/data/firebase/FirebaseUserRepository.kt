@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.devusercode.data.firebase

import com.devusercode.core.domain.user.model.User
import com.devusercode.core.domain.user.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
) : UserRepository {
    override suspend fun getCurrentUser(): User {
        val uid = auth.currentUser?.uid ?: error("No authenticated user")
        val snap =
            db
                .getReference("users")
                .child(uid)
                .get()
                .await()
        return snap.toUser(uid)
    }

    override suspend fun setOnline(online: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.getReference("users").child(uid)
        ref.child("online").setValue(online.toString()).await()
        if (!online) ref.child("lastSeen").setValue(System.currentTimeMillis()).await()
        ref.child("online").onDisconnect().setValue("false")
        ref.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)
    }

    override suspend fun setLastSeen(epochMs: Long) {
        val uid = auth.currentUser?.uid ?: return
        db
            .getReference("users")
            .child(uid)
            .child("lastSeen")
            .setValue(epochMs)
            .await()
    }

    override fun observePresence(userId: String): Flow<Pair<Boolean, Long?>> =
        callbackFlow {
            val ref = db.getReference("users").child(userId)
            val listener =
                object : ValueEventListener {
                    override fun onDataChange(s: DataSnapshot) {
                        val online = s.child("online").getValue(String::class.java)?.toBooleanStrictOrNull() ?: false
                        val last = s.child("lastSeen").getValue(Long::class.java)
                        trySend(online to last)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }

    override suspend fun signOut() {
        auth.signOut()
    }

    private fun DataSnapshot.toUser(uid: String): User {
        val name =
            child("info").child("username").getValue(String::class.java)
                ?: child("username").getValue(String::class.java)
        val photo = child("photoUrl").getValue(String::class.java)
        val online = child("online").getValue(String::class.java)?.toBooleanStrictOrNull() ?: false
        val last = child("lastSeen").getValue(Long::class.java)
        return User(uid, name, photo, online, last)
    }
}
