package com.devusercode.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun AuthScreen(
    onLoggedIn: () -> Unit,
    onNavigateRegister: (() -> Unit)? = null,
    vm: LoginViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("UpChat", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions = KeyboardActions(onDone = { vm.login(onLoggedIn) }),
                    trailingIcon = {
                        val text = if (passwordVisible) "HIDE" else "SHOW"
                        TextButton(onClick = { passwordVisible = !passwordVisible }) { Text(text) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(checked = state.rememberMe, onCheckedChange = { vm.onRememberMeChange(it) })
                    Spacer(Modifier.width(8.dp))
                    Text("Remember me")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { vm.forgotPassword() }) { Text("Forgot password?") }
                }

                if (state.error != null) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { vm.login(onLoggedIn) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Log in")
                }

                if (onNavigateRegister != null) {
                    OutlinedButton(
                        onClick = onNavigateRegister,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Create account") }
                }
            }
        }
    }
}
