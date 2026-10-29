package com.novatech.terratech.iam.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R

@Composable
fun AuthModeSelector(
    registering: Boolean,
    onSelectMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE7F3E8))
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val loginSelected = !registering
        Button(
            onClick = { if (registering) onSelectMode(false) },
            enabled = enabled,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (loginSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor =
                        if (loginSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                ),
            shape = RoundedCornerShape(8.dp),
            elevation =
                if (loginSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.auth_login_tab))
        }
        Button(
            onClick = { if (!registering) onSelectMode(true) },
            enabled = enabled,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (!loginSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor =
                        if (!loginSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                ),
            shape = RoundedCornerShape(8.dp),
            elevation =
                if (!loginSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.auth_register_tab))
        }
    }
}
