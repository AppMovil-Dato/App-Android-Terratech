package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.presentation.ui.*

@Composable
internal fun TerraTechBottomBar(route: String, onNavigate: (String) -> Unit) {
  NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
    listOf("home" to R.string.home, "fields" to R.string.fields, "profile" to R.string.profile)
      .forEach { (destination, label) ->
        NavigationBarItem(
          selected = route == destination,
          onClick = {
            onNavigate(destination)
          },
          icon = {
            if (destination == "fields")
              Icon(painterResource(R.drawable.ic_fields), contentDescription = null)
            else
              Icon(
                if (destination == "home") Icons.Outlined.Home else Icons.Outlined.Person,
                contentDescription = null,
              )
          },
          label = { Text(stringResource(label)) },
        )
      }
  }
}
