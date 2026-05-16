package com.devusercode.upchat.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devusercode.upchat.Key
import com.devusercode.upchat.models.Conversation
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.MessageTypes
import com.devusercode.upchat.models.User
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.security.MessageIntegrity
import com.devusercode.upchat.security.SHA512
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.HashMap
import java.util.function.Consumer

class ConversationUtil(
    private var context: Context,
    private var cid: String,
    private var user: User,
    participant: User,
    sharedSecret: String
) {
    private var mac: MAC = MAC(sharedSecret, cid)
    private var aes: AES = AES(
        sharedSecret,
        cid
    )

    companion object {
        private const val TAG = "ConversationUtil"
        // private val firebaseDatabase = FirebaseDatabase.getInstance()
        // const val REF = "conversations"

        class ConversationResult {
            private var conversation: Conversation?
            var error: Error?

            constructor(conversation: Conversation?, error: Error?) {
                this.conversation = conversation
                this.error = error
            }

            constructor(conversation: Conversation?) {
                this.conversation = conversation
                error = null
            }

            fun getConversation(): Conversation? = conversation

            val isSuccessful: Boolean
                get() = error == null && conversation != null
        }

        fun getConversationId(
            user: User,
            conversations: Map<String, String?>?,
        ): String? {
            if (conversations != null) {
                for ((key, value) in conversations) {
                    if (key == user.uid) {
                        return value
                    }
                }
            }
            // Return null if no matching user ID found
            return null
        }

        fun conversationExists(
            user: User,
            participant: User,
        ): Boolean =
            if (user.conversations != null && participant.conversations != null) {
                val chatExists = participant.conversations!!.containsKey(user.uid)
                val chatExists2 = user.conversations!!.containsKey(participant.uid)

                chatExists && chatExists2
            } else {
                if (user.conversations == null) {
                    Log.d(TAG, "No conversation found for user")
                } else {
                    Log.d(TAG, "No conversation found for participant")
                }
                false
            }
        }

        fun conversationExistsIn(uid: String?, conversations: Map<String?, String?>): Boolean {
            return conversations.containsKey(uid)
        }

        fun newConversation(user: User, participant: User): Task<String> {
            val rootRef = FirebaseDatabase.getInstance().reference
            val conversationId = rootRef.child(Key.Conversation.CONVERSATIONS).push().key
                ?.removePrefix("-")

            if (conversationId.isNullOrBlank()) {
                return Tasks.forException(IllegalStateException("Failed to generate conversation id"))
            }

            val updates = hashMapOf<String, Any?>(
                "${Key.Conversation.CONVERSATIONS}/$conversationId/${Key.Conversation.MEMBERS}/0" to user.uid,
                "${Key.Conversation.CONVERSATIONS}/$conversationId/${Key.Conversation.MEMBERS}/1" to participant.uid,
                "users/${user.uid}/${Key.User.CONVERSATIONS}/${participant.uid}" to conversationId,
                "users/${participant.uid}/${Key.User.CONVERSATIONS}/${user.uid}" to conversationId
            )

            val completion = TaskCompletionSource<String>()

            rootRef.updateChildren(updates)
                .addOnSuccessListener { completion.setResult(conversationId) }
                .addOnFailureListener { error ->
                    completion.setException(error ?: Exception("Unknown error creating conversation"))
                }

            return completion.task
        }

        fun getConversationById(
            cid: String,
            onFinish: Consumer<ConversationResult?>,
        ) {
            val conversationRef =
                FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("conversations")
                    .child(cid)

            conversationRef.get().addOnCompleteListener { task: Task<DataSnapshot?> ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result

                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        val conversation: Conversation? =
                            dataSnapshot.getValue(Conversation::class.java)

                        if (conversation != null) {
                            onFinish.accept(ConversationResult(conversation))
                        } else {
                            val error = Error("Unknown error while retrieving the conversation!")
                            onFinish.accept(ConversationResult(null, error))
                        }
                    } else {
                        val error = Error("ConversationNotFound ($cid)")
                        onFinish.accept(ConversationResult(null, error))
                    }
                } else {
                    val error = Error(task.exception!!.message)
                    onFinish.accept(ConversationResult(null, error))
                }
            }
        }

        fun getLastMessage(
            conversationId: String?,
            onFinish: Consumer<Message?>,
        ) {
            val messagesRef =
                FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("conversations")
                    .child(conversationId!!)
                    .child("messages")

            val lastMessageQuery = messagesRef.orderByChild("timestamp").limitToLast(1)

            lastMessageQuery.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val lastMessageSnapshot = dataSnapshot.children.iterator().next()
                            val lastMessage: Message? =
                                lastMessageSnapshot.getValue(Message::class.java)

                            onFinish.accept(lastMessage)
                        } else {
                            onFinish.accept(null)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(TAG, databaseError.message)
                    }
                },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(msg: String): Task<Void> {
        val data: MutableMap<String, String?> = HashMap()

        val messagesRef =
            FirebaseDatabase.getInstance().reference.child(Key.Conversation.CONVERSATIONS)
                .child(cid).child(Key.Conversation.MESSAGES)

        val messageId = messagesRef.push().key?.removePrefix("-")
            ?: return Tasks.forException(IllegalStateException("Failed to generate message id"))

        val message = msg.trim { it <= ' ' }

        // Save the message data
        data[Key.Message.MESSAGE] = aes.encrypt(message)
        data[Key.Message.ID] = messageId
        data[Key.Message.TYPE] = MessageTypes.TEXT.toString()
        data[Key.Message.SENDER_ID] = user.uid
        data[Key.Message.TIMESTAMP] = System.currentTimeMillis().toString()
        user.username?.let { data[Key.User.USERNAME] = it }
        user.deviceId?.let { data[Key.User.DEVICE_ID] = it }

        val payloadValues = HashMap(data)
        payloadValues[Key.Conversation.CONVERSATION_ID] = cid

        val payload = MessageIntegrity.canonicalize(payloadValues)
        data[Key.Message.MAC] = mac.generate(payload)

        return messagesRef.child(messageId).setValue(data)
    }

    fun sendFile(
        file: Uri,
        mime: String,
        extension: String?,
        msg: String?,
    ): String? {
        val data: MutableMap<String, String?> = HashMap()

        val storageRef =
            FirebaseStorage
                .getInstance()
                .reference
                .child(Key.Document.DOCUMENTS)
                .child(cid)

        val messagesRef =
            FirebaseDatabase
                .getInstance()
                .reference
                .child(Key.Conversation.CONVERSATIONS)
                .child(cid)
                .child(Key.Conversation.MESSAGES)

        var messageId = messagesRef.push().key!!
        if (messageId.startsWith("-")) {
            messageId = messageId.substring(1)
        }

        storageRef.child("$messageId.$extension").putFile(file)

        data[Key.Message.MESSAGE] = msg ?: ""
        data[Key.Message.ID] = messageId
        data[Key.Message.TYPE] = MessageTypes.FILE.toString()
        data[Key.Message.MIME] = mime
        data[Key.Message.URL] = ""
        data[Key.Message.CHECKSUM] = SHA512.generate(file)
        data[Key.Message.SENDER_ID] = user.uid
        data[Key.Message.TIMESTAMP] = System.currentTimeMillis().toString()

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendImage(
        file: Uri,
        msg: String?,
    ) {
        val messagesRef =
            FirebaseDatabase
                .getInstance()
                .reference
                .child(Key.Conversation.CONVERSATIONS)
                .child(cid)
                .child(Key.Conversation.MESSAGES)

        val messageId = messagesRef.push().key?.removePrefix("-") ?: return

        val imageRef =
            FirebaseStorage
                .getInstance()
                .reference
                .child(Key.Document.DOCUMENTS)
                .child(cid)
                .child("$messageId.png")

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(file)

        inputStream?.use { input ->
            var checksum = "null"
            val tempFile = File.createTempFile("temp_image", ".png") // .apply { deleteOnExit() }

            Log.d(TAG, "260 - Temp file path -> ${tempFile.absoluteFile}")
            Log.d(TAG, "261 - Temp file exists -> ${tempFile.exists()}")

            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }

            val fileUri = Uri.fromFile(tempFile)

            Log.d(TAG, "269 - Temp file path -> ${tempFile.absoluteFile}")
            Log.d(TAG, "270 - Temp file exists -> ${tempFile.exists()}")

            imageRef
                .putFile(fileUri)
                .addOnSuccessListener { _ ->
                    imageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.d(TAG, "276 - Temp file path -> ${tempFile.absoluteFile}")
                            Log.d(TAG, "277 - Temp file exists -> ${tempFile.exists()}")

                            if (tempFile.exists()) {
                                checksum = SHA512.generate(uri)
                            }
                            val message = msg?.trim() ?: ""

                            val data = mutableMapOf(
                                Key.Message.MESSAGE to message,
                                Key.Message.ID to messageId,
                                Key.Message.TYPE to MessageTypes.IMAGE.toString(),
                                Key.Message.URL to uri.toString(),
                                Key.Message.CHECKSUM to checksum,
                                Key.Message.SENDER_ID to user.uid,
                                Key.Message.TIMESTAMP to System.currentTimeMillis().toString()
                            )

                            user.username?.let { data[Key.User.USERNAME] = it }
                            user.deviceId?.let { data[Key.User.DEVICE_ID] = it }

                            val payloadValues = HashMap(data)
                            payloadValues[Key.Conversation.CONVERSATION_ID] = cid

                            val payload = MessageIntegrity.canonicalize(payloadValues)
                            data[Key.Message.MAC] = mac.generate(payload)

                            Log.d(TAG, "Data: $data")

                            messagesRef.child(messageId).setValue(data)
                        }.addOnFailureListener { error ->
                            Log.e(TAG, error.message!!)
                        }
                }.addOnFailureListener { error ->
                    Log.e(TAG, error.message!!)
                }
            tempFile.delete()
        } ?: Log.e(TAG, "Failed to open input stream for the selected file")
    }
}
