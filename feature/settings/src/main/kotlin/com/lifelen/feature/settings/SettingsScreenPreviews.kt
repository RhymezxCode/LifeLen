package com.lifelen.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.designsystem.theme.LifeLensTheme

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    LifeLensTheme {
        SettingsScreen(
            settings = AppSettings(
                dashScopeApiKey = "sk-demo",
                searchApiKey = "",
                pricingEnabled = true,
            ),
            onDashScopeKeyChange = {},
            onSearchKeyChange = {},
            onPricingChange = {},
            onBack = {},
        )
    }
}
