package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.presentation.viewmodel.FieldLocationViewModel

@Composable
fun CreateFieldRoute(
    busy: Boolean,
    onSave: (FieldDraft) -> Unit,
    onCancel: () -> Unit,
    viewModel: FieldLocationViewModel = hiltViewModel(),
) {
    val searchState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.reset() }
    CreateFieldScreen(
        busy = busy,
        onSave = onSave,
        onCancel = onCancel,
        searchState = searchState,
        onSearch = viewModel::search,
    )
}
