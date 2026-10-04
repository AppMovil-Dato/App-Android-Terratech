package com.novatech.terratech.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.ui.theme.*

@Composable
fun ProfileScreen(
  state: ProfileState,
  session: Session,
  onSave: (String, String, String, String, Double) -> Unit,
  onRefresh: () -> Unit,
  onLogout: () -> Unit,
) {
  var editing by rememberSaveable { mutableStateOf(false) }
  var confirmLogout by remember { mutableStateOf(false) }
  val profile = state.profile
  LaunchedEffect(state.saved) { if (state.saved) editing = false }
  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    PageTitle(stringResource(R.string.my_profile))
    Notice(state.error, onRefresh)
    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    FarmCard {
      Box(
        Modifier.size(72.dp).background(LeafLight, CircleShape).align(Alignment.CenterHorizontally),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          session.fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1) },
          style = MaterialTheme.typography.headlineMedium,
          color = FarmGreen,
        )
      }
      Pill(profile?.fundoName ?: "TERRATECH")
      Text(
        profile?.fullName?.ifBlank { session.fullName } ?: session.fullName,
        style = MaterialTheme.typography.headlineMedium,
      )
      Text(session.email, color = Muted)
      profile?.location?.let { Text(it, color = Muted) }
    }
    if (editing || (profile == null && state.checked && state.error == null)) {
      ProfileForm(
        profile?.fullName?.ifBlank { session.fullName } ?: session.fullName,
        profile?.fundoName.orEmpty(),
        profile?.contactPhone.orEmpty(),
        profile?.location.orEmpty(),
        profile?.sizeM2?.div(10000)?.toString().orEmpty(),
        state.busy,
        onSave,
      )
      if (profile != null)
        TextButton(onClick = { editing = false }) { Text(stringResource(R.string.cancel)) }
    } else {
      FarmCard {
        Text(stringResource(R.string.personal_data), style = MaterialTheme.typography.titleMedium)
        profile?.let {
          Text(it.contactPhone)
          Text(
            stringResource(R.string.area_ha) +
              ": " +
              (it.sizeM2?.div(10000)?.let(::areaNumber) ?: "—")
          )
        }
        PrimaryButton(
          stringResource(if (profile == null) R.string.complete_profile else R.string.edit_profile),
          !state.busy,
        ) {
          editing = true
        }
      }
    }
    if (state.saved) Pill(stringResource(R.string.profile_saved))
    FarmCard {
      Text(stringResource(R.string.cache), style = MaterialTheme.typography.titleMedium)
      Text(stringResource(R.string.sync_status), color = Muted)
    }
    TextButton(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
    }
  }
  if (confirmLogout)
    AlertDialog(
      onDismissRequest = { confirmLogout = false },
      title = { Text(stringResource(R.string.logout_title)) },
      text = { Text(stringResource(R.string.logout_body)) },
      confirmButton = {
        TextButton(
          onClick = {
            confirmLogout = false
            onLogout()
          }
        ) {
          Text(stringResource(R.string.logout))
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) }
      },
    )
}

@Composable
private fun ProfileForm(
  initialName: String,
  initialFarm: String,
  initialPhone: String,
  initialLocation: String,
  initialArea: String,
  busy: Boolean,
  onSave: (String, String, String, String, Double) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf(initialName) }
  var farm by rememberSaveable { mutableStateOf(initialFarm) }
  var phone by rememberSaveable { mutableStateOf(initialPhone) }
  var location by rememberSaveable { mutableStateOf(initialLocation) }
  var area by rememberSaveable { mutableStateOf(initialArea) }
  FarmCard {
    Text(stringResource(R.string.personal_data), style = MaterialTheme.typography.titleMedium)
    ProfileInput(name, { name = it }, R.string.full_name)
    ProfileInput(farm, { farm = it }, R.string.farm_name)
    ProfileInput(phone, { phone = it }, R.string.phone, KeyboardType.Phone)
    ProfileInput(location, { location = it }, R.string.location)
    ProfileInput(area, { area = it }, R.string.area_ha, KeyboardType.Decimal)
    PrimaryButton(stringResource(if (busy) R.string.loading else R.string.save), !busy) {
      onSave(name, farm, phone, location, area.replace(',', '.').toDoubleOrNull() ?: Double.NaN)
    }
  }
}

@Composable
private fun ProfileInput(
  value: String,
  change: (String) -> Unit,
  label: Int,
  type: KeyboardType = KeyboardType.Text,
) {
  OutlinedTextField(
    value,
    change,
    label = { Text(stringResource(label)) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = type),
  )
}
