package com.novatech.terratech.iam.infrastructure.implementation

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.iam.infrastructure.remote.dto.LoginDto
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
            try {
                login(email, password)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                throw Failure("ACCOUNT_CREATED_LOGIN_REQUIRED")
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

    private suspend fun persistSession(session: Session) {
        val previous = current.value
        if (previous?.userId != session.userId) {
            current.value = null
            store.clear()
            db.dao().clearPrivateData()
        }
        store.save(session)
        current.value = session
    }

    override suspend fun logout() {
        current.value = null
        store.clear()
        db.dao().clearPrivateData()
    }
}
