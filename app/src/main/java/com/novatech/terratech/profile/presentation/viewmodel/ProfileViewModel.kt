package com.novatech.terratech.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.profile.application.usecase.ProfileActions
import com.novatech.terratech.profile.presentation.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class ProfileViewModel
@Inject
constructor(private val actions: ProfileActions, private val account: AccountRepository) :
    ViewModel() {
    private val mutable = MutableStateFlow(ProfileState())
    val state = mutable.asStateFlow()
    private var observeJob: Job? = null
    private var requestJob: Job? = null

    init {
        viewModelScope.launch {
            account.session.collect { s ->
                requestJob?.cancel()
                observeJob?.cancel()
                mutable.value = ProfileState(userId = s?.userId)
                if (s != null) {
                    observeJob =
                        viewModelScope.launch {
                            actions.observe(s.userId).collect { p ->
                                mutable.update { it.copy(profile = p) }
                            }
                        }
                    refresh()
                }
            }
        }
    }

    fun refresh() = request { actions.refresh(it) }

    fun save(name: String, farm: String, phone: String, location: String, hectares: Double) =
        request {
            actions.save(it, name, farm, phone, location, hectares)
            mutable.update { it.copy(saved = true) }
        }

    fun clearMessage() {
        mutable.update { it.copy(error = null, saved = false) }
    }

    private fun request(block: suspend (Int) -> Unit) {
        val user = account.session.value?.userId ?: return
        if (mutable.value.busy) return
        requestJob =
            viewModelScope.launch {
                mutable.update { it.copy(busy = true, error = null, saved = false) }
                try {
                    block(user)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    mutable.update { it.copy(error = (e as? Failure)?.code ?: "UNKNOWN_ERROR") }
                } finally {
                    mutable.update { it.copy(busy = false, checked = true) }
                }
            }
    }
}
