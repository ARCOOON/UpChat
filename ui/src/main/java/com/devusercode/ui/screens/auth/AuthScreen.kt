package com.devusercode.ui.screens.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AuthScreen(onLoggedIn: () -> Unit) {
    Button(onClick = onLoggedIn) { Text("Login (stub)") }
}
