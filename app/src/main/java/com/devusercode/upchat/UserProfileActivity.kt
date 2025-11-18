package com.devusercode.upchat

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.UserUtils

class UserProfileActivity : AppCompatActivity() {
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
                if (result.code == ErrorCodes.SUCCESS && result.user != null) {
                    val user = result.user!!

                    val profileImage = findViewById<ImageView>(R.id.profile_image)
                    val usernameView = findViewById<TextView>(R.id.username)
                    val emailView = findViewById<TextView>(R.id.email)
                    val userIdView = findViewById<TextView>(R.id.uid)
                    val joinedView = findViewById<TextView>(R.id.joined)

                    if (user.photoUrl!!.isNotEmpty()) {
                        Glide
                            .with(this)
                            .load(user.photoUrl!!.toUri())
                            .placeholder(R.drawable.ic_account_circle_black)
                            .circleCrop()
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.ic_account_circle_black)
                    }

                    usernameView.text = user.username
                    emailView.text = getString(R.string.user_profile__email_label, user.email)
                    userIdView.text = getString(R.string.user_profile__uid_label, user.uid)
                    joinedView.text = getString(R.string.user_profile__joined_label, user.formattedJoined)
                }
            }
        }
    }
}
