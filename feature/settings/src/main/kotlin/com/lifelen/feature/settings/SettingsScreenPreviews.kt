package com.lifelen.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.ThemeMode

@Preview(name = "Settings", showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    LifeLensTheme(themeMode = ThemeMode.DARK) {
        SettingsScreen(
            settings = AppSettings(
                dashScopeApiKey = "sk-demo-key",
                searchApiKey = "",
                pricingEnabled = true,
                themeMode = ThemeMode.SYSTEM,
                autoSaveScans = false,
            ),
            onSaveKeys = { _, _ -> },
            onThemeMode = {},
            onPricingChange = {},
            onHapticsChange = {},
            onAutoSaveChange = {},
            onRememberKeysChange = {},
            onClearLibrary = {},
            onBack = {},
        )
    }
}

@Preview(name = "Settings · Light", showBackground = true, backgroundColor = 0xFFF6F8FB, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenLightPreview() {
    LifeLensTheme(themeMode = ThemeMode.LIGHT) {
        SettingsScreen(
            settings = AppSettings(dashScopeApiKey = "sk-demo-key", themeMode = ThemeMode.LIGHT),
            onSaveKeys = { _, _ -> },
            onThemeMode = {},
            onPricingChange = {},
            onHapticsChange = {},
            onAutoSaveChange = {},
            onRememberKeysChange = {},
            onClearLibrary = {},
            onBack = {},
        )
    }
}
