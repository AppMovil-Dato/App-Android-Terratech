package com.novatech.terratech.profile.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.profile.presentation.component.LogoutConfirmationDialog
import com.novatech.terratech.profile.presentation.state.ProfileState

@Composable
fun ProfileScreen(
    state: ProfileState,
    session: Session,
    onSave: (String, String, String, String, Double) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onContinue: (() -> Unit)? = null,
    onSaveThresholds: (Double, Double) -> Unit = { _, _ -> },
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved) { if (state.saved) editing = false }
    ProfileContent(
        state = state,
        session = session,
        editing = editing,
        onEditingChange = { editing = it },
        onSave = onSave,
        onContinue = onContinue,
        onRefresh = onRefresh,
        onRequestLogout = { confirmLogout = true },
        onSaveThresholds = onSaveThresholds,
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
