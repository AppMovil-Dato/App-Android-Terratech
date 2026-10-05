package com.novatech.terratech.monitoring.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import com.novatech.terratech.core.presentation.component.DiscardChangesDialog
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState
import com.novatech.terratech.monitoring.presentation.state.MapSearchState

@Composable
fun CreateFieldScreen(
    busy: Boolean,
    onSave: (FieldDraft) -> Unit,
    onCancel: () -> Unit = {},
    searchState: MapSearchState = MapSearchState(),
    onSearch: (String) -> Unit = {},
) {
    var form by
        rememberSaveable(stateSaver = FieldEditorState.Saver) { mutableStateOf(FieldEditorState()) }
    val focus = LocalFocusManager.current
    var confirmDiscard by remember { mutableStateOf(false) }
    val back = {
        if (!busy) {
            if (form.step > 0) form = form.copy(step = form.step - 1)
            else if (form.name.isNotBlank() || form.crop.isNotBlank() || form.points.isNotEmpty())
                confirmDiscard = true
            else onCancel()
        }
    }
    BackHandler { back() }
    if (confirmDiscard)
        DiscardChangesDialog(
            { confirmDiscard = false },
            {
                confirmDiscard = false
                onCancel()
            },
        )
    CreateFieldContent(
        form = form,
        busy = busy,
        searchState = searchState,
        onFormChange = { form = it },
        onSearch = onSearch,
        onCancel = {
            if (form.name.isNotBlank() || form.crop.isNotBlank() || form.points.isNotEmpty())
                confirmDiscard = true
            else onCancel()
        },
        onContinue = {
            focus.clearFocus()
            if (form.step < 2) form = form.copy(step = form.step + 1) else onSave(form.draft())
        },
    )
}
