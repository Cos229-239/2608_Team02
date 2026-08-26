package com.cos229239.team02.oto.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.ui.screens.home.HomeScreen
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen

@Composable
fun OtoNavigation() {
    val backStack = rememberNavBackStack(OtoRoute.Home)

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = entryProvider {

            entry<OtoRoute.Home> {
                HomeScreen(
                    onExplorerClick = {
                        backStack.add(OtoRoute.Explorer)
                    },
                    onCrisisClick = {
                        backStack.add(OtoRoute.Crisis)
                    }
                )
            }

            entry<OtoRoute.Explorer> {
                ExplorerScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<OtoRoute.Crisis> {
                ModePlaceholder(
                    title = "Crisis Mode",
                    backStack = backStack
                )
            }
        }
    )
}

@Composable
private fun ModePlaceholder(
    title: String,
    backStack: NavBackStack<NavKey>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)

        Button(
            onClick = {
                backStack.removeLastOrNull()
            }
        ) {
            Text(text = "Back")
        }
    }
}