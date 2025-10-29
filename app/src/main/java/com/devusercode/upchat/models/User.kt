package com.devusercode.upchat.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    var deviceId: String = "",
    var deviceToken: String = "",
    var email: String = "",
    var joined: String = "",
    var online: String = "",
    var username: String = "",
    var uid: String = "",
    var photoUrl: String = "",
    var conversations: Map<String, String>? = null,
    var publicKey: String? = null
) {
    fun getConversationIds(): List<String> = conversations?.values?.toList() ?: emptyList()

    fun getUids(): List<String> = conversations?.keys?.toList() ?: emptyList()

    val formattedJoined: String
        get() = joined?.let {
            val date = Date(it.toLong())
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        } ?: ""

    val info: Map<String, Any?>
        get() = mapOf(
            "deviceId" to deviceId,
            "deviceToken" to deviceToken,
            "username" to username,
            "email" to email,
            "uid" to uid,
            "photoUrl" to photoUrl,
            "publicKey" to publicKey,
            "joined" to joined,
            "online" to online,
            "conversations" to conversations
        )
}
