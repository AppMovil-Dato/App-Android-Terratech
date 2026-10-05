package com.novatech.terratech.core.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.novatech.terratech.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerraTechToolbar(
  title: String,
  busy: Boolean,
  onBack: (() -> Unit)? = null,
  onRefresh: (() -> Unit)? = null,
) {
  TopAppBar(
    title = {
      Text(
        title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium,
      )
    },
    navigationIcon = {
      if (onBack != null)
        IconButton(onClick = onBack, enabled = !busy) {
          Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
        }
    },
    actions = {
      if (onRefresh != null)
        IconButton(onClick = onRefresh, enabled = !busy) {
          Icon(Icons.Outlined.Refresh, stringResource(R.string.retry))
        }
    },
    colors =
      TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
  )
}
