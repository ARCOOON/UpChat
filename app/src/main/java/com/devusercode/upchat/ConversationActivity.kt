package com.devusercode.upchat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.size
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devusercode.upchat.adapter.MessageAdapter
import com.devusercode.upchat.adapter.WrapLayoutManager
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.User
import com.devusercode.upchat.security.ConversationKeyManager
import com.devusercode.upchat.utils.ConversationUtil
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.UserUtils
import com.devusercode.upchat.utils.Util
import com.devusercode.upchat.utils.setComposeContent
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import androidx.core.net.toUri

@RequiresApi(Build.VERSION_CODES.O)
class ConversationActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val conversations = firebaseDatabase.getReference("conversations")
    private val users = firebaseDatabase.getReference("users")

    /* Toolbar */
    private lateinit var backButton: Button
    private lateinit var participantName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var statusOnline: TextView

    /* Content */
    private var adapter: MessageAdapter? = null
    private lateinit var recyclerview: RecyclerView
    private lateinit var attachButton: Button
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: Button

    private var participant: User? = null
    private var user: User? = null

    private lateinit var contentView: View
    private lateinit var storageController: StorageController
    private lateinit var conversationUtil: ConversationUtil
    private var conversationSecret: String? = null

    private var file: Uri? = null
    private var filePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            Util.showMessage(this, "Image selected...")
            file = result
        }

    private lateinit var currentConversationId: String
    private var chatExists = false

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        contentView = setComposeContent(R.layout.activity_conversation)

        storageController = StorageController.getInstance(this)!!

        // get uid and get participant user
        if (intent != null && intent.extras != null) {
            val uid = intent.getStringExtra("uid")!!

            UserUtils.getUserByUid(uid) { result ->
                if (result.code == ErrorCodes.SUCCESS) {
                    participant = result.user

                    if (storageController.contains("user")) {
                        user = storageController.getUser("user")
                        Log.d(TAG, "User from Storage" + user?.info.toString())

                        initialize()
                    } else {

                        UserUtils.getUserByUid(auth.currentUser!!.uid) { result2 ->
                            if (result2.code == ErrorCodes.SUCCESS) {
                                user = result2.user
                                initialize()
                            } else {
                                user = null
                                Log.e(TAG, result2.error?.message!!)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, result.error?.message!!)
                }
            }
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.right_in, R.anim.left_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.left_in, R.anim.right_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.left_in, R.anim.right_out)
        }
    }

    private fun initialize() {
        // val coordinator = findViewById<CoordinatorLayout>(R.id._coordinator)
        // val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbar = contentView.findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toolbar.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", participant?.uid)
            startActivity(intent)
        }

        backButton = toolbar.findViewById(R.id.back_button)
        participantName = toolbar.findViewById(R.id.participant_name)
        profileImage = toolbar.findViewById(R.id.profile_image)
        statusOnline = toolbar.findViewById(R.id.status_online)

        attachButton = contentView.findViewById(R.id.attach_button)
        messageInput = contentView.findViewById(R.id.message_input)
        sendButton = contentView.findViewById(R.id.send_button)
        recyclerview = contentView.findViewById(R.id.chat_recyclerview)

        attachButton.setOnClickListener { filePickerLauncher.launch("image/*") }

        val layoutManager: LinearLayoutManager =
            WrapLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        recyclerview.layoutManager = layoutManager
        recyclerview.itemAnimator = DefaultItemAnimator()

        // recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {})
        // view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        recyclerview.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                recyclerview.scrollBy(0, oldBottom - bottom)
            }
            if (bottom > oldBottom)
                recyclerview.scrollBy(0, bottom - oldBottom)
        }

        // recyclerview.smoothScrollToPosition(recyclerview.adapter!!.itemCount - 1);

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        sendButton.setOnClickListener {
            if (!chatExists) {
                chatExists = true

                ConversationUtil.newConversation(user!!, participant!!)
                    .addOnSuccessListener { conversationId ->
                        currentConversationId = conversationId
                        Log.d(TAG, "Conversation ($currentConversationId) created")
                        prepareConversation(currentConversationId)
                        Util.showMessage(this, "Setting up secure channel...")
                    }
                    .addOnFailureListener { error ->
                        chatExists = false
                        Log.e(TAG, "Error creating conversation", error)
                        Util.showMessage(
                            this,
                            error.message ?: getString(R.string.conversation__error_generic)
                        )
                    }
                return@setOnClickListener
            }

            if (!::conversationUtil.isInitialized) {
                prepareConversation(currentConversationId)
                Util.showMessage(this, "Secure channel is initializing. Please wait...")
                return@setOnClickListener
            }

            val message: String = messageInput.text.toString()

            if (file != null) {
                if (message.isNotEmpty()) {
                    conversationUtil.sendImage(file!!, message)
                    messageInput.setText("")
                } else {
                    conversationUtil.sendImage(file!!, null)
                }
                file = null
                return@setOnClickListener
            }

            if (message.isNotEmpty()) {
                conversationUtil.sendMessage(message)
                    .addOnSuccessListener { messageInput.setText("") }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Failed to send message", error)
                        Util.showMessage(
                            this,
                            error.message ?: getString(R.string.conversation__error_generic)
                        )
                    }
            }
        }

        participantName.text = participant!!.username

        if (participant?.photoUrl!!.isNotEmpty()) {
            Glide.with(applicationContext)
                .load(participant!!.photoUrl!!.toUri())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_account_circle_white)
                .circleCrop()
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle_white)
        }

        val onlineRef = users.child(participant?.uid!!).child("online")

        onlineRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.value.toString().toBoolean()

                if (online) {
                    statusOnline.visibility = View.VISIBLE
                    statusOnline.text = getString(R.string.conversation__status_online)
                } else {
                    statusOnline.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error reading 'online' value: ${error.message}")
            }
        })

        chatExists = ConversationUtil.conversationExists(user!!, participant!!)

        if (chatExists) {
            val existingConversationId =
                ConversationUtil.getConversationId(participant!!, user!!.conversations)

            if (!existingConversationId.isNullOrEmpty()) {
                currentConversationId = existingConversationId
                prepareConversation(existingConversationId)
            } else {
                Log.e(TAG, "Conversation ID not found despite chat existence")
            }
        }
    }

    private fun prepareConversation(conversationId: String) {
        val previousSecret = conversationSecret
        conversationSecret = null

        ConversationKeyManager.ensureConversationSecret(
            this,
            conversationId,
            user!!,
            participant!!,
            onSuccess = success@{ secret ->
                if (previousSecret == secret && ::conversationUtil.isInitialized) {
                    conversationSecret = secret
                    adapter?.setConversationSecret(secret)
                    return@success
                }

                conversationSecret = secret
                conversationUtil = ConversationUtil(this, conversationId, user!!, participant!!, secret)
                loadConversation(conversationId)
            },
            onError = { error ->
                Log.e(TAG, "Failed to establish secure channel", error)
                conversationSecret = previousSecret
                previousSecret?.let { adapter?.setConversationSecret(it) }
                Util.showMessage(this, "Unable to secure the conversation. Please try again.")
            }
        )
    }

    private fun loadConversation(cid: String) {
        val messages: Query = conversations.child(cid).child(Key.Conversation.MESSAGES)

        val options =
            FirebaseRecyclerOptions.Builder<Message>().setQuery(messages, Message::class.java)
                .build()

        adapter?.stopListening()

        adapter = MessageAdapter(applicationContext, options).apply {
            setConversationId(cid)
            setParticipant(participant)
            conversationSecret?.let { setConversationSecret(it) }

            registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    recyclerview.smoothScrollToPosition(positionStart)
                }
            })
        }

        recyclerview.adapter = adapter
        adapter?.startListening()
    }

    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        UserUtils.update("online", "true")
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
        super.onResume()
    }
}
