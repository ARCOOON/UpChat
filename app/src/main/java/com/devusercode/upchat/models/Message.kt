package com.devusercode.upchat.models

import com.devusercode.upchat.utils.GetTimeAgo.parse

class Message {
    var senderId: String? = null
    var message: String? = null
    var timestamp: String? = null
    var type: Any? = null
    var messageId: String? = null
    var mac: String? = null
    var url: String? = null
    var checksum: String? = null
    var seen: MessageStatus = MessageStatus.UNKNOWN
    var replyId: String = ""

    constructor()

    constructor(senderId: String?, message: String?, timestamp: String?, type: Any?) {
        this.senderId = senderId
        this.message = message
        this.timestamp = timestamp

        if (type is String) {
            this.type = MessageTypes.parse(type)
        } else {
            this.type = type
        }
    }

    val parsedTime: String?
        get() = parse(timestamp!!)
}