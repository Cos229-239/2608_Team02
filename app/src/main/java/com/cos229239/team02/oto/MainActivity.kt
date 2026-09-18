package com.cos229239.team02.oto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cos229239.team02.oto.ui.theme.OTOTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Install the OTO system splash screen before the activity starts.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            OTOTheme {
                OTOApp()
            }

        }
    }
}