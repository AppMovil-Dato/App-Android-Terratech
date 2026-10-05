package com.novatech.terratech.core.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R

@Composable
internal fun TerraTechBottomBar(route: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        listOf("home" to R.string.home, "fields" to R.string.fields, "profile" to R.string.profile)
            .forEach { (destination, label) ->
                NavigationBarItem(
                    selected =
                        when (destination) {
                            "fields" ->
                                route in
                                    listOf(
                                        "fields",
                                        "new-field",
                                        "sensors",
                                        "register-sensor",
                                        "sensor",
                                    )
                            "home" ->
                                route == "home" ||
                                    route == "history" ||
                                    route.startsWith("reading/")
                            else -> route == destination
                        },
                    onClick = { onNavigate(destination) },
                    icon = {
                        if (destination == "fields")
                            Icon(painterResource(R.drawable.ic_fields), contentDescription = null)
                        else
                            Icon(
                                if (destination == "home") Icons.Outlined.Home
                                else Icons.Outlined.Person,
                                contentDescription = null,
                            )
                    },
                    label = { Text(stringResource(label)) },
                )
            }
    }
}
