package com.devusercode.upchat

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.UserUtils

class UserProfileActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<Button>(R.id.back_button)

        setSupportActionBar(toolbar)

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (intent != null && intent.extras != null) {
            val userid = intent.getStringExtra("uid")!!

            UserUtils.getUserByUid(userid) { result ->
                if (result.code == ErrorCodes.SUCCESS) {
                    val user = result.user!!

                    val profileImage = findViewById<ImageView>(R.id.profile_image)
                    val usernameView = findViewById<TextView>(R.id.username)
                    val emailView = findViewById<TextView>(R.id.email)
                    val userIdView = findViewById<TextView>(R.id.uid)
                    val joinedView = findViewById<TextView>(R.id.joined)

                    if (user.photoUrl!!.isNotEmpty()) {
                        Glide.with(this).load(Uri.parse(user.photoUrl))
                            .placeholder(R.drawable.ic_account_circle_black).circleCrop()
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.ic_account_circle_black)
                    }

                    usernameView.text = user.username
                    emailView.text = "Email: ${user.email}"
                    userIdView.text = "Uid: ${user.uid}"
                    joinedView.text = "Joined: ${user.formattedJoined}"
                }
            }
        }
    }
}