package com.devusercode.upchat.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.R
import com.devusercode.upchat.adapter.viewholder.ReceivedMessageViewHolder
import com.devusercode.upchat.adapter.viewholder.SentImageViewHolder
import com.devusercode.upchat.adapter.viewholder.SentMessageViewHolder
import com.devusercode.upchat.adapter.viewholder.SystemMessageViewHolder
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.MessageTypes
import com.devusercode.upchat.models.User
import com.devusercode.upchat.utils.Util
import com.devusercode.upchat.utils.Util.setCornerRadius
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt

@RequiresApi(Build.VERSION_CODES.O)
class MessageAdapter(
    private val currentContext: Context, options: FirebaseRecyclerOptions<Message?>
) : FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    private var participant: User? = null
    private var conversationId: String? = null

    private object MessageSender {
        const val MESSAGE_SENT_TEXT = 0
        const val MESSAGE_SENT_IMAGE = 1
        const val MESSAGE_SENT_AUDIO = 2
        const val MESSAGE_SENT_FILE = 3

        const val MESSAGE_RECEIVED_TEXT = 4
        const val MESSAGE_RECEIVED_IMAGE = 5
        const val MESSAGE_RECEIVED_AUDIO = 6
        const val MESSAGE_RECEIVED_FILE = 7

        const val SYSTEM_MESSAGE = 8
    }

    fun setConversationId(cid: String) {
        conversationId = cid
    }

    fun setParticipant(user: User?) {
        participant = user
    }

    override fun getItemViewType(position: Int): Int {
        val message = snapshots[position]

        return when (message.senderId) {
            "system" -> MessageSender.SYSTEM_MESSAGE

            firebase_user!!.uid -> {
                if (MessageTypes.parse(message.type) == MessageTypes.IMAGE) {
                    MessageSender.MESSAGE_SENT_IMAGE
                } else {
                    MessageSender.MESSAGE_SENT_TEXT
                }
            }

            participant!!.uid -> MessageSender.MESSAGE_RECEIVED_TEXT

            else -> {
                Log.e(TAG, "Unknown view type")
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        // val model = snapshots[viewType]

        return when (viewType) {
            MessageSender.MESSAGE_SENT_TEXT -> {
                val inflater = LayoutInflater.from(parent.context)
                view = inflater.inflate(R.layout.item_conversation_sent_text, parent, false)
                SentMessageViewHolder(view)
            }

            MessageSender.MESSAGE_SENT_IMAGE -> {
                val inflater = LayoutInflater.from(parent.context)
                view = inflater.inflate(R.layout.item_conversation_sent_image, parent, false)
                SentImageViewHolder(view)
            }

            MessageSender.MESSAGE_RECEIVED_TEXT -> {
                val inflater = LayoutInflater.from(parent.context)
                view = inflater.inflate(R.layout.item_conversation_received_text, parent, false)
                ReceivedMessageViewHolder(view)
            }

            MessageSender.SYSTEM_MESSAGE -> {
                val inflater = LayoutInflater.from(parent.context)
                view = inflater.inflate(R.layout.item_conversation_system, parent, false)
                SystemMessageViewHolder(view)
            }

            else -> {
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {
        when (holder) {
            is SentMessageViewHolder -> {
                holder.bind(model, conversationId!!, participant?.uid!!)
            }

            is SentImageViewHolder -> {
                holder.bind(model, conversationId!!, participant?.uid!!)
            }

            is ReceivedMessageViewHolder -> {
                holder.bind(model, conversationId!!, firebase_user!!.uid)
            }

            is SystemMessageViewHolder -> {
                holder.bind(model)
            }
        }
    }

    companion object {
        private val TAG = MessageAdapter::class.java.simpleName
        private val firebase_user = FirebaseAuth.getInstance().currentUser
        var conversationId: String? = null

        fun showTooltipOverlay(anchorView: View, model: Message) {
            if (model.senderId != firebase_user?.uid) return

            val inflater = LayoutInflater.from(anchorView.context)
            val tooltipView = inflater.inflate(R.layout.item_conversation_popup, null)

            // Create a PopupWindow with WRAP_CONTENT size
            val popupWindow = PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // Darken the background by setting a semi-transparent black color
            val darkBackground = "#50000000".toColorInt().toDrawable()
            popupWindow.setBackgroundDrawable(darkBackground)

            // Set animation style
            popupWindow.animationStyle = R.style.ConversationPopupWindow
            // Show the PopupWindow centered relative to anchorView
            popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)

            // Dismiss the popup when clicked outside of it
            popupWindow.isOutsideTouchable = true
            popupWindow.isFocusable = true
            popupWindow.update()

            val infoButton = tooltipView.findViewById<Button>(R.id.info_button)
            val replyButton = tooltipView.findViewById<Button>(R.id.reply_button)
            val deleteButton = tooltipView.findViewById<Button>(R.id.delete_button)

            infoButton.setOnClickListener {
                val messageInfo = ""
                messageInfo.plus("Id: ${model.messageId}")
                messageInfo.plus("Status: ${model.seen}")
                messageInfo.plus("Type: ${model.type}")
                messageInfo.plus("Reply Id: ${model.replyId}")
                messageInfo.plus("Mac: ${model.mac}")
                messageInfo.plus("Timestamp: ${model.timestamp}")

                MaterialAlertDialogBuilder(anchorView.context)
                    .setTitle("Message Info")
                    .setMessage(messageInfo)
                    .setPositiveButton("Close") { dialog, _ ->
                        dialog.dismiss()
                        popupWindow.dismiss()
                    }
                    .show()
            }

            deleteButton.setOnClickListener {
                conversationId?.let { cid ->
                    model.messageId?.let { messageId ->
                        val messageRef = FirebaseDatabase.getInstance().reference
                            .child("conversations")
                            .child(cid)
                            .child("messages")
                            .child(messageId)

                        messageRef.removeValue()
                            .addOnCompleteListener { result ->
                                if (result.isSuccessful) {
                                    popupWindow.dismiss()
                                } else {
                                    Log.e(TAG, "Delete unsuccessful")
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e(TAG, error.message ?: "Delete failed")
                            }
                    }
                }
            }

            replyButton.setOnClickListener {
                Util.showMessage(anchorView.context, "Not implemented yet.")
                popupWindow.dismiss()
            }
        }

    }
}