package com.novatech.terratech.iam.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.ui.theme.*

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
  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).imePadding(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(20.dp))
    Pill("TERRATECH")
    Text(stringResource(R.string.tagline), color = Muted)
    Text(
      stringResource(if (registering) R.string.register_title else R.string.welcome),
      style = MaterialTheme.typography.headlineLarge,
    )
    FarmCard {
      if (registering)
        OutlinedTextField(
          name,
          onNameChange,
          label = { Text(stringResource(R.string.full_name)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
      OutlinedTextField(
        email,
        onEmailChange,
        label = { Text(stringResource(R.string.email)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      OutlinedTextField(
        password,
        onPasswordChange,
        label = { Text(stringResource(R.string.password)) },
        visualTransformation =
          if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      if (registering)
        OutlinedTextField(
          confirmation,
          onConfirmationChange,
          label = { Text(stringResource(R.string.confirmation)) },
          visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(visible, onVisibilityChange)
        Text(stringResource(R.string.show_password))
      }
      Notice(state.error)
      if (state.registered) Text(stringResource(R.string.registered), color = FarmGreen)
      PrimaryButton(
        stringResource(
          if (state.busy) R.string.loading
          else if (registering) R.string.register else R.string.login
        ),
        !state.busy,
      ) {
        if (registering) onRegister(name, email, password, confirmation)
        else onLogin(email, password)
      }
    }
    if (!reauth)
      TextButton(
        onClick = onToggleMode,
        enabled = !state.busy,
      ) {
        Text(stringResource(if (registering) R.string.have_account else R.string.no_account))
      }
  }
}
