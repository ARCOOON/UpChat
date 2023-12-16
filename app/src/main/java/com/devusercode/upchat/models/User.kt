package com.devusercode.upchat.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class User() {
    var deviceId: String? = null
    var deviceToken: String? = null
    var email: String? = null
    var joined: String? = null
    var online: String? = null
    var username: String? = null
    var uid: String? = null
    var photoUrl: String? = null
    var conversations: Map<String, String>? = null

    fun getConversationIds(): List<String> {
        val conversationIds: MutableList<String> = ArrayList()

        for ((_, value) in conversations!!) {
            conversationIds.add(value)
        }

        return conversationIds
    }

    fun getUids(): List<String> {
        val uids: MutableList<String> = ArrayList()

        for ((uid, _) in conversations!!) {
            uids.add(uid)
        }

        return uids
    }

    val formattedJoined: String
        get() {
            val date = Date(this.joined!!.toLong())
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        }

    val info: HashMap<String, Any?>
        get() {
            val _userinfo = HashMap<String, Any?>()

            _userinfo["deviceId"] = deviceId
            _userinfo["deviceToken"] = deviceToken
            _userinfo["username"] = username
            _userinfo["email"] = email
            _userinfo["uid"] = uid
            _userinfo["photoUrl"] = photoUrl
            _userinfo["joined"] = joined
            _userinfo["online"] = online
            _userinfo["conversations"] = conversations

            return _userinfo
        }
}
