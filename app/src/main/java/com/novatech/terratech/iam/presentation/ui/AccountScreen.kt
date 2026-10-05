package com.novatech.terratech.iam.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.*
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.ui.theme.*

@Composable
fun AccountScreen(
    state: AccountState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClear: () -> Unit,
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
        state,
        registering,
        name,
        email,
        password,
        confirmation,
        visible,
        reauth,
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
    )
}
