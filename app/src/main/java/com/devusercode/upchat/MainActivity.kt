package com.devusercode.upchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.devusercode.core.domain.user.actions.SetOnlineStatus
import com.devusercode.ui.navigation.Routes
import com.devusercode.ui.navigation.UpChatNavHost
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var setOnlineStatus: SetOnlineStatus

    private val appScope = CoroutineScope(Dispatchers.Main.immediate)
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startRoute = if (auth.currentUser == null) Routes.AUTH else Routes.HOME

        setContent {
            val nav = rememberNavController()
            UpChatNavHost(nav = nav, start = startRoute)
        }

        // presence lifecycle (process-wide)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    // App enters foreground => online
                    appScope.launch { runCatching { setOnlineStatus(true) } }
                }

                override fun onStop(owner: LifecycleOwner) {
                    // App goes background => offline
                    appScope.launch { runCatching { setOnlineStatus(false) } }
                }
            },
        )
    }
}
