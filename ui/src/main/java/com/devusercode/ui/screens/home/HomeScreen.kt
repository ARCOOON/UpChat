package com.devusercode.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.devusercode.core.domain.chat.model.UserPair

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
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
                        DropdownMenuItem(text = { Text("Profile") }, onClick = { menu = false; onProfile() })
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { menu = false; onSettings() })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: user list */ }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { inner ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(inner)) { CircularProgressIndicator() }
            state.error != null -> Text("Error: ${state.error}", modifier = Modifier.padding(16.dp))
            else -> LazyColumn(Modifier.fillMaxSize().padding(inner)) {
                items(state.conversations, key = { it.conversationId }) { pair ->
                    ConversationItem(pair, onOpenChat)
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(item: UserPair, onOpen: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onOpen(item.conversationId) }
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
                        modifier = Modifier.weight(1f)
                    )
                    PresenceDot(isOnline = item.user.online)
                }
                Text(item.lastMessageText ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun PresenceDot(isOnline: Boolean) {
    val color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(Modifier.size(12.dp)) {
        Surface(color = color, shape = MaterialTheme.shapes.small) { Spacer(Modifier.size(12.dp)) }
    }
}
