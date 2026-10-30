package com.novatech.terratech.iam.presentation.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.iam.presentation.state.AccountMessage

@Composable
internal fun AccountEmailNotice(message: AccountMessage?) {
    val resource = when (message) {
        AccountMessage.VERIFICATION_SENT -> R.string.verification_sent
        AccountMessage.RESET_REQUESTED -> R.string.reset_requested
        null -> return
    }
    Text(stringResource(resource))
}
