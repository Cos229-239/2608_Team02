package com.cos229239.team02.oto.ui.components.map


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle



//Display OTO's shared interactive map.
@Composable
fun OtoMap(
    modifier: Modifier = Modifier
) {
    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri(
            "https://tiles.openfreemap.org/styles/liberty"
        )
    )
}