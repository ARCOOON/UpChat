package com.devusercode.upchat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.adapter.HomeAdapter
import com.devusercode.upchat.models.User
import com.devusercode.upchat.models.UserPair
import com.devusercode.upchat.utils.ConversationUtil
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.UserUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
class HomeActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val auth = FirebaseAuth.getInstance()

    private var user: User? = null
    private lateinit var recyclerview: RecyclerView
    private var adapter: HomeAdapter? = null
    private var conversationsCounter = 0

    private var openConversations: MutableList<UserPair>? = null
    private val userListeners: MutableMap<String, ValueEventListener> = HashMap()
    private lateinit var storageController: StorageController

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_item_logout -> {
                auth.signOut()

                storageController["save_login_info"] = false
                storageController.remove("email")
                storageController.remove("password")

                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        storageController = StorageController.getInstance(this)!!

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toolbarProfileName = toolbar.findViewById<TextView>(R.id.toolbar_profile_name)
        toolbarProfileName.text = getString(R.string.app_name)

        setSupportActionBar(toolbar)

        val newConversationButton: FloatingActionButton = findViewById(R.id.new_conversation_button)

        newConversationButton.setOnClickListener {
            val intent = Intent(this, ListUsersActivity::class.java)
            startActivity(intent)
        }

        recyclerview = findViewById(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)

        if (storageController.contains("user")) {
            user = storageController.getUser("user")
            Log.d(TAG, "User from Storage" + user?.info.toString())

            loadOpenConversations()
        } else {
            UserUtils.getUserByUid(auth.currentUser!!.uid) { result ->
                if (result.code == ErrorCodes.SUCCESS) {
                    user = result.user!!
                    loadOpenConversations()
                } else {
                    Log.e(TAG, result.error?.message!!)
                    Toast.makeText(this, "Error: ${result.code}", Toast.LENGTH_SHORT).show()

                    if (result.code == ErrorCodes.USER_NOT_FOUND) {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun loadOpenConversations() {
        if (user == null || user!!.conversations == null) {
            return
        }

        openConversations = ArrayList()
        conversationsCounter = 0 // Reset the conversations counter

        for (cid in user!!.getConversationIds()) {
            ConversationUtil.getConversationById(cid) { task ->
                if (!task!!.isSuccessful) {
                    Log.e(TAG, task.error?.message!!)
                    // Increment the conversationsCounter even for invalid conversations
                    // so that we can still check when all iterations are completed.
                    conversationsCounter++

                    // Check if we have processed all conversations
                    if (conversationsCounter == user!!.getConversationIds().size) {
                        // Create the adapter with the valid conversations
                        adapter = HomeAdapter(openConversations as ArrayList<UserPair>)
                        recyclerview.adapter = adapter
                    }
                    return@getConversationById
                }

                task.getConversation()?.getParticipant { result ->
                    if (!result.isSuccessful) {
                        Log.e(TAG, result.error?.message!!)
                    } else {
                        (openConversations as ArrayList<UserPair>).add(
                            UserPair(
                                result.user!!,
                                cid
                            )
                        )
                    }

                    // Increment the conversationsCounter after each iteration
                    conversationsCounter++

                    // Check if we have processed all conversations
                    if (conversationsCounter == user!!.getConversationIds().size) {
                        // Create the adapter with the valid conversations
                        adapter = HomeAdapter(openConversations as ArrayList<UserPair>)
                        recyclerview.adapter = adapter

                        for (userPair in openConversations ?: emptyList()) {
                            Log.d(TAG, "setting OnlineStatusListener for: ${userPair.user.uid}")
                            setupUserOnlineStatusListener(userPair.user.uid!!)
                        }
                    }
                }
            }
        }
    }

    private fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        openConversations?.forEach { userPair ->
            if (userPair.user.uid == userId) {
                userPair.user.online = isOnline.toString()
            }
        }
    }

    private fun setupUserOnlineStatusListener(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val listener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("online").exists()) {
                    val isOnline = dataSnapshot.child("online").getValue(String::class.java).toBoolean()
                    updateOnlineStatus(userId, isOnline)
                    adapter?.update(openConversations as ArrayList<UserPair>)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "setupUserOnlineStatusListener -> onCancelled: ${databaseError.message}")
                // Handle the cancellation if needed
            }
        }

        userRef.addValueEventListener(listener)
        userListeners[userId] = listener
    }

    override fun onStop() {
        super.onStop()

        for (pair in openConversations ?: emptyList()) {
            val uid = pair.user.uid!!
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

            Log.d(TAG, "removing OnlineStatusListener for: $uid")
            userRef.removeEventListener(userListeners[uid]!!)
        }
    }

    override fun onStart() {
        super.onStart()

        for (userPair in openConversations ?: emptyList()) {
            Log.d(TAG, "creating OnlineStatusListener for: ${userPair.user.uid}")
            setupUserOnlineStatusListener(userPair.user.uid!!)
        }
    }
}