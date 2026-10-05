package com.novatech.terratech.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.format.areaNumber
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.profile.presentation.component.ProfileForm
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.ui.theme.*

@Composable
internal fun ProfileContent(
  state: ProfileState,
  session: Session,
  editing: Boolean,
  onEditingChange: (Boolean) -> Unit,
  onSave: (String, String, String, String, Double) -> Unit,
  onRefresh: () -> Unit,
  onRequestLogout: () -> Unit,
  onContinue: (() -> Unit)? = null,
) {
  val profile = state.profile
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
        TextButton(onClick = { onEditingChange(false) }) { Text(stringResource(R.string.cancel)) }
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
          onEditingChange(true)
        }
      }
    }
    if (state.saved) {
      Pill(stringResource(R.string.profile_saved))
      if (onContinue != null)
        PrimaryButton(stringResource(R.string.profile_continue_fields), !state.busy, onContinue)
    }
    if (profile != null && !editing)
      FarmCard {
        Text(stringResource(R.string.cache), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.sync_status), color = Muted)
      }
    TextButton(onClick = onRequestLogout, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
    }
  }
}
