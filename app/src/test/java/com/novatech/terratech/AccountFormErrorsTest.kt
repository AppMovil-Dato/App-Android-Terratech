package com.novatech.terratech

import com.novatech.terratech.iam.presentation.state.AccountFormErrors
import org.junit.Assert.*
import org.junit.Test

class AccountFormErrorsTest {
    @Test
    fun registrationReportsIndividualFieldsAndExactConfirmation() {
        val errors = AccountFormErrors.validate("A", "invalid", "short", "different", true)
        assertNotNull(errors.name)
        assertNotNull(errors.email)
        assertNotNull(errors.password)
        assertNotNull(errors.confirmation)
        assertFalse(errors.valid)
        assertTrue(
            AccountFormErrors.validate(
                    "Ana Torres",
                    "ana@example.com",
                    " secret ",
                    " secret ",
                    true,
                )
                .valid
        )
        assertFalse(
            AccountFormErrors.validate("Ana Torres", "ana@example.com", " secret ", "secret", true)
                .valid
        )
    }

    @Test
    fun loginDoesNotRequireRegistrationFields() {
        assertTrue(AccountFormErrors.validate("", "ana@example.com", "password", "", false).valid)
    }
}
