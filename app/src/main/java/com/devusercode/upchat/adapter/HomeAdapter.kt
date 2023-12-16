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
import com.devusercode.upchat.ConversationActivity
import com.devusercode.upchat.R
import com.devusercode.upchat.models.UserPair
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.utils.ConversationUtil
import com.google.android.material.card.MaterialCardView
import java.lang.RuntimeException

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
        var username: TextView
        var last_message_text: TextView
        var last_message_time: TextView
        var materialcardview1: MaterialCardView
        var profile_image: ImageView
        var online_status: ImageView

        init {
            materialcardview1 = view.findViewById(R.id.materialcardview1)
            profile_image = view.findViewById(R.id.profile_image)
            online_status = view.findViewById(R.id.online_status)
            username = view.findViewById(R.id.username)
            last_message_text = view.findViewById(R.id.last_message_text)
            last_message_time = view.findViewById(R.id.last_message_time)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(pair: UserPair) {
            val user = pair.user
            val cid = pair.conversationId

            username.text = user.username

            if (user.online.toBoolean()) {
                online_status.setImageResource(R.drawable.ic_status_online)
            } else {
                online_status.setImageResource(R.drawable.ic_status_offline)
            }

            if (user.photoUrl!!.isNotEmpty()) {
                Glide.with(profile_image.context).load(Uri.parse(user.photoUrl))
                    .placeholder(R.drawable.ic_account_circle_black).circleCrop()
                    .into(profile_image)
            } else {
                profile_image.setImageResource(R.drawable.ic_account_circle_black)
            }

            ConversationUtil.getLastMessage(cid) { last_msg ->
                if (last_msg == null) {
                    last_message_text.visibility = View.GONE
                    last_message_time.visibility = View.GONE

                } else if (last_msg.senderId.equals("system")) {
                    last_message_text.visibility = View.GONE
                    last_message_time.visibility = View.GONE

                } else {
                    val aes = AES(user.uid!!)
                    last_message_text.text = aes.decrypt(last_msg.message!!)
                    last_message_time.text = last_msg.parsedTime
                }
            }

            materialcardview1.setOnClickListener { view: View ->
                val intent = Intent(view.context, ConversationActivity::class.java)
                intent.putExtra("uid", user.uid)
                view.context.startActivity(intent)
            }
        }
    }
}