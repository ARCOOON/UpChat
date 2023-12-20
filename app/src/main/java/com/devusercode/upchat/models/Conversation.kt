package com.devusercode.upchat.models

import com.devusercode.upchat.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import java.util.function.Consumer

class Conversation {
    companion object {
        class Result {
            private var user: User?
            private var participant: User?
            var error: Error?

            constructor() {
                user = null
                participant = null
                error = null
            }

            constructor(user: User?, participant: User?, error: Error?) {
                this.user = user
                this.participant = participant
                this.error = error
            }

            constructor(user: User?, participant: User?) {
                this.user = user
                this.participant = participant
                error = null
            }

            val isSuccessful: Boolean
                get() = error == null && (user != null) or (participant != null)

            override fun toString(): String {
                val user: HashMap<String, Any?>? = user?.info
                val participant: HashMap<String, Any?>? = participant?.info
                val error = if (error != null) error!!.message else null

                return "Result($user, $participant, $error)"
            }
        }
    }

    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val members: MutableList<String>? = null
    private val messages: Map<String, Message>? = null

    fun getMembers(): List<String>? {
        return members
    }

    fun getMessages(): Map<String, Message>? {
        return messages
    }

    fun getMessageById(mid: String): Message? {
        return messages!![mid]
    }

    val participantUid: String
        get() {
            members!!.remove(uid)
            return members[0]
        }

    fun getParticipant(onFinish: Consumer<UserUtils.Companion.Result>) {
        members!!.remove(uid)
        UserUtils.getUserByUid(members[0]) { result ->
            if (result.isSuccessful) {
                onFinish.accept(result)
            } else {
                onFinish.accept(result)
            }
        }
    }
}