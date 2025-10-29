package com.devusercode.upchat.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.devusercode.upchat.models.User
import com.devusercode.upchat.utils.StorageController
import com.google.firebase.database.FirebaseDatabase
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

object ConversationKeyManager {
    private const val TAG = "ConversationKeyManager"
    private const val STORAGE_PUBLIC_KEY = "crypto.publicKey"
    private const val STORAGE_PRIVATE_KEY = "crypto.privateKey"

    private val secureRandom = SecureRandom()
    private val secretCache = ConcurrentHashMap<String, String>()

    data class KeyMaterial(
        val publicKey: PublicKey,
        val privateKey: PrivateKey,
        val encodedPublicKey: String
    )

    fun getCachedSecret(conversationId: String): String? = secretCache[conversationId]

    fun ensureConversationSecret(
        context: Context,
        conversationId: String,
        currentUser: User,
        participant: User,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cached = secretCache[conversationId]
        if (!cached.isNullOrBlank()) {
            onSuccess(cached)
            return
        }

        val storage = StorageController.getInstance(context)
        @Suppress("SENSELESS_COMPARISON")
        if (storage == null) {
            onError(IllegalStateException("Secure storage unavailable"))
            return
        }

        val currentUid = currentUser.uid
        val participantUid = participant.uid
        if (currentUid.isNullOrBlank() || participantUid.isNullOrBlank()) {
            onError(IllegalArgumentException("Missing conversation member identifiers"))
            return
        }

        val keyMaterial = ensureUserKeyPair(storage, currentUser) { error ->
            onError(error)
        } ?: return

        currentUser.publicKey = keyMaterial.encodedPublicKey

        val secretsRef = FirebaseDatabase.getInstance().reference
            .child("conversations")
            .child(conversationId)
            .child("secrets")

        secretsRef.child(currentUid).get()
            .addOnSuccessListener { snapshot ->
                val encryptedSecret = snapshot.getValue(String::class.java)

                if (!encryptedSecret.isNullOrBlank()) {
                    try {
                        val secret = ETE.decrypt(encryptedSecret, keyMaterial.privateKey)
                        secretCache[conversationId] = secret
                        onSuccess(secret)
                        return@addOnSuccessListener
                    } catch (error: Exception) {
                        if (error is BadPaddingException || error is IllegalBlockSizeException) {
                            Log.w(
                                TAG,
                                "Failed to decrypt conversation secret for $conversationId; regenerating",
                                error
                            )
                        } else {
                            Log.e(TAG, "Unexpected error decrypting conversation secret", error)
                        }
                    }
                }

                fetchParticipantPublicKey(
                    participantUid,
                    participant.publicKey,
                    onSuccess = { encodedParticipantKey ->
                        try {
                            val participantPublicKey = ETE.decodePublicKey(encodedParticipantKey)
                            participant.publicKey = encodedParticipantKey

                            val secret = generateSecret()
                            val encryptedForCurrent = ETE.encrypt(secret, keyMaterial.publicKey)
                            val encryptedForParticipant = ETE.encrypt(secret, participantPublicKey)

                            val updates = hashMapOf<String, Any>(
                                currentUid to encryptedForCurrent,
                                participantUid to encryptedForParticipant
                            )

                            secretsRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    secretCache[conversationId] = secret
                                    onSuccess(secret)
                                }
                                .addOnFailureListener { error ->
                                    onError(error)
                                }
                        } catch (error: Exception) {
                            onError(error)
                        }
                    },
                    onError = { error ->
                        onError(error)
                    }
                )
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    private fun fetchParticipantPublicKey(
        participantUid: String,
        cachedValue: String?,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!cachedValue.isNullOrBlank()) {
            onSuccess(cachedValue)
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(participantUid)
            .child("publicKey")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val encodedKey = dataSnapshot.getValue(String::class.java)
                if (!encodedKey.isNullOrBlank()) {
                    onSuccess(encodedKey)
                } else {
                    onError(IllegalStateException("Participant public key not available"))
                }
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    private fun ensureUserKeyPair(
        storage: StorageController,
        user: User,
        onError: (Exception) -> Unit
    ): KeyMaterial? {
        val storedPublic = storage.getString(STORAGE_PUBLIC_KEY)
        val storedPrivate = storage.getString(STORAGE_PRIVATE_KEY)

        if (!storedPublic.isNullOrBlank() && !storedPrivate.isNullOrBlank()) {
            try {
                val publicKey = ETE.decodePublicKey(storedPublic)
                val privateKey = ETE.decodePrivateKey(storedPrivate)
                ensureRemotePublicKey(user.uid, storedPublic)
                return KeyMaterial(publicKey, privateKey, storedPublic)
            } catch (error: Exception) {
                Log.w(TAG, "Existing key pair invalid; generating a new one", error)
            }
        }

        return try {
            val (publicKey, privateKey) = ETE.generateKeyPair()
            val encodedPublic = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
            val encodedPrivate = Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP)

            storage[STORAGE_PUBLIC_KEY] = encodedPublic
            storage[STORAGE_PRIVATE_KEY] = encodedPrivate

            ensureRemotePublicKey(user.uid, encodedPublic)

            KeyMaterial(publicKey, privateKey, encodedPublic)
        } catch (error: Exception) {
            onError(error)
            null
        }
    }

    private fun ensureRemotePublicKey(uid: String?, encodedPublicKey: String) {
        if (uid.isNullOrBlank()) {
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("publicKey")
            .setValue(encodedPublicKey)
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to publish public key", error)
            }
    }

    private fun generateSecret(): String {
        val secretBytes = ByteArray(32)
        secureRandom.nextBytes(secretBytes)
        return Base64.encodeToString(secretBytes, Base64.NO_WRAP)
    }

}
