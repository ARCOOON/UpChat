package com.devusercode.upchat.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.security.crypto.MasterKey
import com.devusercode.upchat.models.User
import com.google.gson.Gson
import java.io.IOException
import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.PreferenceDataStoreFactory

class StorageController private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_FILE_NAME) }
    )
    private val gson = Gson()
    private val keyStore: KeyStore
    private val masterKeyAlias: String

    init {
        try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            masterKeyAlias = masterKey.keyAlias
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("Failed to initialize secure storage", e)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to initialize secure storage", e)
        }
    }

    companion object {
        private const val DATA_STORE_FILE_NAME = "secure_storage.preferences_pb"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val LOG_TAG = "StorageController"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128

        @Volatile
        private var instance: StorageController? = null

        fun getInstance(context: Context): StorageController {
            return instance ?: synchronized(this) {
                instance ?: StorageController(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(masterKeyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: throw IllegalStateException("Master key entry missing for alias: $masterKeyAlias")
    }

    private fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_GCM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val payload = ByteBuffer.allocate(Int.SIZE_BYTES + iv.size + cipherBytes.size)
            payload.putInt(iv.size)
            payload.put(iv)
            payload.put(cipherBytes)
            Base64.encodeToString(payload.array(), Base64.NO_WRAP)
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("Failed to encrypt value", e)
        }
    }

    private fun decrypt(encrypted: String): String {
        return try {
            val payload = ByteBuffer.wrap(Base64.decode(encrypted, Base64.NO_WRAP))
            val ivLength = payload.int
            val iv = ByteArray(ivLength)
            payload.get(iv)
            val cipherBytes = ByteArray(payload.remaining())
            payload.get(cipherBytes)
            val cipher = Cipher.getInstance(AES_GCM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            Log.e(LOG_TAG, "Failed to decrypt value", e)
            throw IllegalStateException("Failed to decrypt value", e)
        }
    }

    private suspend fun writeRaw(key: String, rawValue: String?) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            if (rawValue == null) {
                preferences.remove(prefKey)
            } else {
                preferences[prefKey] = encrypt(rawValue)
            }
        }
    }

    private suspend fun readRaw(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        val encrypted = preferences[prefKey] ?: return null
        return runCatching { decrypt(encrypted) }
            .onFailure { Log.e(LOG_TAG, "Removing corrupt entry for $key", it) }
            .getOrElse {
                dataStore.edit { it.remove(prefKey) }
                null
            }
    }

    operator fun set(key: String, data: Any?) {
        runBlocking(Dispatchers.IO) {
            when (data) {
                null -> writeRaw(key, null)
                is String -> writeRaw(key, data)
                is Boolean -> writeRaw(key, data.toString())
                is Int -> writeRaw(key, data.toString())
                is Long -> writeRaw(key, data.toString())
                is Float -> writeRaw(key, data.toString())
                is User -> writeRaw(key, gson.toJson(data))
                else -> throw IllegalArgumentException("Unsupported data type: ${data::class.java.name}")
            }
        }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return runBlocking(Dispatchers.IO) { readRaw(key) } ?: defaultValue
    }

    fun getBool(key: String, defaultValue: Boolean = false): Boolean {
        val raw = runBlocking(Dispatchers.IO) { readRaw(key) } ?: return defaultValue
        return when (raw.lowercase()) {
            "true" -> true
            "false" -> false
            else -> defaultValue
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        val raw = runBlocking(Dispatchers.IO) { readRaw(key) } ?: return defaultValue
        return raw.toIntOrNull() ?: defaultValue
    }

    fun getUser(key: String): User? {
        val userJson = runBlocking(Dispatchers.IO) { readRaw(key) } ?: return null
        return runCatching { gson.fromJson(userJson, User::class.java) }
            .onFailure { Log.e(LOG_TAG, "Failed to parse user for key $key", it) }
            .getOrNull()
    }

    operator fun contains(key: String): Boolean {
        val prefKey = stringPreferencesKey(key)
        return runBlocking(Dispatchers.IO) {
            dataStore.data.first()[prefKey] != null
        }
    }

    fun remove(key: String) {
        runBlocking(Dispatchers.IO) { writeRaw(key, null) }
    }

    fun clear() {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }
}
