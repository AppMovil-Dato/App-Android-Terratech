package com.novatech.terratech.core.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.navigation.TopLevelDestination

@Composable
internal fun TerraTechBottomBar(
    selectedTab: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar(containerColor = Color.White) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                modifier = Modifier.testTag("tab-${destination.name.lowercase()}"),
                selected = selectedTab == destination,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == TopLevelDestination.FIELDS) {
                        Icon(painterResource(R.drawable.ic_fields), contentDescription = null)
                    } else {
                        Icon(
                            if (destination == TopLevelDestination.HOME) Icons.Outlined.Home
                            else Icons.Outlined.Person,
                            contentDescription = null,
                        )
                    }
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}
