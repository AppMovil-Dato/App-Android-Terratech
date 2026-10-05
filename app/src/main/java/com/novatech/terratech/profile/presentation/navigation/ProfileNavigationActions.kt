package com.novatech.terratech.profile.presentation.navigation

data class ProfileNavigationActions(
    val save: (String, String, String, String, Double) -> Unit,
    val refresh: () -> Unit,
    val logout: () -> Unit,
)
