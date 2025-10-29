package com.devusercode.upchat.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devusercode.upchat.ConversationActivity
import com.devusercode.upchat.R
import com.devusercode.upchat.models.UserPair
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.utils.ConversationUtil
import com.google.android.material.card.MaterialCardView
import androidx.core.net.toUri

@RequiresApi(Build.VERSION_CODES.O)
class HomeAdapter(private var data: List<UserPair>) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {
    private val TAG = "HomeAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_home_user, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val pair = data[position]
        holder.bind(pair)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: List<UserPair>) {
        data = newData
        notifyDataSetChanged()
    }

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView = view.findViewById(R.id.username)
        private var lastMessageText: TextView = view.findViewById(R.id.last_message_text)
        private var lastMessageTime: TextView = view.findViewById(R.id.last_message_time)
        private var materialCardView: MaterialCardView = view.findViewById(R.id.materialcardview1)
        private var profileImage: ImageView = view.findViewById(R.id.profile_image)
        private var onlineStatus: ImageView = view.findViewById(R.id.online_status)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(pair: UserPair) {
            val user = pair.user
            val cid = pair.conversationId

            username.text = user.username

            if (user.online.toBoolean()) {
                onlineStatus.setImageResource(R.drawable.ic_status_online)
            } else {
                onlineStatus.setImageResource(R.drawable.ic_status_offline)
            }

            if (user.photoUrl!!.isNotEmpty()) {
                Glide.with(profileImage.context)
                    .load(user.photoUrl?.toUri())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.ic_account_circle_black)
                    .circleCrop()
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_account_circle_black)
            }

            ConversationUtil.getLastMessage(cid) { lastMsg ->
                if (lastMsg == null) {
                    lastMessageText.visibility = View.GONE
                    lastMessageTime.visibility = View.GONE
                    return@getLastMessage
                }

                if (lastMsg.senderId.equals("system")) {
                    lastMessageText.visibility = View.GONE
                    lastMessageTime.visibility = View.GONE

                } else if (lastMsg.type == "image") {
                    lastMessageText.text = username.context.getString(R.string.home_last_message_photo)
                    lastMessageTime.text = lastMsg.parsedTime
                }
            }

            materialCardView.setOnClickListener { view: View ->
                val intent = Intent(view.context, ConversationActivity::class.java)
                intent.putExtra("uid", user.uid)
                view.context.startActivity(intent)
            }
        }
    }
}