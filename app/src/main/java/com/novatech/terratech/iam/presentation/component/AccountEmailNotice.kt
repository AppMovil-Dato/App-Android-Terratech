package com.novatech.terratech.iam.presentation.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R

@Composable
internal fun AccountEmailNotice(message: String?) {
    if (message == null) return
    Text(stringResource(if (message == "RESET_REQUESTED") R.string.reset_requested else R.string.verification_sent))
}
