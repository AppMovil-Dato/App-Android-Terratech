package com.novatech.terratech.profile.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.profile.presentation.component.LogoutConfirmationDialog
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.ui.theme.*

@Composable
fun ProfileScreen(
    state: ProfileState,
    session: Session,
    onSave: (String, String, String, String, Double) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onContinue: (() -> Unit)? = null,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved) { if (state.saved) editing = false }
    ProfileContent(
        state,
        session,
        editing,
        onEditingChange = { editing = it },
        onSave = onSave,
        onContinue = onContinue,
        onRefresh = onRefresh,
        onRequestLogout = { confirmLogout = true },
    )
    if (confirmLogout) {
        LogoutConfirmationDialog(
            onDismiss = { confirmLogout = false },
            onConfirm = {
                confirmLogout = false
                onLogout()
            },
        )
    }
}
