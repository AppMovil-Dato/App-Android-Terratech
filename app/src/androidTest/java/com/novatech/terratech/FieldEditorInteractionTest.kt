package com.novatech.terratech

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.monitoring.application.usecase.FindFieldLocation
import com.novatech.terratech.monitoring.domain.repository.LocationSearch
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.presentation.ui.CreateFieldScreen
import com.novatech.terratech.monitoring.presentation.viewmodel.FieldLocationViewModel
import com.novatech.terratech.ui.theme.TerraTechTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FieldEditorInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cancelPreservesDraftUntilDiscardIsConfirmed() {
        var cancelled = 0
        val location =
            FieldLocationViewModel(
                FindFieldLocation(
                    object : LocationSearch {
                        override suspend fun find(query: String): Coordinates? = null
                    }
                )
            )
        compose.setContent {
            TerraTechTheme { CreateFieldScreen(false, {}, { cancelled++ }, location) }
        }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("Field name").performTextInput("North")
        compose.onNodeWithText("Crop").performTextInput("Potato")
        compose.onNodeWithText("Continue").assertIsEnabled()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Leave without saving?").assertIsDisplayed()
        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("North").assertExists()
        compose.runOnIdle { assertEquals(0, cancelled) }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Leave").performClick()
        compose.runOnIdle { assertEquals(1, cancelled) }
    }
}
