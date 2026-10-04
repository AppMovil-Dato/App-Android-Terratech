package com.novatech.terratech.iam.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
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
  LaunchedEffect(state.registered) {
    if (state.registered) {
      registering = false
      password = ""
      confirmation = ""
    }
  }
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
          { name = it },
          label = { Text(stringResource(R.string.full_name)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
      OutlinedTextField(
        email,
        { email = it },
        label = { Text(stringResource(R.string.email)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      OutlinedTextField(
        password,
        { password = it },
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
          { confirmation = it },
          label = { Text(stringResource(R.string.confirmation)) },
          visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(visible, { visible = it })
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
        onClick = {
          registering = !registering
          onClear()
          password = ""
          confirmation = ""
        },
        enabled = !state.busy,
      ) {
        Text(stringResource(if (registering) R.string.have_account else R.string.no_account))
      }
  }
}
