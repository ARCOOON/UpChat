package com.devusercode.upchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devusercode.upchat.adapter.UserAdapter
import com.devusercode.upchat.adapter.WrapLayoutManager
import com.devusercode.upchat.models.User
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator

@RequiresApi(Build.VERSION_CODES.O)
class ListUsersActivity : AppCompatActivity() {
    private val TAG = "ListUsersActivity"

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val users = firebaseDatabase.getReference("users")

    private lateinit var scanQrcodeButton: Button
    private lateinit var recyclerview1: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: UserAdapter
    private lateinit var noDataAvailableText: TextView
    private lateinit var searchView: SearchView

    private var selectedItem: MenuItem? = null

    private val intent = Intent()
    private var menuFilter: String = "username"

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_users)

        initialize()
        initializeLogic()
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            val qrcodeData = result.contents
            val intent = Intent(this, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", qrcodeData)
            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initialize() {
        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val coordinator = findViewById<CoordinatorLayout>(R.id.coordinator)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toolbarBackButton = findViewById<Button>(R.id.back_button)

        toolbarBackButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.searchview)
        noDataAvailableText = findViewById(R.id.no_data_available_text)
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout)
        recyclerview1 = findViewById(R.id.recyclerview1)
        scanQrcodeButton = findViewById(R.id.scan_qrcode_button)

        recyclerview1.layoutManager = WrapLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        swipeRefreshLayout.setOnRefreshListener {
            loadUsers()
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }

        scanQrcodeButton.setOnClickListener {
            IntentIntegrator(this).setCaptureActivity(CaptureActivityPortrait::class.java)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setPrompt("Scan an QR Code from a Friend").setBeepEnabled(false)
                .setOrientationLocked(true).initiateScan()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterUsers(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterUsers(query)
                return true
            }
        })

        loadUsers()
    }

    private fun loadUsers() {
        val options = FirebaseRecyclerOptions.Builder<User>().setQuery(users, User::class.java)
            .build()

        adapter = UserAdapter(this, options)
        recyclerview1.adapter = adapter

        adapter.startListening()
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (adapter.itemCount > 1) {
                    recyclerview1.visibility = View.VISIBLE
                    noDataAvailableText.visibility = View.GONE
                } else {
                    recyclerview1.visibility = View.GONE
                    noDataAvailableText.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun initializeLogic() {
        if (auth.currentUser == null) {
            intent.setClass(applicationContext, StartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun filterUsers(text: String) {
        val query = users.orderByChild(menuFilter).startAt(text).endAt(text + "\uf8ff")
        val options = FirebaseRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
            .setLifecycleOwner(this).build()

        adapter = UserAdapter(this, options)
        recyclerview1.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.all_users_filter, menu)
        val defaultItem = menu.findItem(R.id.menu_username)

        defaultItem.isChecked = true // Set it as checked
        selectedItem = defaultItem

        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        // Uncheck all menu items
        if (selectedItem != null) {
            selectedItem!!.isChecked = false
        }

        when (itemId) {
            R.id.menu_username -> {
                searchView.queryHint = "Username..."
                menuFilter = "username"
            }

            R.id.menu_email -> {
                searchView.queryHint = "Email..."
                menuFilter = "email"
            }

            R.id.menu_uid -> {
                searchView.queryHint = "User Id..."
                menuFilter = "uid"
            }
        }

        // Update the checked state of the menu items
        item.isChecked = true
        selectedItem = item
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRestart() {
        super.onRestart()
        loadUsers()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}