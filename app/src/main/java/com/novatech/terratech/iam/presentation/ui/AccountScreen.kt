package com.novatech.terratech.iam.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.novatech.terratech.iam.presentation.state.AccountState

@Composable
fun AccountScreen(
    state: AccountState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClear: () -> Unit,
    onLogout: (() -> Unit)? = null,
    reauth: Boolean = false,
) {
    var registering by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(state.session?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(state.error) {
        if (state.error == "ACCOUNT_CREATED_LOGIN_REQUIRED") registering = false
    }
    AccountContent(
        state = state,
        registering = registering,
        name = name,
        email = email,
        password = password,
        confirmation = confirmation,
        visible = visible,
        reauth = reauth,
        onNameChange = {
            name = it
            onClear()
        },
        onEmailChange = {
            email = it
            onClear()
        },
        onPasswordChange = {
            password = it
            onClear()
        },
        onConfirmationChange = {
            confirmation = it
            onClear()
        },
        onVisibilityChange = { visible = it },
        onToggleMode = {
            registering = !registering
            onClear()
            password = ""
            confirmation = ""
        },
        onLogin = onLogin,
        onRegister = onRegister,
        onLogout = onLogout,
    )
}
