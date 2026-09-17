package com.cos229239.team02.oto.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.ui.theme.OtoSpacing

//Display the shared layout used by the four Home action cards.
@Composable
fun HomeActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonLabel: String,
    cardColor: Color,
    buttonColor: Color,
    buttonContentColor: Color,
    onClick: () -> Unit,
    artwork: @Composable BoxScope.() -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),

        //Add the same soft edge used by the approved Home design.
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(
                alpha = 0.22f
            )
        ),

        //Lift the card slightly away from the wilderness background.
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            //Allow each Home card to provide its own decorative artwork.
            artwork()

            //Display the card information above the decorative artwork.
            Column(
                modifier = Modifier.padding(
                    OtoSpacing.Large
                )
            ) {

                //Keep the icon, title, and description together.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    //Show the card icon inside the shared circular badge.
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White.copy(
                                    alpha = 0.14f
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(
                                30.dp
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(
                            OtoSpacing.Medium
                        )
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(
                                OtoSpacing.XSmall
                            )
                        )

                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(
                                alpha = 0.90f
                            )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        OtoSpacing.Standard
                    )
                )

                //Display the shared full-width Home action button.
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 56.dp
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonContentColor
                    )
                ) {

                    //Keep the action label and arrow centered together.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(
                            modifier = Modifier.width(
                                OtoSpacing.Small
                            )
                        )

                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(
                                24.dp
                            )
                        )
                    }
                }
            }
        }
    }
}