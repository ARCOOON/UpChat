package com.devusercode.upchat.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.devusercode.upchat.models.User
import com.google.gson.Gson
import java.io.IOException
import java.security.GeneralSecurityException

class StorageController private constructor(context: Context) {
    private var preferences: SharedPreferences? = null

    companion object {
        private const val TAG = "StorageController"
        private const val SHARED_PREFS_NAME = "upchat.storage.shared.prefs"
        private var instance: StorageController? = null

        @Synchronized
        fun getInstance(context: Context): StorageController? {
            if (instance == null) {
                instance = StorageController(context.applicationContext)
            }
            return instance
        }
    }

    init {
        preferences = try {
            val masterKey =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

            EncryptedSharedPreferences.create(
                context,
                SHARED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Error initializing EncryptedSharedPreferences", e)
        } catch (e: IOException) {
            throw RuntimeException("Error initializing EncryptedSharedPreferences", e)
        }
    }

    operator fun set(key: String?, data: Any) {
        try {
            val encryptedData = encrypt(data)
            preferences!!.edit().putString(key, encryptedData).apply()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
    }

    fun getString(key: String?): String? {
        val encryptedData = preferences!!.getString(key, null)

        if (encryptedData != null) {
            try {
                return decrypt(encryptedData)
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getBool(key: String?): Boolean {
        return preferences!!.getString(key, null).toBoolean()
    }

    fun getInt(key: String?): Int {
        val value = preferences!!.getString(key, null)
        return value?.toInt() ?: 0
    }

    fun getUser(key: String?): User? {
        val userJson = preferences!!.getString(key, null)

        return if (userJson != null) {
            Gson().fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    operator fun contains(key: String?): Boolean {
        return preferences!!.contains(key)
    }

    fun remove(key: String?) {
        preferences!!.edit().remove(key).apply()
    }

    fun clear() {
        val editor = preferences!!.edit()
        editor.clear()
        editor.apply()
    }

    @Throws(GeneralSecurityException::class)
    private fun encrypt(data: Any): String {
        return when (data) {
            is String -> Base64.encodeToString(data.toByteArray(), Base64.DEFAULT)
            is Boolean -> data.toString()
            is Int -> data.toString()
            is User -> Gson().toJson(data)
            else -> throw IllegalArgumentException("Unsupported data type: ${data::class.java.name}")
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun decrypt(encryptedData: String): String {
        val data = Base64.decode(encryptedData, Base64.DEFAULT)
        return String(data)
    }
}