package com.novatech.terratech.iam.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.BrandMark
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.iam.presentation.component.AccountField
import com.novatech.terratech.iam.presentation.component.AccountPasswordField
import com.novatech.terratech.iam.presentation.state.AccountFormErrors
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.ui.theme.Muted

@Composable
internal fun AccountContent(
    state: AccountState,
    registering: Boolean,
    name: String,
    email: String,
    password: String,
    confirmation: String,
    visible: Boolean,
    reauth: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
) {
    var attempted by rememberSaveable(registering) { mutableStateOf(false) }
    val validation = AccountFormErrors.validate(name, email, password, confirmation, registering)
    val errors = if (attempted) validation else AccountFormErrors()
    val focus = LocalFocusManager.current
    val submit = {
        attempted = true
        if (validation.valid && !state.busy) {
            focus.clearFocus()
            if (registering) onRegister(name, email, password, confirmation)
            else onLogin(email, password)
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BrandMark()
            Text(
                stringResource(if (registering) R.string.register_title else R.string.welcome),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                stringResource(
                    if (registering) R.string.auth_register_help else R.string.auth_login_help
                ),
                color = Muted,
            )
            FarmCard {
                if (registering)
                    AccountField(
                        name,
                        onNameChange,
                        R.string.full_name,
                        errors.name,
                        enabled = !state.busy,
                    )
                AccountField(
                    email,
                    onEmailChange,
                    R.string.email,
                    if (state.error == "EMAIL_EXISTS") "EMAIL_EXISTS" else errors.email,
                    email = true,
                    enabled = !state.busy,
                )
                AccountPasswordField(
                    password,
                    onPasswordChange,
                    R.string.password,
                    errors.password,
                    visible,
                    { onVisibilityChange(!visible) },
                    registering,
                    !registering,
                    state.busy,
                    submit,
                )
                if (registering)
                    AccountPasswordField(
                        confirmation,
                        onConfirmationChange,
                        R.string.confirmation,
                        errors.confirmation,
                        visible,
                        { onVisibilityChange(!visible) },
                        true,
                        true,
                        state.busy,
                        submit,
                    )
                if (state.error != "EMAIL_EXISTS") Notice(state.error)
                PrimaryButton(
                    stringResource(
                        if (state.busy) R.string.loading
                        else if (registering) R.string.register else R.string.login
                    ),
                    !state.busy,
                    submit,
                )
            }
            if (!reauth)
                TextButton(
                    onClick = onToggleMode,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (registering) R.string.have_account else R.string.no_account
                        )
                    )
                }
        }
    }
}
