package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

//Display OTO's shared interactive map.
@Composable
fun OtoMap(
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null
) {

    val cameraState = rememberCameraState()

    //Center the map when OTO receives a valid location.
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
    ) {

        if (latitude != null && longitude != null) {

            //Show one temporary OTO marker for Map MVP 1.
            val testMarkerSource = rememberGeoJsonSource(
                GeoJsonData.Features(
                    Point(
                        Position(
                            longitude = longitude,
                            latitude = latitude
                        )
                    )
                )
            )

            CircleLayer(
                id = "oto-test-marker",
                source = testMarkerSource,
                radius = const(9.dp),
                color = const(Color(0xFF0B5D1E)),
                strokeColor = const(Color.White),
                strokeWidth = const(3.dp)
            )
        }
    }
}