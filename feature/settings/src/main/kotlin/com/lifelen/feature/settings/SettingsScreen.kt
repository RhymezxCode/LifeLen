package com.lifelen.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.MediaIconButton
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.OnAmber
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.SheetGrabber
import com.lifelen.core.designsystem.theme.TitleStyle
import com.lifelen.core.designsystem.theme.Raised2
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onDashScopeKeyChange = viewModel::setDashScopeKey,
        onSearchKeyChange = viewModel::setSearchKey,
        onPricingChange = viewModel::setPricingEnabled,
        onBack = onBack,
    )
}

@Composable
internal fun SettingsScreen(
    settings: AppSettings,
    onDashScopeKeyChange: (String) -> Unit,
    onSearchKeyChange: (String) -> Unit,
    onPricingChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Chamber),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaIconButton(LifeLensIcons.ChevronLeft, "Back", onBack)
            Text("Settings", style = TitleStyle, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Spacer(Modifier.size(38.dp))
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("API keys", style = TitleStyle, color = TextPrimary)
            Text(
                "Keys are stored on this device only. See docs/API-KEYS.md to obtain them.",
                style = CaptionStyle,
                color = TextSecondary,
            )
            KeyField("DashScope (Qwen) API key", settings.dashScopeApiKey, onDashScopeKeyChange)
            KeyField("Search (Serper) API key", settings.searchApiKey, onSearchKeyChange)

            HorizontalDivider(color = com.lifelen.core.designsystem.theme.Hairline)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Live pricing", style = BodyStyle, color = TextPrimary)
                    Text(
                        "Search the web for current prices and buy links.",
                        style = CaptionStyle,
                        color = TextSecondary,
                    )
                }
                Switch(
                    checked = settings.pricingEnabled,
                    onCheckedChange = onPricingChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnAmber,
                        checkedTrackColor = Amber,
                        uncheckedTrackColor = Raised2,
                    ),
                )
            }

            HorizontalDivider(color = com.lifelen.core.designsystem.theme.Hairline)
            Text("LifeLens · dark theme · local storage only", style = CaptionStyle, color = TextFaint)
        }
    }
}

@Composable
private fun KeyField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = LabelStyle, color = TextSecondary)
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(com.lifelen.core.designsystem.theme.LifeLensShapes.control)
                .background(Raised)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text("Paste key…", style = BodyStyle, color = TextFaint)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = BodyStyle.copy(color = TextPrimary),
                cursorBrush = SolidColor(Amber),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
