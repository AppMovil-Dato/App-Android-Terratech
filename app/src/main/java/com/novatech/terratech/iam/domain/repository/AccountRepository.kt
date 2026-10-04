package com.novatech.terratech.iam.domain.repository

import com.novatech.terratech.iam.domain.entity.Session
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
  val session: StateFlow<Session?>
  val restored: StateFlow<Boolean>

  suspend fun register(name: String, email: String, password: String, confirmation: String)

  suspend fun login(email: String, password: String)

  suspend fun logout()
}
