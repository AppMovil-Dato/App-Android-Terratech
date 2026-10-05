package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.component.StepProgress
import com.novatech.terratech.monitoring.presentation.component.FieldDetailsForm
import com.novatech.terratech.monitoring.presentation.component.FieldLocationEditor
import com.novatech.terratech.monitoring.presentation.component.FieldReview
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState
import com.novatech.terratech.monitoring.presentation.state.MapSearchState

@Composable
fun CreateFieldContent(
    form: FieldEditorState,
    busy: Boolean,
    searchState: MapSearchState,
    onFormChange: (FieldEditorState) -> Unit,
    onSearch: (String) -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
) {
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
            TextButton(onClick = onCancel, enabled = !busy) {
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
                0 -> FieldDetailsForm(form, onFormChange)
                1 -> FieldLocationEditor(form, searchState, onSearch, onFormChange)
                else -> FieldReview(form)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            if (form.step > 0)
                OutlinedButton({ onFormChange(form.copy(step = form.step - 1)) }, enabled = !busy) {
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
                    onContinue()
                }
            }
        }
    }
}
