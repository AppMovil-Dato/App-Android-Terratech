package com.novatech.terratech.iam.infrastructure.implementation

import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.iam.infrastructure.remote.dto.LoginDto
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AccountRepositoryImpl(
  private val api: AccountApi,
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
    val result = apiCall { api.register(RegisterDto(name, email, password, confirmation)) }
    if (!result.token.isNullOrBlank() && !result.expiresAt.isNullOrBlank()) {
      persistSession(
        Session(
          result.id,
          result.emailAddress,
          result.fullName.orEmpty(),
          result.token,
          Instant.parse(result.expiresAt),
        )
      )
    } else {
      // Compatibility with servers deployed before signup returned a session.
      try {
        login(email, password)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        throw com.novatech.terratech.core.domain.Failure("ACCOUNT_CREATED_LOGIN_REQUIRED")
      }
    }
  }

  override suspend fun login(email: String, password: String) {
    val dto = apiCall { api.login(LoginDto(email, password)) }
    persistSession(
      Session(
        dto.id,
        dto.emailAddress,
        dto.fullName.orEmpty(),
        dto.token,
        Instant.parse(dto.expiresAt),
      )
    )
  }

  private suspend fun persistSession(s: Session) {
    val previous = current.value
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
