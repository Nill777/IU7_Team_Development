package com.kbk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kbk.presentation.dashboard.DashboardViewModel
import com.kbk.presentation.navigation.KeystrokeApp
import com.kbk.presentation.playground.PlaygroundViewModel
import com.kbk.presentation.settings.SettingsViewModel
import com.kbk.ui.theme.KeystrokeBiometricsKeyboardSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as KeystrokeApplication
        val biometricService = app.biometricService

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                        DashboardViewModel(biometricService) as T

                    modelClass.isAssignableFrom(PlaygroundViewModel::class.java) ->
                        PlaygroundViewModel() as T

                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel() as T

                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        setContent {
            KeystrokeBiometricsKeyboardSDKTheme {
                KeystrokeApp(viewModelFactory = viewModelFactory)
            }
        }
    }
}
