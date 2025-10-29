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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devusercode.upchat.adapter.UserListAdapter
import com.devusercode.upchat.adapter.WrapLayoutManager
import com.devusercode.upchat.models.User
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.devusercode.upchat.utils.applyActivityCloseAnimation
import com.devusercode.upchat.utils.applyActivityOpenAnimation
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@RequiresApi(Build.VERSION_CODES.O)
class ListUsersActivity : AppCompatActivity() {
    private val TAG = "ListUsersActivity"

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val users = firebaseDatabase.getReference("users")

    private lateinit var scanQrcodeButton: Button
    private lateinit var recyclerview1: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: UserListAdapter
    private lateinit var noDataAvailableText: TextView
    private lateinit var searchView: SearchView

    private var selectedItem: MenuItem? = null

    enum class MenuFilter { USERNAME, EMAIL, UID }

    private val intent = Intent()
    private var selectedFilter: MenuFilter = MenuFilter.USERNAME

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_users)

        initialize()
        initializeLogic()
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        applyActivityOpenAnimation(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        applyActivityCloseAnimation(R.anim.left_in, R.anim.right_out)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initialize() {
        // val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        // val coordinator = findViewById<CoordinatorLayout>(R.id.coordinator)
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
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Scan an QR Code from a Friend")
                setBeepEnabled(false)
                setOrientationLocked(true)
                setCaptureActivity(CaptureActivityPortrait::class.java)
            }
            qrScanner.launch(options)
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

        adapter = UserListAdapter(this, options)
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
        val query = users.orderByChild(selectedFilter.toString().lowercase()).startAt(text).endAt(text + "\uf8ff")
        val options = FirebaseRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
            .setLifecycleOwner(this).build()

        adapter = UserListAdapter(this, options)
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
                selectedFilter = MenuFilter.USERNAME
            }

            R.id.menu_email -> {
                searchView.queryHint = "Email..."
                selectedFilter = MenuFilter.EMAIL
            }

            R.id.menu_uid -> {
                searchView.queryHint = "User Id..."
                selectedFilter = MenuFilter.UID
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

    private val qrScanner = registerForActivityResult(ScanContract()) { result ->
        val qrcodeData = result.contents ?: return@registerForActivityResult
        val intent = Intent(this, ConversationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("uid", qrcodeData)
        }
        startActivity(intent)
    }
}