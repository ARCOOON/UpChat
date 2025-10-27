package com.devusercode.upchat.security

import com.devusercode.upchat.Key
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.MessageTypes

object MessageIntegrity {
    fun canonicalize(values: Map<String, String?>): String {
        val sortedEntries = values.entries
            .filter { it.value != null }
            .sortedBy { it.key }

        val builder = StringBuilder()
        for ((key, value) in sortedEntries) {
            builder.append(key.length)
                .append(':')
                .append(key)
                .append('=')
                .append(value!!.length)
                .append(':')
                .append(value)
                .append(';')
        }

        return builder.toString()
    }

    fun canonicalize(message: Message): String {
        val values = linkedMapOf<String, String?>()

        message.message?.let { values[Key.Message.MESSAGE] = it }
        message.messageId?.let { values[Key.Message.ID] = it }

        normalizeType(message.type)?.let { values[Key.Message.TYPE] = it }

        message.url?.let { values[Key.Message.URL] = it }
        message.checksum?.let { values[Key.Message.CHECKSUM] = it }
        message.senderId?.let { values[Key.Message.SENDER_ID] = it }
        message.timestamp?.let { values[Key.Message.TIMESTAMP] = it }

        return canonicalize(values)
    }

    private fun normalizeType(type: Any?): String? {
        return when (type) {
            is MessageTypes -> type.name
            is String -> type.uppercase()
            else -> null
        }
    }
}
