package com.novatech.terratech.iam.presentation.state

import com.novatech.terratech.iam.domain.entity.Session

data class AccountState(
    val restored: Boolean = false,
    val session: Session? = null,
    val busy: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false,
)
