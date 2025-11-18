package com.devusercode.upchat.models

enum class MessageStatus {
    SENT,
    READ,
    UNKNOWN,
    ;

    companion object {
        private val typeMap = entries.associateBy { it.toString().lowercase() }

        fun parse(data: Any?): MessageStatus =
            when (data) {
                is String -> MessageStatus.typeMap[data.lowercase()] ?: MessageStatus.UNKNOWN
                else -> MessageStatus.UNKNOWN
            }
    }
}

enum class MessageTypes {
    TEXT,
    AUDIO,
    IMAGE,
    FILE,
    UNKNOWN,
    ;

    companion object {
        private val typeMap = entries.associateBy { it.toString().lowercase() }

        fun parse(data: Any?): MessageTypes =
            when (data) {
                is String -> typeMap[data.lowercase()] ?: UNKNOWN
                else -> UNKNOWN
            }
    }
}

enum class Placeholder {
    NULL,
}
