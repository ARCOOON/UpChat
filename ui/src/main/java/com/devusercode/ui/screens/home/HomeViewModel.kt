package com.devusercode.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devusercode.core.domain.chat.actions.ListOpenConversations
import com.devusercode.core.domain.chat.model.UserPair
import com.devusercode.core.domain.user.actions.GetCurrentUser
import com.devusercode.core.domain.user.actions.ObservePresence
import com.devusercode.core.domain.user.actions.SetOnlineStatus
import com.devusercode.core.domain.user.actions.SignOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val conversations: List<UserPair> = emptyList(),
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getCurrentUser: GetCurrentUser,
        private val listConversations: ListOpenConversations,
        private val setOnline: SetOnlineStatus,
        private val observePresence: ObservePresence,
        private val signOut: SignOut,
    ) : ViewModel() {
        private val _state = MutableStateFlow(HomeUiState())
        val state: StateFlow<HomeUiState> = _state.asStateFlow()

        private var watchers: MutableMap<String, Job> = mutableMapOf()

        fun onStart() {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                val me = getCurrentUser()
                setOnline(true)
                val convs = listConversations(me.uid)
                _state.update { it.copy(isLoading = false, conversations = convs) }
                watchPresence(convs.map { it.user.uid })
            }
        }

        fun onStop() {
            watchers.values.forEach { it.cancel() }
            watchers.clear()
        }

        fun doLogout(onLoggedOut: () -> Unit) {
            viewModelScope.launch {
                runCatching { setOnline(false) }
                runCatching { signOut() }
                onLoggedOut()
            }
        }

        private fun watchPresence(uids: List<String>) {
            watchers.values.forEach { it.cancel() }
            watchers.clear()
            uids.forEachIndexed { index, id ->
                watchers[id] =
                    viewModelScope.launch {
                        observePresence(id).collect { (online, lastSeen) ->
                            _state.update { st ->
                                // Optimize: Only update the specific user at the given index
                                val updated =
                                    st.conversations.toMutableList().apply {
                                        val targetIndex = indexOfFirst { it.user.uid == id }
                                        if (targetIndex >= 0) {
                                            val pair = get(targetIndex)
                                            set(targetIndex, pair.copy(user = pair.user.copy(online = online, lastSeenEpochMs = lastSeen)))
                                        }
                                    }
                                st.copy(conversations = updated)
                            }
                        }
                    }
            }
        }
    }
