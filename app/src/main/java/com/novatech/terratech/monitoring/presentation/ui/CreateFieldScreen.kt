package com.novatech.terratech.monitoring.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.DiscardChangesDialog
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.component.StepProgress
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.presentation.component.*
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState
import com.novatech.terratech.monitoring.presentation.viewmodel.FieldLocationViewModel

@Composable
fun CreateFieldScreen(
    busy: Boolean,
    onSave: (FieldDraft) -> Unit,
    onCancel: () -> Unit = {},
    location: FieldLocationViewModel = viewModel(),
) {
    var form by
        rememberSaveable(stateSaver = FieldEditorState.Saver) { mutableStateOf(FieldEditorState()) }
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) { location.reset() }
    val search by location.state.collectAsStateWithLifecycle()
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
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.new_field),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    if (
                        form.name.isNotBlank() || form.crop.isNotBlank() || form.points.isNotEmpty()
                    )
                        confirmDiscard = true
                    else onCancel()
                },
                enabled = !busy,
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
        StepProgress(
            form.step,
            listOf(
                stringResource(R.string.field_step_details),
                stringResource(R.string.field_step_map),
                stringResource(R.string.field_step_review),
            ),
        )
        Box(Modifier.weight(1f)) {
            when (form.step) {
                0 -> FieldDetailsForm(form) { form = it }
                1 -> FieldLocationEditor(form, search, location::search) { form = it }
                else -> FieldReview(form)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            if (form.step > 0)
                OutlinedButton({ form = form.copy(step = form.step - 1) }, enabled = !busy) {
                    Text(stringResource(R.string.back))
                }
            Box(Modifier.weight(1f)) {
                PrimaryButton(
                    stringResource(
                        if (busy) R.string.loading
                        else if (form.step == 2) R.string.create_field else R.string.continue_action
                    ),
                    !busy && (if (form.step == 0) form.validDetails else form.validLocation),
                ) {
                    focus.clearFocus()
                    if (form.step < 2) form = form.copy(step = form.step + 1)
                    else onSave(form.draft())
                }
            }
        }
    }
}
