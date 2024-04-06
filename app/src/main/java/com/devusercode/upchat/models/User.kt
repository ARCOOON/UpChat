package com.devusercode.upchat.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    var deviceId: String? = null,
    var deviceToken: String? = null,
    var email: String? = null,
    var joined: String? = null,
    var online: String? = null,
    var username: String? = null,
    var uid: String? = null,
    var photoUrl: String? = null,
    var conversations: Map<String, String>? = null
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
            "joined" to joined,
            "online" to online,
            "conversations" to conversations
        )
}
