package com.novatech.terratech

import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*

class FakeAccountRepository : AccountRepository {
  override val session = MutableStateFlow<Session?>(null)
  override val restored = MutableStateFlow(true)
  var calls = 0
  var last: List<String> = emptyList()

  override suspend fun register(
    name: String,
    email: String,
    password: String,
    confirmation: String,
  ) {
    calls++
    last = listOf(name, email, password, confirmation)
  }

  override suspend fun login(email: String, password: String) {
    calls++
    last = listOf(email, password)
  }

  override suspend fun logout() {
    session.value = null
  }
}
