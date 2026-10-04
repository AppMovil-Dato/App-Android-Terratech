package com.novatech.terratech.iam.infrastructure.implementation

import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.*
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AccountRepositoryImpl(
  private val api: TerraApi,
  private val store: SessionStore,
  private val db: TerraDatabase,
  private val scope: CoroutineScope,
) : AccountRepository {
  private val current = MutableStateFlow<Session?>(null)
  private val ready = MutableStateFlow(false)
  override val session = current.asStateFlow()
  override val restored = ready.asStateFlow()

  init {
    scope.launch {
      try {
        current.value = store.load()
      } finally {
        ready.value = true
      }
    }
  }

  override suspend fun register(
    name: String,
    email: String,
    password: String,
    confirmation: String,
  ) {
    apiCall { api.register(RegisterDto(name, email, password, confirmation)) }
  }

  override suspend fun login(email: String, password: String) {
    val dto = apiCall { api.login(LoginDto(email, password)) }
    val previous = current.value
    val s =
      Session(
        dto.id,
        dto.emailAddress,
        dto.fullName.orEmpty(),
        dto.token,
        Instant.parse(dto.expiresAt),
      )
    if (previous?.userId != s.userId) {
      current.value = null
      store.clear()
      db.dao().clearPrivateData()
    }
    store.save(s)
    current.value = s
  }

  override suspend fun logout() {
    current.value = null
    store.clear()
    db.dao().clearPrivateData()
  }
}
