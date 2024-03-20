package com.devusercode.upchat.models


enum class MessageTypes {
    TEXT, AUDIO, IMAGE, FILE, UNKNOWN;

    override fun toString(): String {
        return super.toString().lowercase()
    }

    companion object {
        fun parse(data: Any?): MessageTypes {
            if (data == null)
                return UNKNOWN

            return if (data is String) {
                when (data.lowercase()) {
                    "text" -> TEXT
                    "audio" -> AUDIO
                    "image" -> IMAGE
                    "file" -> FILE
                    else -> UNKNOWN
                }
            } else {
                UNKNOWN
            }
        }
    }
}

enum class Placeholder {
    NULL
}