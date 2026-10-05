package com.novatech.terratech.iam.presentation.state

import com.novatech.terratech.iam.domain.valueobject.*

data class AccountFormErrors(
  val name: String? = null,
  val email: String? = null,
  val password: String? = null,
  val confirmation: String? = null,
) {
  val valid
    get() = name == null && email == null && password == null && confirmation == null

  companion object {
    fun validate(
      name: String,
      email: String,
      password: String,
      confirmation: String,
      registering: Boolean,
    ) =
      AccountFormErrors(
        if (registering && runCatching { FullName.of(name) }.isFailure) "INVALID_NAME" else null,
        if (runCatching { Email.of(email) }.isFailure) "INVALID_EMAIL" else null,
        if (runCatching { Password.of(password) }.isFailure) "INVALID_PASSWORD" else null,
        if (registering && password != confirmation) "PASSWORD_CONFIRMATION_MISMATCH" else null,
      )
  }
}
