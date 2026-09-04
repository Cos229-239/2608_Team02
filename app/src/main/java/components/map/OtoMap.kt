package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

//Display OTO's shared interactive maps.
@Composable
fun OtoMap(
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null
) {

    val cameraState = rememberCameraState()

    //Center the mapwhen OTO receives a valid locations.
    LaunchedEffect(latitude, longitude) {

        if (latitude != null && longitude != null) {

            cameraState.position = CameraPosition(
                target = Position(
                    longitude = longitude,
                    latitude = latitude
                ),
                zoom = 14.0
            )
        }
    }

    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri(
            "https://tiles.openfreemap.org/styles/liberty"
        ),
        cameraState = cameraState
    )
}




