package com.lifelen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.lifelen.core.designsystem.theme.Chamber
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
private fun LifeLensApp(viewModel: MainViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    LifeLensTheme(themeMode = themeMode) {
        val navController = rememberNavController()
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(Chamber)) {
            LifeLensNavHost(navController = navController)
        }
    }
}
