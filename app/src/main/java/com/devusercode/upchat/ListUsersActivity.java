package com.devusercode.upchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.devusercode.upchat.adapter.UserAdapter;
import com.devusercode.upchat.adapter.WrapLayoutManager;
import com.devusercode.upchat.models.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ListUsersActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference users = firebaseDatabase.getReference("users");

    private RecyclerView recyclerview1;
    private SwipeRefreshLayout swiperefreshlayout1;
    private UserAdapter adapter;
    private MenuItem selectedItem;
    private TextView no_data_available_text;

    private final Intent intent = new Intent();

    private String menu_filter = "username";

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_view_all_users);
        initialize(_savedInstanceState);
        initializeLogic();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initialize(Bundle savedInstanceState) {
        AppBarLayout app_bar = findViewById(R.id.app_bar);
        CoordinatorLayout coordinator = findViewById(R.id.coordinator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SearchView searchView = findViewById(R.id.searchview);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        no_data_available_text = findViewById(R.id.no_data_available_text);
        swiperefreshlayout1 = findViewById(R.id.swiperefreshlayout1);
        recyclerview1 = findViewById(R.id.recyclerview1);

        recyclerview1.setLayoutManager(new WrapLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        swiperefreshlayout1.setOnRefreshListener(() -> {
            adapter.notifyDataSetChanged();
            swiperefreshlayout1.setRefreshing(false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filterUsers(query);
                return true;
            }
        });

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(users, User.class).build();

        adapter = new UserAdapter(getApplicationContext(), options);
        recyclerview1.setAdapter(adapter);

        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (adapter.getItemCount() > 1) {
                    recyclerview1.setVisibility(View.VISIBLE);
                    no_data_available_text.setVisibility(View.GONE);
                } else {
                    recyclerview1.setVisibility(View.GONE);
                    no_data_available_text.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initializeLogic() {
        if (auth.getCurrentUser() == null) {
            intent.setClass(getApplicationContext(), StartupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void filterUsers(String text) {
        Query query = users.orderByChild(menu_filter).startAt(text).endAt(text + "\uf8ff");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).setLifecycleOwner(this).build();

        adapter = new UserAdapter(getApplicationContext(), options);
        recyclerview1.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_all_users_filter_menu, menu);
        MenuItem defaultItem = menu.findItem(R.id.menu_username);
        defaultItem.setChecked(true); // Set it as checked
        selectedItem = defaultItem;
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Uncheck all menu items
        if (selectedItem != null) {
            selectedItem.setChecked(false);
        }

        // Update the selected item and handle the filtering logic
        switch (itemId) {
            case R.id.menu_username -> menu_filter = "username";
            case R.id.menu_email -> menu_filter = "email";
            case R.id.menu_uid -> menu_filter = "uid";
            case R.id.menu_joined -> menu_filter = "joined";
        }

        // Update the checked state of the menu items
        item.setChecked(true);
        selectedItem = item;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
