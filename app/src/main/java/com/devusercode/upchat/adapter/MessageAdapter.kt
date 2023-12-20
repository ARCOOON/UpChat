package com.devusercode.upchat.adapter

import android.annotation.SuppressLint
import android.content.Context
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
import com.devusercode.upchat.models.User
import com.devusercode.upchat.utils.Util
import com.devusercode.upchat.utils.Util.setCornerRadius
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    private object MessageType {
        const val TEXT = "text"
        const val IMAGE = "image"
        const val AUDIO = "audio"
        const val FILE = "file"
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
            firebase_user!!.uid -> {
                if (message.type == MessageType.IMAGE) {
                    MessageSender.MESSAGE_SENT_IMAGE
                } else {
                    MessageSender.MESSAGE_SENT_TEXT
                }
            }

            "system" -> {
                MessageSender.SYSTEM_MESSAGE
            }

            participant!!.uid -> {
                MessageSender.MESSAGE_RECEIVED_TEXT
            }

            else -> {
                throw IllegalStateException("Unknown view type")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val model = snapshots[viewType]

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
        private val conversationId: String? = null

        @SuppressLint("InflateParams")
        fun showTooltipOverlay(anchorView: View, model: Message) {
            // Inflate the tooltip overlay layout
            val tooltipView = LayoutInflater.from(anchorView.context)
                .inflate(R.layout.item_conversation_popup, null)
            val rootLayout = tooltipView.findViewById<LinearLayout>(R.id.root_layout)

            setCornerRadius(rootLayout, 50f)

            // Create a PopupWindow to display the tooltip overlay
            val popupWindow = PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // Set the location of the popup window relative to the anchor view
            // popupWindow.showAsDropDown(anchorView)
            // popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
            popupWindow.elevation = 10f

            // Handle button clicks inside the tooltip overlay
            val deleteButton = tooltipView.findViewById<Button>(R.id.delete_button)
            val replyButton = tooltipView.findViewById<Button>(R.id.reply_button)

            deleteButton.setOnClickListener {
                val messageId = model.messageId
                val messageRef = FirebaseDatabase.getInstance().reference.child("conversations")
                    .child(conversationId!!).child("messages").child(messageId!!)

                messageRef.removeValue().addOnCompleteListener { popupWindow.dismiss() }
                    .addOnFailureListener { error: Exception ->
                        Log.e(TAG, error.message!!)
                        popupWindow.dismiss()
                    }
            }

            replyButton.setOnClickListener {
                Util.showMessage(anchorView.context, "Not implemented yet.")
                popupWindow.dismiss() // Dismiss the tooltip overlay
            }

            popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
            popupWindow.animationStyle = R.style.ConversationPopupWindow
        }
    }
}