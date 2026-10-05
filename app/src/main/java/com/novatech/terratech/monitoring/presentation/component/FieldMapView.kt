package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.novatech.terratech.BuildConfig
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.ui.theme.FarmGreen
import kotlinx.coroutines.delay

@Composable
fun FieldMapView(
    points: List<Coordinates>,
    drawing: Boolean,
    modifier: Modifier = Modifier,
    target: Coordinates? = null,
    interactive: Boolean = true,
    onPoint: (Coordinates) -> Unit = {},
) {
    if (!BuildConfig.MAPS_CONFIGURED) {
        Box(modifier) { Text(stringResource(R.string.map_not_configured)) }
        return
    }
    val first = points.firstOrNull() ?: target ?: Coordinates.of(-14.0678, -75.7286)
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(first.latitude, first.longitude), 16f)
    }
    var loaded by remember { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    LaunchedEffect(loaded) {
        if (!loaded) {
            delay(15000)
            unavailable = true
        }
    }
    LaunchedEffect(target, loaded) {
        if (loaded && target != null)
            camera.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(target.latitude, target.longitude), 16f)
            )
    }
    val mapLabel = stringResource(R.string.field_map)
    val mapDescription = stringResource(if (loaded) R.string.map_ready else R.string.map_loading)
    val vertices = points.map { LatLng(it.latitude, it.longitude) }
    LaunchedEffect(points, interactive, loaded) {
        if (loaded && !interactive && vertices.isNotEmpty()) {
            val update =
                if (vertices.size > 1) {
                    val bounds =
                        LatLngBounds.Builder().apply { vertices.forEach { include(it) } }.build()
                    CameraUpdateFactory.newLatLngBounds(bounds, 48)
                } else CameraUpdateFactory.newLatLngZoom(vertices.first(), 16f)
            camera.move(update)
        }
    }

    Box(
        modifier.testTag("parcel-map").semantics {
            stateDescription = mapDescription
            contentDescription = mapLabel
        }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(mapType = MapType.HYBRID),
            uiSettings =
                MapUiSettings(
                    zoomControlsEnabled = interactive,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = interactive,
                    zoomGesturesEnabled = interactive,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                ),
            onMapLoaded = { loaded = true },
            onMapClick = {
                if (interactive && loaded) onPoint(Coordinates.of(it.latitude, it.longitude))
            },
        ) {
            vertices.forEachIndexed { i, point ->
                Marker(
                    state = rememberUpdatedMarkerState(point),
                    title = (i + 1).toString(),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                )
            }
            if (drawing && vertices.size >= 3)
                Polygon(
                    points = vertices,
                    fillColor = FarmGreen.copy(alpha = .25f),
                    strokeColor = FarmGreen,
                    strokeWidth = 5f,
                )
            else if (drawing && vertices.size >= 2)
                Polyline(points = vertices, color = FarmGreen, width = 5f)
        }
        if (!loaded) {
            if (unavailable)
                Text(stringResource(R.string.map_unavailable), Modifier.align(Alignment.Center))
            else CircularProgressIndicator(Modifier.size(32.dp).align(Alignment.Center))
        }
    }
}
