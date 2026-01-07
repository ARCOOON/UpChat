package com.devusercode.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.devusercode.core.domain.chat.model.UserPair
import com.devusercode.ui.navigation.Routes

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
    // this is used by the nav host to bounce to Auth
    onLoggedOutNavigateToAuth: ((String) -> Unit)? = null,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.onStart() }
    DisposableEffect(Unit) { onDispose { vm.onStop() } }

    var menu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UpChat") },
                actions = {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Profile") }, onClick = {
                            menu = false
                            onProfile()
                        })
                        DropdownMenuItem(text = { Text("Settings") }, onClick = {
                            menu = false
                            onSettings()
                        })
                        DropdownMenuItem(text = { Text("Logout") }, onClick = {
                            menu = false
                            vm.doLogout {
                                // If NavHost passed a navigator, use it; else no-op
                                onLoggedOutNavigateToAuth?.invoke(Routes.AUTH)
                            }
                        })
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: open user picker */ }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { inner ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(inner)) { CircularProgressIndicator() }
            }

            state.error != null -> {
                Text("Error: ${state.error}", modifier = Modifier.padding(16.dp))
            }

            else -> {
                LazyColumn(Modifier.fillMaxSize().padding(inner)) {
                    items(state.conversations, key = { it.conversationId }) { pair ->
                        ConversationItem(pair, onOpenChat)
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ConversationItem(
    item: UserPair,
    onOpen: (String) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onOpen(item.conversationId) },
    ) {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(model = item.user.photoUrl, contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row {
                    Text(
                        item.user.displayName ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    PresenceDot(isOnline = item.user.online)
                }
                Text(item.lastMessageText ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PresenceDot(isOnline: Boolean) {
    val color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(Modifier.size(12.dp)) { Surface(color = color, shape = MaterialTheme.shapes.small) { Spacer(Modifier.size(12.dp)) } }
}
