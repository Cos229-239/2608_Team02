package com.cos229239.team02.oto.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


//Draw the decorative topographic lines used by the Location card.
@Composable
internal fun BoxScope.HomeLocationArtwork() {

    Canvas(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(
                width = 132.dp,
                height = 100.dp
            )
    ) {

        val contourColor =
            Color.White.copy(
                alpha = 0.08f
            )

        //Draw the outer contour line.
        val outerContour = Path().apply {
            moveTo(
                size.width * 0.12f,
                size.height * 0.20f
            )

            cubicTo(
                size.width * 0.35f,
                size.height * 0.02f,
                size.width * 0.82f,
                size.height * 0.06f,
                size.width * 0.92f,
                size.height * 0.30f
            )

            cubicTo(
                size.width * 0.98f,
                size.height * 0.50f,
                size.width * 0.76f,
                size.height * 0.66f,
                size.width * 0.54f,
                size.height * 0.52f
            )
        }

        drawPath(
            path = outerContour,
            color = contourColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the middle contour line.
        val middleContour = Path().apply {
            moveTo(
                size.width * 0.28f,
                size.height * 0.31f
            )

            cubicTo(
                size.width * 0.48f,
                size.height * 0.16f,
                size.width * 0.75f,
                size.height * 0.19f,
                size.width * 0.83f,
                size.height * 0.38f
            )

            cubicTo(
                size.width * 0.88f,
                size.height * 0.54f,
                size.width * 0.70f,
                size.height * 0.65f,
                size.width * 0.53f,
                size.height * 0.56f
            )
        }

        drawPath(
            path = middleContour,
            color = contourColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the inner contour line.
        val innerContour = Path().apply {
            moveTo(
                size.width * 0.42f,
                size.height * 0.42f
            )

            cubicTo(
                size.width * 0.56f,
                size.height * 0.31f,
                size.width * 0.70f,
                size.height * 0.34f,
                size.width * 0.76f,
                size.height * 0.47f
            )

            cubicTo(
                size.width * 0.79f,
                size.height * 0.58f,
                size.width * 0.67f,
                size.height * 0.67f,
                size.width * 0.55f,
                size.height * 0.61f
            )
        }

        drawPath(
            path = innerContour,
            color = contourColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )
    }
}

//Draw the decorative pine trees used by the Preparedness card.
@Composable
internal fun BoxScope.HomePreparednessArtwork() {

    Canvas(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(
                width = 120.dp,
                height = 105.dp
            )
    ) {

        val treeColor =
            Color.White.copy(
                alpha = 0.08f
            )

        //Draw the larger pine tree.
        val largeTree = Path().apply {
            moveTo(
                size.width * 0.70f,
                size.height * 0.12f
            )

            lineTo(
                size.width * 0.55f,
                size.height * 0.43f
            )

            lineTo(
                size.width * 0.63f,
                size.height * 0.43f
            )

            lineTo(
                size.width * 0.50f,
                size.height * 0.66f
            )

            lineTo(
                size.width * 0.62f,
                size.height * 0.66f
            )

            lineTo(
                size.width * 0.48f,
                size.height * 0.86f
            )

            lineTo(
                size.width * 0.92f,
                size.height * 0.86f
            )

            lineTo(
                size.width * 0.78f,
                size.height * 0.66f
            )

            lineTo(
                size.width * 0.90f,
                size.height * 0.66f
            )

            lineTo(
                size.width * 0.77f,
                size.height * 0.43f
            )

            lineTo(
                size.width * 0.85f,
                size.height * 0.43f
            )

            close()
        }

        drawPath(
            path = largeTree,
            color = treeColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the smaller pine tree.
        val smallTree = Path().apply {
            moveTo(
                size.width * 0.34f,
                size.height * 0.34f
            )

            lineTo(
                size.width * 0.22f,
                size.height * 0.58f
            )

            lineTo(
                size.width * 0.29f,
                size.height * 0.58f
            )

            lineTo(
                size.width * 0.18f,
                size.height * 0.77f
            )

            lineTo(
                size.width * 0.50f,
                size.height * 0.77f
            )

            lineTo(
                size.width * 0.39f,
                size.height * 0.58f
            )

            lineTo(
                size.width * 0.46f,
                size.height * 0.58f
            )

            close()
        }

        drawPath(
            path = smallTree,
            color = treeColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )
    }
}

//Draw the decorative mountain ridges used by the Explorer card.
@Composable
internal fun BoxScope.HomeExplorerArtwork() {

    Canvas(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(
                width = 125.dp,
                height = 100.dp
            )
    ) {

        val mountainColor =
            Color.White.copy(
                alpha = 0.08f
            )

        //Draw the larger mountain ridge.
        val largeMountain = Path().apply {
            moveTo(
                size.width * 0.18f,
                size.height * 0.76f
            )

            lineTo(
                size.width * 0.56f,
                size.height * 0.20f
            )

            lineTo(
                size.width * 0.88f,
                size.height * 0.76f
            )
        }

        drawPath(
            path = largeMountain,
            color = mountainColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the smaller mountain ridge.
        val smallMountain = Path().apply {
            moveTo(
                size.width * 0.46f,
                size.height * 0.72f
            )

            lineTo(
                size.width * 0.70f,
                size.height * 0.40f
            )

            lineTo(
                size.width * 0.94f,
                size.height * 0.72f
            )
        }

        drawPath(
            path = smallMountain,
            color = mountainColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw a small inner ridge for extra detail.
        val innerRidge = Path().apply {
            moveTo(
                size.width * 0.44f,
                size.height * 0.58f
            )

            lineTo(
                size.width * 0.56f,
                size.height * 0.42f
            )

            lineTo(
                size.width * 0.64f,
                size.height * 0.56f
            )
        }

        drawPath(
            path = innerRidge,
            color = mountainColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )
    }
}

//Draw the emergency radio beacon artwork used by the Crisis card.
@Composable
internal fun BoxScope.HomeCrisisArtwork() {

    Canvas(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(
                width = 125.dp,
                height = 100.dp
            )
    ) {

        val crisisArtworkColor =
            Color.White.copy(
                alpha = 0.10f
            )

        val towerX =
            size.width * 0.72f

        //Show the beacon light at the top of the emergency tower.
        drawCircle(
            color = crisisArtworkColor,
            radius = 4.dp.toPx(),
            center = Offset(
                x = towerX,
                y = size.height * 0.25f
            )
        )

        //Draw the center radio mast.
        drawLine(
            color = crisisArtworkColor,
            start = Offset(
                x = towerX,
                y = size.height * 0.29f
            ),
            end = Offset(
                x = towerX,
                y = size.height * 0.76f
            ),
            strokeWidth = 1.8.dp.toPx()
        )

        //Draw the left tower support.
        drawLine(
            color = crisisArtworkColor,
            start = Offset(
                x = towerX,
                y = size.height * 0.47f
            ),
            end = Offset(
                x = size.width * 0.58f,
                y = size.height * 0.78f
            ),
            strokeWidth = 1.5.dp.toPx()
        )

        //Draw the right tower support.
        drawLine(
            color = crisisArtworkColor,
            start = Offset(
                x = towerX,
                y = size.height * 0.47f
            ),
            end = Offset(
                x = size.width * 0.86f,
                y = size.height * 0.78f
            ),
            strokeWidth = 1.5.dp.toPx()
        )

        //Draw the base of the emergency tower.
        drawLine(
            color = crisisArtworkColor,
            start = Offset(
                x = size.width * 0.55f,
                y = size.height * 0.78f
            ),
            end = Offset(
                x = size.width * 0.89f,
                y = size.height * 0.78f
            ),
            strokeWidth = 1.5.dp.toPx()
        )

        //Draw the inner left emergency signal wave.
        val leftInnerSignal = Path().apply {
            moveTo(
                size.width * 0.64f,
                size.height * 0.28f
            )

            cubicTo(
                size.width * 0.56f,
                size.height * 0.34f,
                size.width * 0.56f,
                size.height * 0.45f,
                size.width * 0.64f,
                size.height * 0.51f
            )
        }

        drawPath(
            path = leftInnerSignal,
            color = crisisArtworkColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the outer left emergency signal wave.
        val leftOuterSignal = Path().apply {
            moveTo(
                size.width * 0.55f,
                size.height * 0.19f
            )

            cubicTo(
                size.width * 0.40f,
                size.height * 0.31f,
                size.width * 0.40f,
                size.height * 0.51f,
                size.width * 0.55f,
                size.height * 0.62f
            )
        }

        drawPath(
            path = leftOuterSignal,
            color = crisisArtworkColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the inner right emergency signal wave.
        val rightInnerSignal = Path().apply {
            moveTo(
                size.width * 0.80f,
                size.height * 0.28f
            )

            cubicTo(
                size.width * 0.88f,
                size.height * 0.34f,
                size.width * 0.88f,
                size.height * 0.45f,
                size.width * 0.80f,
                size.height * 0.51f
            )
        }

        drawPath(
            path = rightInnerSignal,
            color = crisisArtworkColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )

        //Draw the outer right emergency signal wave.
        val rightOuterSignal = Path().apply {
            moveTo(
                size.width * 0.89f,
                size.height * 0.19f
            )

            cubicTo(
                size.width * 1.02f,
                size.height * 0.31f,
                size.width * 1.02f,
                size.height * 0.51f,
                size.width * 0.89f,
                size.height * 0.62f
            )
        }

        drawPath(
            path = rightOuterSignal,
            color = crisisArtworkColor,
            style = Stroke(
                width = 1.5.dp.toPx()
            )
        )
    }
}