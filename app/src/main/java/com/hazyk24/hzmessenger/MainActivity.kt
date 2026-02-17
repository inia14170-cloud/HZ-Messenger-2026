package com.hazyk24.hzmessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hazyk24.hzmessenger.ui.AppRoot
import com.hazyk24.hzmessenger.ui.HZApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HZApp {
                AppRoot()
            }
        }
    }
}
