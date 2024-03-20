package com.devusercode.upchat

object Key {
    object Document {
        const val DOCUMENTS = "documents"
    }

    object Conversation {
        // For direct chat
        const val CONVERSATIONS = "conversations"
        const val CONVERSATION_ID = "conversationId"
        const val MESSAGES = "messages"
        // For group chat
        const val MEMBERS = "members"
    }

    object Message {
        const val ID = "messageId"
        const val MESSAGE = "message"
        const val SENDER_ID = "senderId"
        const val TIMESTAMP = "timestamp"
        const val MAC = "mac"
        const val SEEN = "seen"
        // Message type: File, Text, Image, Audio
        const val TYPE = "type"
        const val MIME = "mimetype"
        // For file transfer
        const val URL = "url"
        const val CHECKSUM = "checksum"
    }

    object User {
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val DEVICE_ID = "deviceId"
        const val DEVICE_TOKEN = "deviceToken"
        const val UID = "uid"
        const val PHOTO_URL = "photoUrl"
        const val CONVERSATIONS = "conversations"
        const val JOINED = "joined"
        const val ONLINE = "online"
    }
}
