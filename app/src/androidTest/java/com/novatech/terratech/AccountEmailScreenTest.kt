package com.novatech.terratech

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.ui.theme.TerraTechTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccountEmailScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test
    fun emailActionsUseTheEnteredAddressWithoutSubmittingAPassword() {
        var verification = ""
        var recovery = ""
        compose.setContent {
            TerraTechTheme {
                AccountScreen(AccountState(restored = true, error = "EMAIL_NOT_VERIFIED"), { _, _ -> }, { _, _, _, _ -> }, {},
                    onRequestVerification = { verification = it }, onRequestPasswordReset = { recovery = it })
            }
        }
        compose.onNodeWithText("Email").performTextInput("test@example.com")
        compose.onNodeWithText("Forgot password?").performClick()
        compose.onNodeWithText("Resend verification email").performClick()
        assertEquals("test@example.com", recovery)
        assertEquals("test@example.com", verification)
    }
}
