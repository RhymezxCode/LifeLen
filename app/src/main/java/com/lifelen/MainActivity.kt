package com.lifelen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.navigation.LifeLensNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LifeLensApp() }
    }
}

@Composable
private fun LifeLensApp() {
    val viewModel: MainViewModel = hiltViewModel()
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()

    LifeLensTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
        val navController = rememberNavController()
        LifeLensNavHost(navController = navController)
    }
}
