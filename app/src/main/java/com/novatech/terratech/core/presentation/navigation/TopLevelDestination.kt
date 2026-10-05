package com.novatech.terratech.core.presentation.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.presentation.navigation.FieldsDestination
import com.novatech.terratech.monitoring.presentation.navigation.FieldsGraph
import com.novatech.terratech.profile.presentation.navigation.ProfileDestination
import com.novatech.terratech.profile.presentation.navigation.ProfileGraph

enum class TopLevelDestination(val label: Int, val graph: Any, val start: Any) {
    HOME(R.string.home, HomeGraph, HomeDestination),
    FIELDS(R.string.fields, FieldsGraph, FieldsDestination),
    PROFILE(R.string.profile, ProfileGraph, ProfileDestination);

    fun contains(destination: NavDestination?): Boolean =
        destination?.hierarchy?.any { it.hasRoute(graph::class) } == true
}
