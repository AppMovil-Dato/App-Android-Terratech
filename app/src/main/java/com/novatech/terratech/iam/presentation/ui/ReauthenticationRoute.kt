package com.novatech.terratech.iam.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.novatech.terratech.iam.presentation.state.AccountState

@Composable
fun ReauthenticationRoute(
    state: AccountState,
    previousToken: String,
    onLogin: (String, String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    LaunchedEffect(state.session?.token) {
        if (state.session?.token != null && state.session.token != previousToken) onDismiss()
    }
    AccountScreen(
        state = state,
        onLogin = onLogin,
        onRegister = { _, _, _, _ -> },
        onClear = onClear,
        reauth = true,
    )
}
