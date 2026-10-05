package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.application.usecase.AccountActions
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ApplicationActionsTest {

    @Test
    fun rejectedConfirmationNeverCallsBackend() = runTest {
        val repo = FakeAccountRepository()
        try {
            AccountActions(repo).register("Ana", "a@example.com", "secret ", "secret")
            fail()
        } catch (exception: Failure) {
            assertEquals("PASSWORD_CONFIRMATION_MISMATCH", exception.code)
        }
        assertEquals(0, repo.calls)
    }

    @Test
    fun validRegistrationKeepsPasswordSpaces() = runTest {
        val repo = FakeAccountRepository()
        AccountActions(repo).register(" Ana ", " A@EXAMPLE.COM ", " secret ", " secret ")
        assertEquals(listOf("Ana", "a@example.com", " secret ", " secret "), repo.last)
    }

    @Test
    fun shortNameNeverCallsBackend() = runTest {
        val repo = FakeAccountRepository()
        try {
            AccountActions(repo).register("A", "a@example.com", "secret", "secret")
            fail()
        } catch (exception: Failure) {
            assertEquals("INVALID_NAME", exception.code)
        }
        assertEquals(0, repo.calls)
    }

    @Test
    fun loginUsesValidatedNormalizedEmail() = runTest {
        val repo = FakeAccountRepository()
        AccountActions(repo).login(" A@EXAMPLE.COM ", "secret")
        assertEquals(listOf("a@example.com", "secret"), repo.last)
    }
}
