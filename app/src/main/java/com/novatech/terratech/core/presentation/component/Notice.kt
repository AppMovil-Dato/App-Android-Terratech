package com.novatech.terratech.core.presentation.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.format.errorText
import com.novatech.terratech.ui.theme.*

@Composable
fun Notice(error: String?, retry: (() -> Unit)? = null) {
  if (error == null) return
  val warning = error == "OFFLINE" || error == "UNAUTHENTICATED"
  Surface(
    color = if (warning) AmberLight else MaterialTheme.colorScheme.errorContainer,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(errorText(error))
      retry?.let { TextButton(onClick = it) { Text(stringResource(R.string.retry)) } }
    }
  }
}
