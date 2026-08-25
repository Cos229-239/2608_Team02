package com.cos229239.team02.oto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cos229239.team02.oto.ui.theme.OTOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OTOTheme {
                OTOApp()
            }
        }
    }
}