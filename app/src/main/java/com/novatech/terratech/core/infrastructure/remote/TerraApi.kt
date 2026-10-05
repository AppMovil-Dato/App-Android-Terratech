package com.novatech.terratech.core.infrastructure.remote

import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import com.novatech.terratech.profile.infrastructure.remote.ProfileApi

interface TerraApi : AccountApi, ProfileApi, MonitoringApi
