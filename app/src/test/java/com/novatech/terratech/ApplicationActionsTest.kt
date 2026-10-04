package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.application.usecase.AccountActions
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ApplicationActionsTest {
  class Accounts : AccountRepository {
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

  @Test
  fun rejectedConfirmationNeverCallsBackend() = runTest {
    val repo = Accounts()
    try {
      AccountActions(repo).register("Ana", "a@example.com", "secret ", "secret")
      fail()
    } catch (e: Failure) {
      assertEquals("PASSWORD_CONFIRMATION_MISMATCH", e.code)
    }
    assertEquals(0, repo.calls)
  }

  @Test
  fun validRegistrationKeepsPasswordSpaces() = runTest {
    val repo = Accounts()
    AccountActions(repo).register(" Ana ", " A@EXAMPLE.COM ", " secret ", " secret ")
    assertEquals(listOf("Ana", "a@example.com", " secret ", " secret "), repo.last)
  }

  @Test
  fun shortNameNeverCallsBackend() = runTest {
    val repo = Accounts()
    try {
      AccountActions(repo).register("A", "a@example.com", "secret", "secret")
      fail()
    } catch (e: Failure) {
      assertEquals("INVALID_NAME", e.code)
    }
    assertEquals(0, repo.calls)
  }

  @Test
  fun loginUsesValidatedNormalizedEmail() = runTest {
    val repo = Accounts()
    AccountActions(repo).login(" A@EXAMPLE.COM ", "secret")
    assertEquals(listOf("a@example.com", "secret"), repo.last)
  }
}
