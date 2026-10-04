package com.novatech.terratech.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.application.usecase.AccountActions
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.presentation.state.AccountState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class AccountViewModel
@Inject
constructor(private val actions: AccountActions, repository: AccountRepository) : ViewModel() {
  private val mutable = MutableStateFlow(AccountState())
  val state = mutable.asStateFlow()

  init {
    viewModelScope.launch {
      combine(repository.session, repository.restored) { session, ready -> session to ready }
        .collect { (session, ready) ->
          mutable.update { it.copy(session = session, restored = ready) }
        }
    }
  }

  fun clearMessage() {
    mutable.update { it.copy(error = null, registered = false) }
  }

  fun register(name: String, email: String, password: String, confirmation: String) = run {
    actions.register(name, email, password, confirmation)
    mutable.update { it.copy(registered = true) }
  }

  fun login(email: String, password: String) = run { actions.login(email, password) }

  fun logout() = run { actions.logout() }

  private fun run(block: suspend () -> Unit) {
    if (mutable.value.busy) return
    mutable.update { it.copy(busy = true, error = null, registered = false) }
    viewModelScope.launch {
      try {
        block()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        mutable.update { it.copy(error = (e as? Failure)?.code ?: "UNKNOWN_ERROR") }
      } finally {
        mutable.update { it.copy(busy = false) }
      }
    }
  }
}
