package com.novatech.terratech.core.infrastructure.remote

import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import com.novatech.terratech.profile.infrastructure.remote.ProfileApi

/** Retrofit composition point; each context owns its endpoint contract. */
interface TerraApi : AccountApi, ProfileApi, MonitoringApi
