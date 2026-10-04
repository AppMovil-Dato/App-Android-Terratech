package com.novatech.terratech.iam.application.usecase

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.domain.valueobject.*

class AccountActions(private val repository: AccountRepository) {
  suspend fun register(name: String, email: String, password: String, confirmation: String) {
    val fullName = FullName.of(name)
    val address = Email.of(email)
    val secret = Password.of(password)
    if (secret.value != confirmation) throw Failure("PASSWORD_CONFIRMATION_MISMATCH")
    repository.register(fullName.value, address.value, secret.value, confirmation)
  }

  suspend fun login(email: String, password: String) {
    repository.login(Email.of(email).value, Password.of(password).value)
  }

  suspend fun logout() = repository.logout()
}
