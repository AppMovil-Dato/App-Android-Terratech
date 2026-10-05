package com.novatech.terratech.monitoring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.monitoring.application.usecase.FindFieldLocation
import com.novatech.terratech.monitoring.presentation.state.MapSearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FieldLocationViewModel @Inject constructor(private val find: FindFieldLocation) :
    ViewModel() {
    private val mutable = MutableStateFlow(MapSearchState())
    val state = mutable.asStateFlow()
    private var job: Job? = null

    fun reset() {
        job?.cancel()
        mutable.value = MapSearchState()
    }

    fun search(query: String) {
        job?.cancel()
        job =
            viewModelScope.launch {
                mutable.value = MapSearchState(searching = true)
                try {
                    val point = find(query)
                    mutable.value = MapSearchState(result = point, error = point == null)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    mutable.value = MapSearchState(error = true)
                }
            }
    }
}
