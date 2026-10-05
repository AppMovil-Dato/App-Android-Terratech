package com.novatech.terratech

import com.novatech.terratech.iam.application.usecase.AccountActions
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelStateTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun invalidConfirmationSetsErrorAndFinishesLoading() =
        runTest(dispatcher) {
            val repo = FakeAccountRepository()
            val vm = AccountViewModel(AccountActions(repo), repo)
            vm.register("Ana", "a@example.com", "secret", "Secret")
            advanceUntilIdle()
            assertEquals("PASSWORD_CONFIRMATION_MISMATCH", vm.state.value.error)
            assertFalse(vm.state.value.busy)
            assertFalse(vm.state.value.registered)
            assertEquals(0, repo.calls)
        }

    @Test
    fun successClearsPreviousErrorAndReturnsToLogin() =
        runTest(dispatcher) {
            val repo = FakeAccountRepository()
            val vm = AccountViewModel(AccountActions(repo), repo)
            vm.register("A", "a@example.com", "secret", "secret")
            advanceUntilIdle()
            assertEquals("INVALID_NAME", vm.state.value.error)
            vm.register("Ana", "a@example.com", "secret", "secret")
            advanceUntilIdle()
            assertNull(vm.state.value.error)
            assertTrue(vm.state.value.registered)
            assertTrue(vm.state.value.restored)
            assertFalse(vm.state.value.busy)
            assertEquals(1, repo.calls)
        }

    @Test
    fun repeatedTapOnlySendsOneRequest() =
        runTest(dispatcher) {
            val repo = FakeAccountRepository()
            val vm = AccountViewModel(AccountActions(repo), repo)
            vm.login("a@example.com", "secret")
            vm.login("a@example.com", "secret")
            advanceUntilIdle()
            assertEquals(1, repo.calls)
        }
}
