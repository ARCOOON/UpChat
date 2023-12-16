package com.devusercode.upchat.models

import com.devusercode.upchat.utils.GetTimeAgo.parse

class Message {
    var senderId: String? = null
    var message: String? = null
    var timestamp: String? = null
    var type: String? = null
    var messageId: String? = null
    var mac: String? = null
    var url: String? = null

    constructor()

    constructor(senderId: String?, message: String?, timestamp: String?, type: String?) {
        this.senderId = senderId
        this.message = message
        this.timestamp = timestamp
        this.type = type
    }

    val parsedTime: String?
        get() = parse(timestamp!!)
}