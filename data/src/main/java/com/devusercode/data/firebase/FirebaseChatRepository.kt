package com.devusercode.data.firebase

import com.devusercode.core.domain.chat.model.UserPair
import com.devusercode.core.domain.chat.repo.ChatRepository
import com.devusercode.core.domain.user.model.User
import com.devusercode.data.local.db.dao.ConversationDao
import com.devusercode.data.local.mapper.toDomain
import com.devusercode.data.local.mapper.toEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseChatRepository(
    private val db: FirebaseDatabase,
    private val conversationDao: ConversationDao,
) : ChatRepository {
    override suspend fun listOpenConversations(currentUid: String): List<UserPair> =
        withContext(Dispatchers.IO) {
            // 1) Try cache first
            val cached = conversationDao.getAll()
            if (cached.isNotEmpty()) {
                return@withContext cached.map { it.toDomain() }
            }

            // 2) Fallback to network
            val fresh = fetchFromNetwork(currentUid)

            // 3) Upsert cache
            if (fresh.isNotEmpty()) {
                conversationDao.clearAll()
                conversationDao.upsertAll(fresh.map { it.toEntity() })
            }

            return@withContext fresh
        }

    private suspend fun fetchFromNetwork(currentUid: String): List<UserPair> {
        val convSnap =
            db
                .getReference("users")
                .child(currentUid)
                .child("conversations")
                .get()
                .await()
        val cids = convSnap.children.mapNotNull { it.key }
        val res = mutableListOf<UserPair>()

        for (cid in cids) {
            val parts =
                db
                    .getReference("conversations")
                    .child(cid)
                    .child("participants")
                    .get()
                    .await()
            val otherUid = parts.children.mapNotNull { it.key }.firstOrNull { it != currentUid } ?: continue
            val otherSnap =
                db
                    .getReference("users")
                    .child(otherUid)
                    .get()
                    .await()
            val user = otherSnap.toUser(otherUid)
            val last =
                db
                    .getReference("conversations")
                    .child(cid)
                    .child("lastMessage")
                    .get()
                    .await()
            val text = last.child("text").getValue(String::class.java)
            val time = last.child("time").getValue(Long::class.java)

            res += UserPair(user, cid, text, time)
        }

        return res
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
