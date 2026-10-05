package com.novatech.terratech.core.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R

@Composable
fun StepProgress(step: Int, labels: List<String>) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      stringResource(R.string.step_of, step + 1, labels.size),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    LinearProgressIndicator(
      progress = { (step + 1f) / labels.size },
      modifier = Modifier.fillMaxWidth(),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      labels.forEachIndexed { i, text ->
        Text(
          text,
          style = MaterialTheme.typography.labelSmall,
          color =
            if (i == step) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
